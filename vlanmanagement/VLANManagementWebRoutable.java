package net.floodlightcontroller.unipi.vlanmanagement;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import net.floodlightcontroller.restserver.RestletRoutable;
/**
 * This class creates the Restlet router and bind to the proper resources.
 * The below link string compose the url sent in the http request:
 * for instance: http://localhost:8080/vlanmanagement/addvlan/json 
**/
public class VLANManagementWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        
        // Add some REST resources
		
		//Adds a new VLAN
        router.attach( "/addvlan/json", AddVLANResource.class );
        //Deletes an exists VLAN
        router.attach( "/deletevlan/json", DeleteVLANResource.class );
        //Shows the list of VLANs with the belonging hosts
        router.attach( "/showvlans/json", ShowVLANsResource.class );
	    //Adds an existing host to an existing VLAN
        router.attach( "/addhosttovlan/json", AddHostToVLANResource.class );
        //Removes an existing host to an existing VLAN, by moving into the default VLAN 
	    router.attach( "/removehostfromvlan/json", RemoveHostFromVLANResource.class );
	    //Shows the list of Hosts belonging to a certain VLAN
	    router.attach( "/showvlanhosts/json", ShowVLANHostsResource.class );
	       	       	        
        return router;
    }
	 
	//Set the base path for the Topology
    @Override
	public String basePath() {
       return "/vlanmanagement";
	}
}
