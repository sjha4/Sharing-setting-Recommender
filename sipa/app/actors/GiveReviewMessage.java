package actors;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class GiveReviewMessage {
	private ObjectNode message;
	
	public GiveReviewMessage()
    {
    }
	
	 public GiveReviewMessage( ObjectNode message )
     {
          this.message = message;
     }
	 
	 public ObjectNode getMessage()
     {
          return message;
     }
	 
	 public void setMessage( ObjectNode message )
     {
          this.message = message;
     }
}
