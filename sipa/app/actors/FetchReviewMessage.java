package actors;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class FetchReviewMessage {
	private ObjectNode message;
	
	public FetchReviewMessage()
    {
    }
	
	 public FetchReviewMessage( ObjectNode message )
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
