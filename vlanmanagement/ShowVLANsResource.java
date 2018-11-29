package net.floodlightcontroller.unipi.vlanmanagement;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Get;
/**
  *	This Class contains the handleRequest method which is invoked when the http request is received from the Restful Server
  * The method simply call the getVlansInfo method
  * getVlansInfo method has no parameters
  * getVlansInfo is implemented in the VLAN MANAGER class
  * The HTTP Request type is GET because no parameter are needed
*/
public class ShowVLANsResource extends ServerResource  {

  @Get("json") //Json output data
  public Object handleRequest() {			  
		VlanManagementInterface vmi = (VlanManagementInterface) getContext().getAttributes().get(VlanManagementInterface.class.getCanonicalName());
		return vmi.getVlansInfo();
  }
}
