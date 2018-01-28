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
import java.util.HashMap;
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

public class FetchSuggestionRequests extends AbstractActor{

	public static Props getProps() {
        return Props.create(FetchSuggestionRequests.class);
    }
	@Override
	public Receive createReceive() {
		// TODO Auto-generated method stub
		return receiveBuilder()
				.match(FetchSuggestionReqMessage.class, message -> {
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
    	        .setParameter("query", "getCheckinsToSuggest")
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
    			String policyId =ac1.get("suggestedPolicyId").toString();
    			try{
    				//System.out.println("-----------------Reviewing for checkin: " +checkinId);
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
    
	private void Review(String companions, String checkinId, String place, String policyId) throws URISyntaxException, ClientProtocolException, IOException {
		String policy = "1"; //Default is share with no one
    	HashMap<String, String> policyGoals;
    	String policyCommitment = "1";
    	int policyAdaptiveInt = 0;
    	String policyAdaptive = "1";
    	String argumentPolicy = "";
    	if(checkinId.startsWith("\""))
    		checkinId = checkinId.substring(1, checkinId.length()-1);
    	try{
    		policyGoals = AdaptivePolicy.policyRecommenderGoal(place, companions, false);
    		policyCommitment = AdaptivePolicy.policyRecommenderCommitment(place, companions, false);
    		policyAdaptiveInt = AdaptivePolicy.getPolicy(place,companions,0);
    		//System.out.println("policyAdaptiveInt:" + policyAdaptiveInt);
    		if(!(policyCommitment== null || policyCommitment.equals("0"))){
    			policy = policyCommitment;
    		}
    		else
    		{
    			if(policyAdaptiveInt == 0){
    				policy = policyGoals.get("policy");
    			}
    			else
    			{
    				if(policyGoals!=null && policyGoals.get("priority")!=null 
    						&& policyGoals.get("priority").equals("Low")){
    					policy = String.valueOf(policyAdaptiveInt);
    				}
    				if(policyGoals!=null && policyGoals.get("priority")!=null 
    						&& policyGoals.get("priority").equals("High")){
    					policy = policyGoals.get("policy");
    				}
    				if(policyGoals==null || policyGoals.get("policy")==null){
    					policy = String.valueOf(policyAdaptiveInt);
    				}
    			}
    				
    		}
    	}
    	catch(Exception e){
    		System.out.println("Exception in getting policy"+ e.getMessage());
    		policy = "1";
    	}
    	try{
    		argumentPolicy = AdaptivePolicy.getArgumentPolicy(policy);
    		if(argumentPolicy==null){
    			argumentPolicy = "promote()";
    		}
    	}
    	catch(Exception e){
    		System.out.println(e.getMessage());
    		argumentPolicy = "promote()";
    	}
    	//System.out.println("Giving sanction from review helper:"+ sanctionId);
    	//System.out.println("Giving suggestion for checkinId:" + checkinId + " for place: "+ place);
    	URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "respondToCheckinSuggestion")
    	        .setParameter("checkinId", checkinId)
    	        .setParameter("companionId", "30")
    	        .setParameter("responsePolicy", policy)
    	        .setParameter("responseArg", argumentPolicy)
    	        .build();
    	HttpGet httpget = new HttpGet(uri);
    	HttpClient client = HttpClientBuilder.create().build(); 
    	HttpResponse response = client.execute(httpget);
    	String statusCode = response.getStatusLine().toString();
    	String responseBody = EntityUtils.toString(response.getEntity());
    	//System.out.println("StatusCode:"+ statusCode + "Response: "+ responseBody);
    	if(statusCode.contains("200")){
    		AdaptivePolicy.insertSuggestedCheckin(checkinId,policy);
    	};
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
