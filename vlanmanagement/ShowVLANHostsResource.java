package net.floodlightcontroller.unipi.vlanmanagement;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
/**
  *	This Class contains the handleRequest method which is invoked when the http request is received from the Restful Server
  * The method simply extracts the parameters: vlanid
  * Calls getVlanHosts method with the above extracted parameters as input
  * getVlanHosts is implemented in the VLAN MANAGER class
  * The HTTP Request type is POST because a set of parameters has to be transmitted
*/
public class ShowVLANHostsResource extends ServerResource {
	
	@Post //Json output data
	public Object handleRequest( String fmJson ) {//fmJon will be equal to {"vlanid":"v1"} for instance
	    
	    //Check if the payload is provided
	    if( fmJson == null ) {
	        return new String("No attributes");
	    }
		
		//Parse the JSON input	    
	    String response = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			JsonNode root = mapper.readTree(fmJson);
			
			//Get the field vlan
			String vlan = root.get("vlanid").asText();
			
			VlanManagementInterface vmi = (VlanManagementInterface) getContext().getAttributes().get(VlanManagementInterface.class.getCanonicalName());
			response = (String)vmi.getVlanHosts( vlan );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return response;
	
	}
}
