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

public class FetchReview extends AbstractActor{
	public class sanctionNode{
		public String CheckinId;
		public String CompanionId;
	}
	
	public static Props getProps() {
        return Props.create(FetchReview.class);
    }
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(FetchReviewMessage.class, message -> {
					//System.out.println("Inside Actor: Get Sanctions called");
					Runnable helloRunnable = new Runnable() {
					    public void run() {
					    	try{
					    		getReviewsForUnreviewedCheckins();
					    		//System.out.println("Get Check in Sanctions");
					    	}
					    	catch(Exception e){
					    		System.out.println(e.getMessage());
					    	}
					        //System.out.println("Hello world");
					    }
					};
					//System.out.println("Inside Actor: Scheduling!!");
					ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
					executor.scheduleAtFixedRate(helloRunnable, 0, 30, TimeUnit.SECONDS);
				}).build();
	}
	/*
	 * Method: getReviewsForUnreviewedCheckins
	 * Select check-ins,Companion for which sanction is null
	 * For every such check-in/companion pair, get request to review.
	 * Parse the sanction and update the record.
	 * 
	 */
	
	private void getReviewsForUnreviewedCheckins(){
		//System.out.println("getReviewsForUnreviewedCheckins inside Actor is called");
		String unSanctionedSqlStatmt = "";
		unSanctionedSqlStatmt = "Select * from Checkins where Sanction is NULL and (Suggested <>? or Suggested is NULL)";
		List<sanctionNode> unsanctionedList = new ArrayList<>();
		
		try{
			Connection conn = connect();
			PreparedStatement pstmt  = conn.prepareStatement(unSanctionedSqlStatmt);
			pstmt.setString(1, "true");
			ResultSet rs  = pstmt.executeQuery();
			while (rs.next()){
				sanctionNode sn = new sanctionNode();
				sn.CheckinId = rs.getString("Id");
				sn.CompanionId = rs.getString("Companion");
				unsanctionedList.add(sn);
			}
			for(sanctionNode sn1:unsanctionedList){
				//System.out.println("CheckinID: "+ sn1.CheckinId + " CompanionID: "+ sn1.CompanionId);
			}
			setSanction(unsanctionedList);
		}
		catch(Exception e){
			System.out.println("Error in getReviewsForUnreviewedCheckins connect 1" + e.getMessage());
			
		}
			
		
	}
    private void setSanction(List<sanctionNode> unsanctionedList) throws URISyntaxException, IOException, ClientProtocolException{
    	//http://yangtze.csc.ncsu.edu:9090/csc555checkin/services.jsp?query=getCheckinFeedbacks&checkinId=1
    	Map<String,String> sanctionMap = new HashMap<String,String>();
    	for(sanctionNode sn: unsanctionedList){	
    		
    		URI uri = new URIBuilder()
    	        .setScheme("http")
    	        .setHost("yangtze.csc.ncsu.edu:9090")
    	        .setPath("/csc555checkin-d/services.jsp")
    	        .setParameter("query", "getCheckinFeedbacks")
    	        .setParameter("checkinId", sn.CheckinId)
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
        	//System.out.println(content.toString());
        	ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(responseBody);
            Iterator<JsonNode> it = actualObj.elements();
            while (it.hasNext()) {
            	
        		JsonNode actor = it.next();
        		//String acName = actor.get("userName").toString();
        		
        		Iterator<JsonNode> it1 = actor.elements();
        		//System.out.println(actor.asText());
        		
        		while (it1.hasNext()){
        			JsonNode ac1 = it1.next();
        			//System.out.println("Has Next");
//        			System.out.println("Checkin: ");
//        			System.out.println(("userId id: "+ac1.get("userId").toString()));
//        			System.out.println("Checkin id: "+ac1.get("checkinId").toString());
//        			System.out.println("placeId id: "+ac1.get("placeId").toString());
//        			System.out.println("policyId id: "+ac1.get("policyId").toString());
        			String userId =ac1.get("userId").toString();
        			userId = (userId==null?userId:userId.replaceAll("^\"|\"$", ""));
        			//String checkinId =ac1.get("checkinId").toString();
        			String feedback =ac1.get("feedback").toString();
        			feedback = (feedback==null?feedback:feedback.replaceAll("^\"|\"$", ""));
//        			String policyId =ac1.get("policyId").toString();
        			//System.out.println("Checkin Id: " + sn.CheckinId + "UserId : "+userId + " Feedback : "+feedback);
        			if(feedback!=null && feedback.length()!=0){
        				
        				sanctionMap.put(sn.CheckinId +"-"+ userId,feedback);
        			}
        			if(feedback.equals("")){
        				//System.out.println("Feedback is ''. Can use null check!!!!!!");
        			}
        		}
    	}
		
	}
    /*
     * SanctionMap ready	
     */
    	for(String k:sanctionMap.keySet()){
    		String[] CheckInuser = k.split("-");
    		for(String s:CheckInuser){
    			//System.out.println("Key: " + s);
    		}
    		//System.out.println("Value: "+ sanctionMap.get(k));
    		String updateSanctionedSqlStatmt = "";
    		PreparedStatement pstmt = null;
    		updateSanctionedSqlStatmt = "UPDATE Checkins SET Sanction = '"+sanctionMap.get(k)+
    				"' WHERE Id = '"+CheckInuser[0]+"' AND Companion = '"+
    				CheckInuser[1]+"'";
    		try{
        		Connection conn = connect();
        	    pstmt  = conn.prepareStatement(updateSanctionedSqlStatmt);
        	    pstmt.executeUpdate();
        	    //System.out.println("Sanction Updated for Checkin: "+CheckInuser[0]+"' AND Companion = '"+
    				//CheckInuser[1]);
    		}
    		catch(Exception e){
				System.out.println(e.getMessage());
			}
    		
    		
    	}
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
