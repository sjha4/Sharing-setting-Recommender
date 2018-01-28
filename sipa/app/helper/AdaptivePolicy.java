package helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class AdaptivePolicy {
	public static int getPolicy(String Place, String UserList, int alpha){
		
		int policy = 0;
		String adaptiveQuery = "";
		String[] companionSplit = UserList.split("\\|");
		String queryIn = "(";
    	for(String s:companionSplit){
    		//System.out.println(" Companions for Adaptive Policy in storeCheckin ---: "+ s);
    		queryIn += s + ","; 
    	}
    	if(queryIn.charAt(queryIn.length()-1)==','){
    		queryIn= queryIn.substring(0, queryIn.length()-1);
    		queryIn+=")";
    	}	
		adaptiveQuery = "Select * from Checkins where (Sanction = 'positive' or Sanction = 'negative') and Companion IN "+queryIn;
		if(UserList!=null && UserList.length()!=0){
			//System.out.println("Userlist not empty");
			ArrayList<Integer> positivePolicyList = new ArrayList<Integer>();
			ArrayList<Integer> negativePolicyList = new ArrayList<Integer>();
			Connection conn = connect();
			try {
				PreparedStatement pstmt = conn.prepareStatement(adaptiveQuery);
				ResultSet rs  = pstmt.executeQuery();
				while (rs.next()){
					String policyResult = rs.getString("Policy");
					policy = 0;
					if(policyResult!=null && !policyResult.equals("") && rs.getString("Sanction").equals("negative"))
					{
						policy = Integer.parseInt(rs.getString("Policy"));
						//System.out.println("Adding to negativePolicy List: " + policy);
						negativePolicyList.add(policy);
					}
					if(policyResult!=null && !policyResult.equals("")&& rs.getString("Sanction").equals("positive"))
					{
						policy = Integer.parseInt(rs.getString("Policy"));
						//System.out.println("Adding to positivePolicy List: " + policy);
						positivePolicyList.add(policy);
					}
					
				}
				
				policy = findAdaptivePolicy(positivePolicyList,negativePolicyList);
				//System.out.println("Adaptive:--------------------------------");
				//System.out.println("Suggesting aadaptive policy: "+ policy);
				/*
				 * 0 if not enough evidence or error
				 * 1-5 if suggestion available
				 * 10 if suggestion not available but need to change goal policy
				 */
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
		}
		return policy;
	}

	private static int findAdaptivePolicy(ArrayList<Integer> positivePolicyList,
			ArrayList<Integer> negativePolicyList) {
		int policy = 0;
		if(positivePolicyList==null && negativePolicyList==null){
			return policy;
			//No reviews found, continue with goal/commitment policy
		}
		if(positivePolicyList.size()>=negativePolicyList.size()){
			//return max of positivePolicy
			int max = maxPolicy(positivePolicyList);
			return max;
		}
		else if(positivePolicyList.size()<negativePolicyList.size())
		{
			//More negative reviews than positive.
			//Cannot make everyone happy, so choose the maximum of positive policy
			//if maximum of positive policy = maximum of negative policy, need to change.
			//if max(positive)!=max(negative)
			/*return max(positive)
			 * else 
			 * {
			 * 	if(max(positive)>1)////try going more private{
			 * return max(positive--);
			 * }
			 * else
			 * {
			 * 	try going public and stay public till need to go private in future iterations.
			 * }
			 */
			//System.out.println("in negative > positive");
			int maxPos = maxPolicy(positivePolicyList);
			int maxNeg = maxPolicy(negativePolicyList);
			//System.out.println("MaxPos: "+maxPos);
			//System.out.println("MaxNeg: "+maxNeg);
			if(maxPos==maxNeg || maxPos == 0){
				if(maxNeg>1){
					//System.out.println("maxNeg>1");
					return maxNeg-1;
				}
				else if(maxNeg==1){
					//System.out.println("maxNeg==1");
					return 4;
				}
			}
			else{
				//System.out.println("return MaxPos");
				return maxPos;
			}
			
			return policy;
		}
		
		
		
		return policy;
	}

	private static int maxPolicy(ArrayList<Integer> positivePolicyList) {
		if(positivePolicyList==null || positivePolicyList.size()==0) return 0;
		HashMap<Integer,Integer> hm = new HashMap<>();
		for(int i:positivePolicyList){
			if(hm.containsKey(i)){
				hm.put(i, hm.get(i)+1);
			}
			else
				hm.put(i,1);
		}
		int max = -999;
		int maxPol = 0;
		for(int k:hm.keySet()){
			if(max<hm.get(k)){
				max = hm.get(k);
				maxPol = k;
			}
		}
		if(max<0){
			return 0;
		}
		else return maxPol;
	}

	public static String policyRecommenderCommitment(String place, String companions,Boolean review){
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
    	if(companions!=null && !companions.equals("|")){
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
    					//System.out.println("Commitment:Setting group to Alone as exception:" + e.getMessage());
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
    	//System.out.println("Commitment: End of step 1 : Find Group");
    	//System.out.println("Start of step 2: Determine Commitment policy based on commitment");
    	//System.out.println("Place: "+place);
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
				    //System.out.println("No Commitment data"); 
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
    
    public static HashMap<String,String> policyRecommenderGoal(String place, String companions,Boolean review){
    	HashMap<String,String> res = new HashMap<>();
    	String groupSqlStatmt = "";
    	String policySqlStatmt = "";
    	String policy = "1"; //Default policy to share with no one
    	String priority = "Low"; //By default take low priority 
    	groupSqlStatmt = "Select * from Relationship where UserId = ?";
    	String group="Alone"; //Default group is alone
    	//System.out.println("Starting Steps of policy recomender...");
		/*
    	 * Find group
    	 */
    	//System.out.println("Step 1: Find Group");
    	if(companions!=null && !companions.equals("|")){
    		//System.out.println("In finding group");
    		String[] companionList = companions.split("|");
    		//companions!=null && !companions.equals("|")
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
    					//System.out.println("Setting group to Alone as exception:" + e.getMessage());
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
    	//System.out.println("End of step 1 : Find Group");
    	//System.out.println("Start of step 2: Determine policy");
    	//System.out.println("Place: "+place);
    	if(place.length()!=1)
    		place = place.substring(1, place.length()-1);
    	//System.out.println("Group:" + group);
    	if(place!=null){
    		policySqlStatmt = "Select Policy,Priority from Goals where Location = ? and Companion = ?";
    		try{
				Connection conn = connect();
				PreparedStatement pstmt  = conn.prepareStatement(policySqlStatmt);
				pstmt.setString(1,place);
				pstmt.setString(2,group);
				//System.out.println(pstmt.toString());
				ResultSet rs  = pstmt.executeQuery();
				if (!rs.isBeforeFirst() ) {    
				    //System.out.println("No Goals data"); 
				} 
				
				while (rs.next()){
					policy = rs.getString("Policy");
					priority = rs.getString("Priority");
					//System.out.println("Policy from inside sql statement: "+ policy);
					//System.out.println("Priority of Policy from Goal: " + priority);
				}
			}
			catch(Exception e){
				//Default policy : Share with no one
				policy = "1";
				priority = "Low";
				//System.out.println("Setting policy to 1 as exception in db: " + e.getMessage());
			}
    	}
    	/*
    	 * place==null so chose default policy of sharing with none , i.e: "1"
    	 */
    	else{
    		//System.out.println("Setting policy to 1 as place was null");
    		policy = "1";
    		priority = "Low";
    	}
    	
    	//System.out.println("Policy from recomender: " + policy);
    	res.put("policy", policy);
    	res.put("priority", priority);
    	return res;
    			
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
            //System.out.println(e.getMessage());
        }
		return conn; 
    }
	
	public static String getArgumentPolicy(String policy) {
    	String PolicyArgument = "Select Promote,Demote from Policy where Policy = ?";
    	String promote = null;
    	String demote = null;
    	String arg = "";
    	try {
			Connection conn = connect();
			PreparedStatement pstmt  = conn.prepareStatement(PolicyArgument);
			pstmt.setString(1,policy);
			ResultSet rs  = pstmt.executeQuery();
			if (!rs.isBeforeFirst() ) {    
			    //System.out.println("No Policy data"); 
			} 
			
			while (rs.next()){
				promote = rs.getString("Promote");
				demote = rs.getString("Demote");
//				//System.out.println("For Policy : "+ policy + "_____________________________________");
//				//System.out.println("Promote from inside Policy sql statement: "+ promote);
//				//System.out.println("Demote from inside Policy sql statement: "+ demote);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//System.out.println("Error in inserting checkins");
			e.printStackTrace();
			promote = null;
			demote = null;
		}
    	if(promote!=null){
    		arg+="promote("+promote+")";
    	}
    	if(promote==null && demote!=null){
    		arg+="demote("+demote+")";
    	}
    	if(promote!=null && demote!=null){
    		arg+="+demote("+demote+")";
    	}
    	
    	if(arg.equals("")) return null;
    	return arg;
		
	}

	public static void insertSuggestedCheckin(String checkin,String policy){
		String insertSql = "Insert into TaggedCheckin(CheckinId,Policy) values(?,?)";
		try {
			Connection conn = connect();
			PreparedStatement pstmt  = conn.prepareStatement(insertSql);
			pstmt.setString(1,checkin);
			pstmt.setString(2, policy);
    		pstmt.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error in inserting Suggestions to checkins");
			e.printStackTrace();
		}
	}

	public static void deleteTaggedCheckin(String checkinId){
		String deleteTaggedCheckinQuery = "Delete from TaggedCheckin where CheckinId = ?";
		try{
			Connection conn = connect();
			PreparedStatement pstmt  = conn.prepareStatement(deleteTaggedCheckinQuery);
			pstmt.setString(1,checkinId);
			pstmt.execute();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}
