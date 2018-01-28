package actors;

import java.io.BufferedReader;
import java.io.IOException;
import helper.*;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.AbstractActor;
import akka.actor.Props;
import helper.AdaptivePolicy;
import play.mvc.Result;

public class GiveReview extends AbstractActor{

	public static Props getProps() {
        return Props.create(GiveReview.class);
    }
	@Override
	public Receive createReceive() {
		// TODO Auto-generated method stub
		return receiveBuilder()
				.match(GiveReviewMessage.class, message -> {
					Runnable helloRunnable = new Runnable() {
					    public void run() {
					    	try{
					    		getCheckInAndReview();
					    		//System.out.println("Set Check in Reviews");
					    	}
					    	catch(Exception e){
					    		System.out.println(e.getMessage());
					    	}
					        //System.out.println("Hello world");
					    }
					};
					ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
					executor.scheduleAtFixedRate(helloRunnable, 0, 5, TimeUnit.SECONDS);

				})
				.build();
		//return null;
	}
	
	public void getCheckInAndReview() throws URISyntaxException, IOException, ClientProtocolException {
    	String policy = "1";
    	URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "getUnattendedCheckins")
    	        .setParameter("userId", "30")
    	        .build();
    	HttpGet httpget = new HttpGet(uri);
    	HttpClient client = HttpClientBuilder.create().build(); 
    	HttpResponse response = client.execute(httpget);
    	String statusCode = response.getStatusLine().toString();
    	String responseBody = EntityUtils.toString(response.getEntity());
    	BufferedReader br = new BufferedReader(
    		    new InputStreamReader( 
    		        (client.execute(httpget).getEntity().getContent())
    		    )
    		);

    		StringBuilder content = new StringBuilder();
    		String line;
    		while (null != (line = br.readLine())) {
    		    content.append(line);
    		}
    	ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(responseBody);
        Iterator<JsonNode> it = actualObj.elements();
    	while (it.hasNext()) {
    		JsonNode actor = it.next();
    		//String acName = actor.get("userName").toString();
    		//System.out.println("Has Next");
    		Iterator<JsonNode> it1 = actor.elements();
    		//System.out.println(actor.asText());
    		
    		while (it1.hasNext()){
    			JsonNode ac1 = it1.next();
//    			System.out.println("Checkin: ");
//    			System.out.println(("userId id: "+ac1.get("userId").toString()));
//    			System.out.println("Checkin id: "+ac1.get("checkinId").toString());
//    			System.out.println("placeId id: "+ac1.get("placeId").toString());
//    			System.out.println("policyId id: "+ac1.get("policyId").toString());
    			String userId =ac1.get("userId").toString();
    			String checkinId =ac1.get("checkinId").toString();
    			String placeId =ac1.get("placeId").toString();
    			String policyId =ac1.get("policyId").toString();
    			try{
    				Review(userId,checkinId,placeId,policyId);
    			}
    			catch(Exception e){
    				System.out.println("Exception in setting review");
    			}
    			//System.out.println(ac1.asText());
    		
    	}
    	}
    	//System.out.println(httpget.getURI());
    	//System.out.println(statusCode);
    	//System.out.println(responseBody);
    	return ;//ok(views.html.index.render());    	
    }
    
	private void Review(String userId, String checkinId, String placeId, String policyId) throws URISyntaxException, ClientProtocolException, IOException {
    	policyId = policyId.replaceAll("^\"|\"$", "");
		/*System.out.println("-------------------------Review------------------");
    	System.out.println("-------------------------Review------------------");
    	System.out.println("-------------------------Review------------------");
    	System.out.println("-------------------------Review------------------");
    	System.out.println(policyId);
    	System.out.println("-------------------------Review------------------");
    	System.out.println("-------------------------Review------------------");
    	System.out.println("-------------------------Review------------------");
		*/
		String policy = "-99";
    	String commitmentPolicy = "";
    	String policyAdaptive = "1";
    	int policyAdaptiveInt = 0;
    	if(checkinId.startsWith("\""))
    		checkinId = checkinId.substring(1, checkinId.length()-1);
    	String sanctionId = "-99"; //Positive by default
    	String queryTaggedCheckin = "Select Policy from TaggedCheckin where CheckinId = ?";
    	try{
			Connection conn = connect();
			PreparedStatement pstmt  = conn.prepareStatement(queryTaggedCheckin);
			pstmt.setString(1,checkinId);
			ResultSet rs  = pstmt.executeQuery();
			while (rs.next()){
				policy = rs.getString("Policy");
				//System.out.println("Setting group from db to "+ group);
			}
			if(policyId.equals(policy)){
				sanctionId = "1";
				//System.out.println("SETTING POSITIVE FROM TAGGEDCHECKIN");
			}
			else{
				sanctionId = "2";
				//System.out.println("SETTING NEGATIVE FROM TAGGEDCHECKIN");
			}
		}
		catch(Exception e){
			System.out.println("Setting policy to -99 as exception:" + e.getMessage());
			sanctionId = "-99";
		}
    	
    	if(sanctionId.equals("-99")){
    	try{
    		policy = policyRecommenderGoal(placeId, userId, true);
    		commitmentPolicy = policyRecommenderCommitment(placeId, userId, true);
    		policyAdaptiveInt = AdaptivePolicy.getPolicy(placeId,userId,0);
    		policyAdaptive = String.valueOf(policyAdaptiveInt);
    		if(policyId.equals(policy)|| policyId.equals(commitmentPolicy) || policyId.equals(policyAdaptive)){ 
    			sanctionId = "1";
    		}
    		else{
    			sanctionId = "2";
    		}
    			
    	}
    	catch(Exception e){
    		System.out.println("Exception in getting policy"+ e.getMessage());
    		System.out.println("Giving positive review because of exception");
    	}
    	}
    	/*
    	 * If policy is not equal to recommended policy, set negative sanction i.e 2 else positive i.e 1;
    	 */
    	//System.out.println("Giving sanction from review helper:"+ sanctionId);
    	URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "respondToCheckin")
    	        .setParameter("checkinId", checkinId)
    	        .setParameter("sanctionId", sanctionId)
    	        .setParameter("companionId", "30")
    	        .build();
    	HttpGet httpget = new HttpGet(uri);
    	HttpClient client = HttpClientBuilder.create().build(); 
    	HttpResponse response = client.execute(httpget);
    	String statusCode = response.getStatusLine().toString();
    	if(statusCode.contains("200"))
    		AdaptivePolicy.deleteTaggedCheckin(checkinId);
    	//System.out.println("Status response from setting feedback:"+ statusCode);
		
	}
	
	public String policyRecommenderCommitment(String place, String companions,Boolean review){
    	String groupSqlStatmt = "";
    	String policySqlStatmt = "";
    	String policy = "0"; //Default policy to share with no one
    	groupSqlStatmt = "Select * from Relationship where UserId = ?";
    	String group="Alone"; //Default group is alone
    	//System.out.println("Starting Steps of Commitment policy recomender ...");
		/*
    	 * Find group
    	 */
    	//System.out.println("Commitment: Step 1: Find Group");
    	if(companions!=null){
    		//System.out.println("Commitment:In finding group");
    		String[] companionList = companions.split("|");
    		if(companionList!=null || review){
    			String comp1 = "";
    			if(!review)
    				comp1 = companionList[0];
    			else
    				comp1 = companions;
    			if(comp1.equals("")){
    				//System.out.println("Commitment: Setting group to Alone as no companions found");
    				group = "Alone";
    			}
    			else{
    				try{
    					Connection conn = connect();
    					PreparedStatement pstmt  = conn.prepareStatement(groupSqlStatmt);
    					pstmt.setString(1,comp1);
    					ResultSet rs  = pstmt.executeQuery();
    					while (rs.next()){
    						group = rs.getString("Group");
    						//System.out.println("Commitment: Setting group from db to "+ group);
    					}
    					// The companions mentioned do not belong to relation
    					if(group.equals("Alone")){
    						group = "Crowd";
    					}
    				}
    				catch(Exception e){
    					System.out.println("Commitment:Setting group to Alone as exception:" + e.getMessage());
    					group = "Alone";
    				}
    			}
    		}				
    	}
    	else{
    		group = "Alone";
    		//System.out.println("Commitment:Setting group to Alone as param companions is null");
    	}
    	//System.out.println("Group:" + group);
    	/*
    	 * End of find group
    	 */
    	/*
    	 * Start of find commitment policy based on group and place
    	 */
//    	System.out.println("Commitment: End of step 1 : Find Group");
//    	System.out.println("Start of step 2: Determine Commitment policy based on commitment");
//    	System.out.println("Place: "+place);
    	if(place.length()!=1)
    		place = place.substring(1, place.length()-1);
    	//System.out.println("Group:" + group);
    	if(place!=null){
    		policySqlStatmt = "Select Commited_Policy from Commitments where Context_Location = ? and (Context_Companion = ? or Context_Companion = \"Any\")";
    		try{
				Connection conn = connect();
				PreparedStatement pstmt  = conn.prepareStatement(policySqlStatmt);
				pstmt.setString(1,place);
				pstmt.setString(2,group);
				//System.out.println(pstmt.toString());
				ResultSet rs  = pstmt.executeQuery();
				if (!rs.isBeforeFirst() ) {    
				    System.out.println("No Commitments for this context"); 
				} 
				
				while (rs.next()){
					policy = rs.getString("Commited_Policy");
					//System.out.println("Policy from inside commitment sql statement: "+ policy);
				}
			}
			catch(Exception e){
				//Default policy : Share with no one
				policy = "0";
				//System.out.println("Setting policy to 0 as exception in db: " + e.getMessage());
			}
    	}
    	/*
    	 * place==null so chose default policy of sharing with none , i.e: "1"
    	 */
    	else{
    		//System.out.println("Setting policy to 0 as place was null");
    		policy = "0";
    	}
    	//System.out.println("Policy from Commitment recomender: " + policy);
    	return policy;
    
    	
    }
    
    public String policyRecommenderGoal(String place, String companions,Boolean review){
    	String groupSqlStatmt = "";
    	String policySqlStatmt = "";
    	String policy = "1"; //Default policy to share with no one
    	groupSqlStatmt = "Select * from Relationship where UserId = ?";
    	String group="Alone"; //Default group is alone
    	//System.out.println("Starting Steps of policy recomender...");
		/*
    	 * Find group
    	 */
    	//System.out.println("Step 1: Find Group");
    	if(companions!=null){
    		//System.out.println("In finding group");
    		String[] companionList = companions.split("|");
    		if(companionList!=null || review){
    			String comp1 = "";
    			if(!review)
    				comp1 = companionList[0];
    			else
    				comp1 = companions;
    			if(comp1.equals("")){
    				//System.out.println("Setting group to Alone as no companions found");
    				group = "Alone";
    			}
    			else{
    				try{
    					Connection conn = connect();
    					PreparedStatement pstmt  = conn.prepareStatement(groupSqlStatmt);
    					pstmt.setString(1,comp1);
    					ResultSet rs  = pstmt.executeQuery();
    					while (rs.next()){
    						group = rs.getString("Group");
    						//System.out.println("Setting group from db to "+ group);
    					}
    					// The companions mentioned do not belong to relation
    					if(group.equals("Alone")){
    						group = "Crowd";
    					}
    				}
    				catch(Exception e){
    					System.out.println("Setting group to Alone as exception:" + e.getMessage());
    					group = "Alone";
    				}
    			}
    		}				
    	}
    	else{
    		group = "Alone";
    		//System.out.println("Setting group to Alone as param companions is null");
    	}
    	//System.out.println("Group:" + group);
    	/*
    	 * End of find group
    	 */
    	/*
    	 * Start of find policy based on group and place
    	 */
//    	System.out.println("End of step 1 : Find Group");
//    	System.out.println("Start of step 2: Determine policy");
//    	System.out.println("Place: "+place);
    	if(place.length()!=1)
    		place = place.substring(1, place.length()-1);
    	//System.out.println("Group:" + group);
    	if(place!=null){
    		policySqlStatmt = "Select Policy from Goals where Location = ? and Companion = ?";
    		try{
				Connection conn = connect();
				PreparedStatement pstmt  = conn.prepareStatement(policySqlStatmt);
				pstmt.setString(1,place);
				pstmt.setString(2,group);
				//System.out.println(pstmt.toString());
				ResultSet rs  = pstmt.executeQuery();
				if (!rs.isBeforeFirst() ) {    
				    System.out.println("No Goals data"); 
				} 
				
				while (rs.next()){
					policy = rs.getString("Policy");
					//System.out.println("Policy from inside sql statement: "+ policy);
				}
			}
			catch(Exception e){
				//Default policy : Share with no one
				policy = "1";
				//System.out.println("Setting policy to 1 as exception in db: " + e.getMessage());
			}
    	}
    	/*
    	 * place==null so chose default policy of sharing with none , i.e: "1"
    	 */
    	else{
    		//System.out.println("Setting policy to 1 as place was null");
    		policy = "1";
    	}
    	//System.out.println("Policy from Goal reccomender: " + policy);
    	return policy;
    			
    }
    
    public static Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:SIPAdb.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            //System.out.println("Connection to SQLite has been established.");
            return conn;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		return conn; 
    }
    

}
