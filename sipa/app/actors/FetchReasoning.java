package actors;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class FetchReasoning {
	private ObjectNode message;
	
	public FetchReasoning()
    {
    }
	
	 public FetchReasoning( ObjectNode message )
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
