package actors;

import java.util.*;
import java.net.URI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.*;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.AbstractActor.Receive;
import play.libs.Json;


public class FetchReasonAndCheckin extends AbstractActor{

	public static Props getProps() {
        return Props.create(FetchReasonAndCheckin.class);
    }
 
@Override
	public Receive createReceive() {
	return receiveBuilder()
			.match(FetchReasoning.class, message -> {
				Runnable helloRunnable = new Runnable() {
				    public void run() {
				    	try{
				    		fetchResoningAndCheckin();
				    		//System.out.println("Set Check in Reviews");
				    	}
				    	catch(Exception e){
				    		System.out.println(e.getMessage());
				    	}
				        ////system.out.println("Hello world");
				    }
				};
				ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
				executor.scheduleAtFixedRate(helloRunnable, 0, 10, TimeUnit.SECONDS);

			})
			.build();
}

/*
 * Select checkin where it's a Suggested = true
 * For those checkins fetch suggestions
 * If greater than 1 suggestion, reason about it.
 * Discard suggestions where what you promote is being demoted.
 * Pick the suggestion which hs the highest match with your promote list.
 * Use that policy to set final checkin
 */
	private void fetchResoningAndCheckin() throws URISyntaxException, IOException, ClientProtocolException{
	String selectQuerySuggested = "Select * from Checkins where Suggested = ?";
	//System.out.println("In Samir fetch Reasoning method");
	Map<String,String> checkinsToReason = new HashMap<>();
	String cId = "";
	String policy = "";
	try{
		Connection conn = connect();
		PreparedStatement pstmt  = conn.prepareStatement(selectQuerySuggested);
		pstmt.setString(1,"true");
		ResultSet rs  = pstmt.executeQuery();
		while (rs.next()){
			cId = rs.getString("Id");
			policy = rs.getString("Policy");
			checkinsToReason.put(cId, policy);
			//System.out.println("Checkin: "+ cId + "Policy: " +policy);
		}
	}
	catch(Exception e){
		System.out.println(e.getMessage());
	}	
	for(String checkID:checkinsToReason.keySet()){
		//System.out.println(checkID + " adding to reason loop");
		fetchAndReason(checkID,checkinsToReason.get(checkID));
	}
	
	
}
	private void fetchAndReason(String checkinID, String policy) throws URISyntaxException, IOException, ClientProtocolException{
		//System.out.println("*********************Checkin: "+checkinID);
		String policyToSet = policy;
		policy = policy.replaceAll("\"", "");
		int fame =0,privacy=0,safety=0,pleasure=0;
		HashMap<String,String> policyArgMap = new HashMap<>();
		List<String> policyList = new ArrayList<>();
		boolean demoted = false;
		URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "getAllCheckinSuggestions")
    	        .setParameter("checkinId", checkinID)
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
    			String suggestedPolicyId =ac1.get("suggestedPolicyId").toString();
    			String policyArg =ac1.get("policyArg").toString();
    			String[] promoteDemote = policyArg.split("\\+");
    			demoted= false;
    			for(String s: promoteDemote){
    				if(s.contains("demote")){
    					if(policy.contains("1")){
    						if(s.contains("privacy"))
    							demoted = true;
    					}
    					else if(policy.contains("2")){
    						if(s.contains("privacy")|| s.contains("pleasure") )
    							demoted = true;
    					}
    					else if(policy.contains("3")){
    						if(s.contains("pleasure"))
    							demoted = true;
    					}
    					else if(policy.contains("4")){
    						if(s.contains("safety")|| s.contains("pleasure")|| s.contains("fame"))
    							demoted = true;
    					}
    				}				
    			}
    			if(!demoted){
    				policyList.add(suggestedPolicyId);
    				//System.out.println(suggestedPolicyId + " Added to policyList");
    			}
    					
    			//System.out.println("suggestedPolicyId: " + suggestedPolicyId + "PolicyArg: "+policyArg);
    			
    		}
    	}
    	policyToSet = getMaxOccurence(policyList);
    	if(policyToSet.equals("0"))
    		policyToSet = policy;
    	finalizeCheckin(checkinID,policyToSet);
    	//System.out.println("**********************Initial Policy: " + policy);
	}

	private void finalizeCheckin(String checkinID, String policyToSet) throws URISyntaxException, IOException, ClientProtocolException{
		//System.out.println("finalizeCheckin Called with checkin and policy:"+ checkinID + " ::: " + policyToSet);
		policyToSet = policyToSet.replaceAll("^\"|\"$", "");
		checkinID = checkinID.replaceAll("^\"|\"$", "");
		URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "setFinalCheckinPolicy")
    	        .setParameter("checkinId", checkinID)
    	        .setParameter("userId", "30")
    	        .setParameter("policy", policyToSet)
    	        .build();
		HttpGet httpget = new HttpGet(uri);
    	HttpClient client = HttpClientBuilder.create().build(); 
    	HttpResponse response = client.execute(httpget);
    	String statusCode = response.getStatusLine().toString();
    	if(statusCode.contains("200")){
    		//System.out.println("updateCheckin in DB Called");
    		updateCheckin(checkinID,policyToSet);
    	}
    	
		
	}

	private void updateCheckin(String checkinID, String policyToSet) {
		policyToSet = policyToSet.replaceAll("^\"|\"$", "");
		System.out.println("updateCheckin in DB Function");
		String updateQuery = "Update Checkins SET Policy = ?, Suggested = ? where Id = ? ";
		try{
			Connection conn = connect();
			PreparedStatement pstmt  = conn.prepareStatement(updateQuery);
			pstmt.setString(1,policyToSet);
			pstmt.setString(2,"false");
			pstmt.setString(3,checkinID);
			pstmt.executeUpdate();
			System.out.println("Updated in DB");
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}

	private String getMaxOccurence(List<String> policyList) {
		if(policyList==null || policyList.size()==0){
			return "0";
		}
		HashMap<String,Integer> hm = new HashMap<>();
		for(String s: policyList){
			if(hm.containsKey(s))
				hm.put(s, hm.get(s)+1);
			else
				hm.put(s,1);
		}
		int max = -99;
		String policyToChoose ="0";
		for(String s1:hm.keySet()){
			if(hm.get(s1)>max){
				max = hm.get(s1);
				policyToChoose = s1;
			}
		}
		return policyToChoose;
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
