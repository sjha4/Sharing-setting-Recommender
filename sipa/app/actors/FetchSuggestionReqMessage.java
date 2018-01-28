package actors;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class FetchSuggestionReqMessage {
	private ObjectNode message;
	
	public FetchSuggestionReqMessage()
    {
    }
	
	 public FetchSuggestionReqMessage( ObjectNode message )
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
