package controllers;

import actors.*;
import helper.*;
import play.api.data.Form;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;
import akka.actor.AbstractActor;
import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.Map;

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
import com.google.inject.Inject;

//import actors.AAActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
	final ActorRef GiveReviewActor,FetchReviewActor,FetchSuggestionReqActor, FetchReasonAndCheckinActor;
	 @Inject
	    FormFactory formFactory;
	
	@Inject 
    public HomeController(ActorSystem system) {
		GiveReviewActor = system.actorOf(GiveReview.getProps());
		FetchReviewActor = system.actorOf(FetchReview.getProps());
		FetchSuggestionReqActor = system.actorOf(FetchSuggestionRequests.getProps());
		FetchReasonAndCheckinActor = system.actorOf(FetchReasonAndCheckin.getProps());
		getCheckinSuggestionsAndReview();
		getCheckInAndReview();
		getSanctionsForCheckins();
		getSuggestionsToReason();
		
	}

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	
	public Result startApp(){
		getCheckInAndReview();
		getSanctionsForCheckins();
		return ok(views.html.index.render());
	}
    public Result index() {
        return ok(views.html.index.render());
    }
    
    public Result checkInfromForm() throws URISyntaxException, IOException, ClientProtocolException{
    	DynamicForm requestData = formFactory.form().bindFromRequest();
    	//final Map<String, String[]> form_values = request().body().asFormUrlEncoded();
    	String place = requestData.get("location");
    	String companions = requestData.get("Companions");
    	System.out.println(place);
    	System.out.println(companions);
    	return suggestCheckin(place,companions);
    }
    
    public Result setCheckIn(String place, String companions) throws URISyntaxException, IOException, ClientProtocolException {
    	String policy = "1"; //Default is share with no one
    	HashMap<String, String> policyGoals;
    	String policyCommitment = "1";
    	int policyAdaptiveInt = 0;
    	String policyAdaptive = "1";
    	try{
    		policyGoals = AdaptivePolicy.policyRecommenderGoal(place, companions, false);
    		policyCommitment = AdaptivePolicy.policyRecommenderCommitment(place, companions, false);
    		policyAdaptiveInt = AdaptivePolicy.getPolicy(place,companions,0);
    		System.out.println("policyAdaptiveInt:" + policyAdaptiveInt);
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
    	URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "setCheckIn")
    	        .setParameter("userId", "30")
    	        .setParameter("placeId", place)
    	        .setParameter("companions", companions)
    	        .setParameter("policy", policy)
    	        .build();
    	HttpGet httpget = new HttpGet(uri);
    	HttpClient client = HttpClientBuilder.create().build(); 
    	HttpResponse response = client.execute(httpget);
    	String statusCode = response.getStatusLine().toString();
    	String responseBody = EntityUtils.toString(response.getEntity());
//    	BufferedReader br = new BufferedReader(
//    		    new InputStreamReader( 
//    		        (client.execute(httpget).getEntity().getContent())
//    		    )
//    		);
//
//    		StringBuilder content = new StringBuilder();
//    		String line;
//    		while (null != (line = br.readLine())) {
//    		    content.append(line);
//    		}
//    	ObjectMapper mapper = new ObjectMapper();
//        JsonNode actualObj = mapper.readTree(responseBody);
//        Iterator<JsonNode> it = json.elements();
//    	while (it.hasNext()) {
//    		
//    	}
//    	System.out.println(httpget.getURI());
    	System.out.println("Status code: "+statusCode);
    	System.out.println(responseBody);
    	int indexCheckin = responseBody.indexOf("checkinId");//add 12
    	if(statusCode.contains("200")&& indexCheckin!=-1){
    		String checkinId = responseBody.substring(indexCheckin+12, responseBody.indexOf(",",indexCheckin+12)-1);
    		System.out.println("Checkin Id from response: "+checkinId);
    		try{
    			int check = Integer.parseInt(checkinId);
    			storeCheckinDB(checkinId,place,companions,policy,true);
    		}
    		catch(Exception e){
    			System.out.println("Checkin failed hence not inserting into the DB for feedback!");
    		}
        	
    	}
    	
    	return ok(views.html.index.render());
    	
    }	

    public Result suggestCheckin(String place, String companions) throws URISyntaxException, IOException, ClientProtocolException {
    	String policy = "1"; //Default is share with no one
    	HashMap<String, String> policyGoals;
    	String policyCommitment = "1";
    	int policyAdaptiveInt = 0;
    	String policyAdaptive = "1";
    	String argumentPolicy = "";
    	try{
    		policyGoals = AdaptivePolicy.policyRecommenderGoal(place, companions, false);
    		policyCommitment = AdaptivePolicy.policyRecommenderCommitment(place, companions, false);
    		policyAdaptiveInt = AdaptivePolicy.getPolicy(place,companions,0);
    		System.out.println("policyAdaptiveInt:" + policyAdaptiveInt);
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
    	URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "suggestCheckIn")
    	        .setParameter("userId", "30")
    	        .setParameter("placeId", place)
    	        .setParameter("companions", companions)
    	        .setParameter("policy", policy)
    	        .setParameter("policyArg", argumentPolicy)
    	        .build();
    	HttpGet httpget = new HttpGet(uri);
    	HttpClient client = HttpClientBuilder.create().build(); 
    	HttpResponse response = client.execute(httpget);
    	String statusCode = response.getStatusLine().toString();
    	String responseBody = EntityUtils.toString(response.getEntity());
//    	BufferedReader br = new BufferedReader(
//    		    new InputStreamReader( 
//    		        (client.execute(httpget).getEntity().getContent())
//    		    )
//    		);
//
//    		StringBuilder content = new StringBuilder();
//    		String line;
//    		while (null != (line = br.readLine())) {
//    		    content.append(line);
//    		}
//    	ObjectMapper mapper = new ObjectMapper();
//        JsonNode actualObj = mapper.readTree(responseBody);
//        Iterator<JsonNode> it = json.elements();
//    	while (it.hasNext()) {
//    		
//    	}
//    	System.out.println(httpget.getURI());
    	System.out.println("Status code: "+statusCode);
    	System.out.println(responseBody);
    	int indexCheckin = responseBody.indexOf("checkinId");//add 12
    	if(statusCode.contains("200")&& indexCheckin!=-1){
    		String checkinId = responseBody.substring(indexCheckin+12, responseBody.indexOf(",",indexCheckin+12)-1);
    		System.out.println("Checkin Id from response: "+checkinId);
    		try{
    			int check = Integer.parseInt(checkinId);
    			storeCheckinDB(checkinId,place,companions,policy,true);
    		}
    		catch(Exception e){
    			System.out.println("Checkin failed hence not inserting into the DB for feedback!");
    		}
        	
    	}
    	
    	return ok(views.html.index.render());
    	
    }	

	private void storeCheckinDB(String checkinId, String place, String companions, String policy, boolean suggested) {
		// TODO Auto-generated method stub
    	if(companions==null||companions.equals(""))
    		System.out.println("--------------In checkin Store-----------"+companions);
    	else
    		System.out.println("--------------In checkin Store Not null-----------"+companions);
    	String[] companionSplit = companions.split("\\|");
    	for(String s:companionSplit){
    		System.out.println(companions + " : Companions in storeCheckin ---: "+ s);
    	}
    	int insertionsForCheckin =0;
    	if(companionSplit==null || companionSplit.length ==0||companions==null||companions.equals("")){
    		companionSplit = new String[]{"30"};
    	}
    		for(String s:companionSplit){
    			if(s!=null && !s.equals("")){
    				//Insert Checkin with companion,location and place
    				insertionsForCheckin++;
    				//s = userMapping(s);
    				String CheckinInsertSqlStatmt = "INSERT INTO "
    						+ "Checkins(Id,Location,Companion,Policy,Suggested) VALUES(?,?,?,?,?)";
    				//sqlStatmt = "INSERT INTO Booking(Id,Flight,Fro,Dest,Trip,Hold) VALUES(?,?,?,?,?,?)";
    				try {
						Connection conn = connect();
						PreparedStatement pstmt  = conn.prepareStatement(CheckinInsertSqlStatmt);
						pstmt.setString(1,checkinId);
						pstmt.setString(2, place);
						pstmt.setString(3, s);
						pstmt.setString(4, policy);
						if(suggested){
							pstmt.setString(5, "true");
						}
						else{
							pstmt.setString(5, "false");
						}
		        		pstmt.executeUpdate();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Error in inserting checkins");
						e.printStackTrace();
					}
    				
    			}
    		}

		
	}
    
	public Result getSuggestionsToReason(){
    	ObjectNode message = Json.newObject();
    	message.put("action","FetchReasoning");
    	System.out.println("FetchReasoning called");
    	FetchReasonAndCheckinActor.tell(new FetchReasoning(message),null);
    	return ok(views.html.index.render()); 
    }
	
	public Result getSanctionsForCheckins(){
    	ObjectNode message = Json.newObject();
    	message.put("action","CheckInReview");
    	System.out.println("Get Sanctions called");
    	FetchReviewActor.tell(new FetchReviewMessage(message),null);
    	return ok(views.html.index.render()); 
    }
    
    
    
    public Result getCheckInAndReview(){
    	ObjectNode message = Json.newObject();
    	message.put("action","CheckInReview");
    	GiveReviewActor.tell(new GiveReviewMessage(message),null);
    	return ok(views.html.index.render());    
    }
    
    public Result getCheckinSuggestionsAndReview(){
    	ObjectNode message = Json.newObject();
    	message.put("action","CheckInReview");
    	FetchSuggestionReqActor.tell(new FetchSuggestionReqMessage(message),null);
    	return ok(views.html.index.render());    
    }
    
    public void getReasoningForCheckin(){
    	
    }
    
/*
	public Result getCheckInAndReview() throws URISyntaxException, IOException, ClientProtocolException {
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
    		System.out.println("Has Next");
    		Iterator<JsonNode> it1 = actor.elements();
    		System.out.println(actor.asText());
    		
    		while (it1.hasNext()){
    			JsonNode ac1 = it1.next();
    			System.out.println("Checkin: ");
    			System.out.println(("userId id: "+ac1.get("userId").toString()));
    			System.out.println("Checkin id: "+ac1.get("checkinId").toString());
    			System.out.println("placeId id: "+ac1.get("placeId").toString());
    			System.out.println("policyId id: "+ac1.get("policyId").toString());
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
    			System.out.println(ac1.asText());
    		
    	}
    	}
    	//System.out.println(httpget.getURI());
    	//System.out.println(statusCode);
    	//System.out.println(responseBody);
    	return ok(views.html.index.render());    	
    }
    
    /*
     * Suggest Policy:
     * STEPS:
     * 1. Find group based on companions
     * 2. Find Policy based on group and place
     */
	
    public static Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:SIPAdb.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            return conn;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		return conn; 
    }
    
    
}

