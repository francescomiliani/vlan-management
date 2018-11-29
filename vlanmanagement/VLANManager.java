package net.floodlightcontroller.unipi.vlanmanagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.util.HexString;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.util.FlowModUtils;

public class VLANManager implements IFloodlightModule, IOFMessageListener, VlanManagementInterface{
	
	private static short IDLE_TIMEOUT = 10;		
	private static short HARD_TIMEOUT = 0;
	
	protected IFloodlightProviderService floodlightProvider; 
	protected IRestApiService restApiService;
	
	public static Set<Vlan> vlanList; //main data structure containing all the VLANs and the associated hosts
	//Support data structure to efficiently find the VLAN of a host given its MAC address (Key: MacAddress; Value: Vlan)
	public static Map<MacAddress, Vlan> macVlanTable; 

	public static IOFSwitch mainSwitch = null;//Reference to the switch. We have surely just one switch
	public static Vlan defaultVlan = null;////Reference to the defaultVlan because is used often.
	
	
	@Override
	public String getName(){
		return VLANManager.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
	    Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(VlanManagementInterface.class);
	    return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
	    Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	    m.put(VlanManagementInterface.class, this);
	    return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
	  Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	  l.add(IFloodlightProviderService.class);
	  l.add(IRestApiService.class);
	  return l;
	}

	// This method creates the support data structures, and add the default Vlan to the vlan list
	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);				
		// Retrieve a pointer to the rest api service
		restApiService = context.getServiceImpl(IRestApiService.class);
		
		//INITIALIZE THE DATA STRUCTURES FOR OUR APPLICATION
		macVlanTable = new ConcurrentHashMap<>();
		vlanList = Collections.synchronizedSet( new HashSet<Vlan>());			
		defaultVlan = new Vlan( "Default" );
		vlanList.add( defaultVlan );
		
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		
		// Add as REST interface the one defined in the LoadBalancerWebRoutable class
		restApiService.addRestletRoutable( new VLANManagementWebRoutable() );
	}
	
	
/***************************************               RECEIVE METHOD               *******************************************************/
	/**
	 * This method receive the packet from the switch as an Ethernet one.
	 * Checks the following things:
	 * 		1) If source mac address forwarded by the switch is relative to a new host, we register the host into the default VLAN
	 *			otherwise is already seen;
	 *		2) Checks for a broadcast packet
	 *			a. BROADCAST: the packet has to be sent to the same vlan of the source host
	 *			b. UNICAST: 
					1. DESTINATION HOST IS KNOWNN: VLAN FILTER has to be applied: by dropping packet destinated to a different vlan respect to the source one
	 *				2. DESTINATION HOST IS UNKNOWN: broadcast into the same vlan of the source has to be made.
	 **/
	@Override
	public net.floodlightcontroller.core.IListener.Command receive( IOFSwitch sw, OFMessage msg, FloodlightContext cntx ) {
			
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		if( mainSwitch == null )	mainSwitch = sw;//For the first time
		// Cast to Packet-In
		OFPacketIn pi = (OFPacketIn) msg;
		// Retrieve host port from message
		OFPort inPort = pi.getMatch().get(MatchField.IN_PORT);
		
		// Print the source MAC address
		Long sourceMACHash = Ethernet.toLong(eth.getSourceMACAddress().getBytes());
		Long destMACHash = Ethernet.toLong(eth.getDestinationMACAddress().getBytes());
		System.out.printf(	"Received packet from host on port %s:\n" + 
							"Source MAC address = {%s},\n" + 
							"Dest MAC address = {%s}.\n", inPort.getPortNumber(), HexString.toHexString(sourceMACHash), HexString.toHexString(destMACHash));
		
		//if source mac address forwarded by the switch is relative to a new host, we register the host into the default VLAN
		if( macVlanTable.containsKey( eth.getSourceMACAddress() ) == false ) {	
			macVlanTable.put( eth.getSourceMACAddress(), defaultVlan );
			defaultVlan.addHost( new Host( eth.getSourceMACAddress(), inPort ) );
			System.out.printf("The source host is new --> inserted in the default VLAN.\n" );
		}
		else {
			//this packet is received because a rule expired in the switch
			System.out.printf("The source host was already registered.\n");	
		}
		
		if( eth.isBroadcast() || eth.isMulticast() ) {
			
			System.out.printf("Handling a broadcast message...\n");
			writeRule( eth, pi, true );
			System.out.printf("Packet flooded within the VLAN of the source host\n");
			
		} else {//UNICAST
			System.out.printf("Handling a unicast message...\n");
			//filter to check the source and dest belong to the same vlan
			if( applyVLANFilter( eth ) ) {//both hosts belong to the same VLAN
				System.out.printf("VLAN filter: source and destination belong to the same VLAN\n");
				writeRule( eth, pi, false );
				System.out.printf("Packet sent to the destination\n");
			
			} else {
				//In this case there is no known host with the required destination address inside the VLAN of the source host.
				//In this case the switch floods the packet within the VLAN of the source host (this is the default behavior).
				
				System.out.printf("VLAN filter: no known host with the required destination address exists inside the VLAN of the source host\n");
				writeRule( eth, pi, true );
				System.out.printf("Packet flooded within the VLAN of the source host \n");
				
			}
		}
		
		// Interrupt the chain of modules
		// Because we do not want that the next module processes the packet, otherwise we command the switch to forward it, independently of our rules
		// Command.CONTINUE is used when we want that the packet is processed by the following module
		return Command.STOP;

	}

	//VLAN FILTER: checks if the source vlan & destination vlan are the same.
	private boolean applyVLANFilter( Ethernet eth ) {
		MacAddress destMac = eth.getDestinationMACAddress();
		MacAddress srcMac = eth.getSourceMACAddress();
		
		Vlan destVlan = macVlanTable.get( destMac );		
		Vlan srcVlan = macVlanTable.get( srcMac );
		//Printf for checking
		System.out.printf("Source VLAN: ");
		if( srcVlan == null ) System.out.printf("UNKNOWN, ");
		else System.out.printf("%s, ", srcVlan.getName());
		
		System.out.printf("Dest VLAN: ");
		if( destVlan == null ) System.out.printf("UNKNOWN\n");
		else System.out.printf("%s\n", destVlan.getName());
		
		if(  destVlan == null  || srcVlan == null ) return false;
		if( destVlan.getId() != srcVlan.getId() )
			return false;
		else //Hosts belonging to the same VLAN
			return true;
		
	}
	
	
/***************************************************** RULES HANDLING **********************************************************/
	/**
	 * This method write a packet of PACKET_OUT type which contains the rules that the switch has to apply to the corresponding src-dest addresses
	 * These rules are added to the switch flow table.
	 * The kind of rule that are prepared is simply a setting of output port on the switch: namely, forward the packet on port number 4.
	 * There are 2 possiblity:
	 * 		a. UNICAST: just one rules is prepared
	 * 		b. BROADCAST: a rule for each belonging host to the vlan has to be prepared
	 * Furthermore, it is sent the action to perform on this packet because otherwise the switch adds the rule, but does not perform any action on the
	 * current packet.
	**/
	private void writeRule( Ethernet eth, OFPacketIn pi, boolean isBroadcast ) {
		MacAddress srcMac = eth.getSourceMACAddress();
		Vlan srcVlan = macVlanTable.get( srcMac );
		MacAddress destMac = eth.getDestinationMACAddress();
		
		//Create a flow table modification message to add a rule
		OFFlowAdd.Builder fmb = mainSwitch.getOFFactory().buildFlowAdd();
        fmb.setIdleTimeout( IDLE_TIMEOUT );
        fmb.setHardTimeout( HARD_TIMEOUT );
        fmb.setBufferId( pi.getBufferId() );
        fmb.setOutPort( OFPort.ANY );
        fmb.setCookie( U64.of(0) );
        fmb.setPriority( FlowModUtils.PRIORITY_MAX );

        //Create the match structure  
        Match.Builder mb = mainSwitch.getOFFactory().buildMatch();
        mb.setExact(MatchField.ETH_DST, destMac )
        .setExact(MatchField.ETH_SRC, srcMac );
        
        //Creating list of actions associated to this match
        OFActions actions = mainSwitch.getOFFactory().actions();
        // Create the actions: --> Forward to the destination port
        ArrayList<OFAction> actionList = new ArrayList<OFAction>();
        
        if( isBroadcast ) {
        	// Since a broadcast command limited to the current VLAN is not available, 
        	// we build a list of actions in which we perform a send operation of the packet to a port corresponding to a given host, 
        	// for each host belonging to the same vlan as the source
        	for(Host h : srcVlan.getHostList()) {
        		if(srcMac.equals( h.getAddr()) == false ){ // do not flood on the same port which this packet came from
	        		OFActionOutput output = actions.buildOutput()
	    	        	    .setMaxLen(0xFFffFFff)
	    	        	    .setPort( OFPort.of(h.getPort().getPortNumber()) )
	    	        	    .build();
	    	        actionList.add(output);
        		}
        	}
	        
        }
        else {
        	//Just set the output port corresponding to the destination host
        	//The destination host is in the same vlan as the source host, we have to retrieve the destination port
        	OFPort destPort = srcVlan.getHostByMacAddress(destMac).getPort();
	        OFActionOutput output = actions.buildOutput()
	        	    .setMaxLen(0xFFffFFff)
	        	    .setPort( OFPort.of(destPort.getPortNumber()) )
	        	    .build();
	        actionList.add(output);
        }
        
        fmb.setActions( actionList );
        fmb.setMatch( mb.build() );
        
        //Send Flow-Mod to the switch
        mainSwitch.write( fmb.build() );
		
        // The switch received the rule and updated its Flow Table, but it doesn't perform any other action. 
        // We have to explicitly tell the switch that we also want it to forward the packet to the destination,
        // otherwise it doesn't do anything else
        
 		// Create the Packet-Out and set basic data for it (buffer id and in port)
 		OFPacketOut.Builder pob = mainSwitch.getOFFactory().buildPacketOut();
 		pob.setBufferId( pi.getBufferId() );
 		pob.setInPort( OFPort.ANY );
 		
 		// Assign the action
 		pob.setActions(actionList);
 		
		// Packet might be buffered in the switch or encapsulated in Packet-In 
		// If the packet is encapsulated in Packet-In send it back
		if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
			// Packet-In buffer-id is none, the packet is encapsulated -> send it back
		    byte[] packetData = pi.getData();
		    pob.setData(packetData);
		            
		}
		
		//Send a PACKET_OUT to the switch
		mainSwitch.write( pob.build() );
	}

	/**
	 * This method is invoked whenever a host is moved from a VLAN to another: 
	 * any rule involving that host as source or as destination has to be removed
	 * Therefore, a packet that commands to the switch to delete a specific rule has to be sent
	 * Furthermore, rules related to the broadcast address have to be deleted too
	**/
	private void deleteRules( MacAddress mac ) {

		//Destination  
        Match.Builder mb = mainSwitch.getOFFactory().buildMatch();
        mb.setExact(MatchField.ETH_DST, mac);
			
	    OFFlowAdd flowAdd = mainSwitch.getOFFactory().buildFlowAdd().setMatch(mb.build()).setTableId(TableId.of(1)).build();
		
	    //Send Flow-Mod to the switch
		mainSwitch.write(FlowModUtils.toFlowDelete( flowAdd ) );
		
		//Source
        mb = mainSwitch.getOFFactory().buildMatch();
        mb.setExact( MatchField.ETH_SRC, mac );
			
	    flowAdd = mainSwitch.getOFFactory().buildFlowAdd().setMatch(mb.build()).setTableId(TableId.of(1)).build();
		
	    //Send Flow-Mod to the switch
		mainSwitch.write( FlowModUtils.toFlowDelete(flowAdd) );
		
		//Rules related to the broadcast address have to be deleted too
		mb = mainSwitch.getOFFactory().buildMatch();
        mb.setExact( MatchField.ETH_DST, MacAddress.BROADCAST );
			
	    flowAdd = mainSwitch.getOFFactory().buildFlowAdd().setMatch(mb.build()).setTableId(TableId.of(1)).build();
		
	    //Send Flow-Mod to the switch
		mainSwitch.write(FlowModUtils.toFlowDelete( flowAdd ) );
		
	  }
	
	
/********************************************* INTERFACE IMPLEMENTATION ***********************************************************/
	//NOTE: each method of the interface returns a json string response.
	
	/**
	 * This method returns a list of vlan with the belonging host.
	**/
	@Override
	public Object getVlansInfo() {
		String jsonResponse = "{  ";

		Iterator<Vlan> itVlan = vlanList.iterator();
		while( itVlan.hasNext() ) //Surely contains the DEFAULT VLAN
		{
			Vlan v = itVlan.next();
			jsonResponse += "\"" + v.getName() + "\":";
			jsonResponse += v.getHostListAsString();
			if( itVlan.hasNext() == true )
				jsonResponse += ",\n    ";
		}
		jsonResponse +=" }";
		
		return jsonResponse;
	}
	
	/**
	 * This method returns a list of host belonging to a certain vlan.
	**/
	public Object getVlanHosts( String vlanName ) {		  
		String jsonResponse = "";
		  
		Vlan v = getVlanByName( vlanName );
		if (v == null)
			return jsonResponse = "{\"response\": \"ERROR: VLAN " + vlanName + " does not exist.\"}";
		  
		jsonResponse += "{\"" + vlanName + "\": ";
		jsonResponse += v.getHostListAsString();
		jsonResponse += "}";
		return jsonResponse;
		
	}

	/**
	 * This method remove a existing vlan, after checking its existence.
	 * Each host is moved from the vlan to be delete to the default vlan, and the vlan list and macVlanTable are updated
	 * A empty vlan is simply deleted.
	 * The corresponding rule to the host have to be removed (that is, rules with host address either as source or destination )
	 * It does NOT create rules, if needed delete existing ones and update the data structures on the
	 * controller side (rule creation is done in the receive method!)
	**/
	public Object addVlan( String vlanName ) {
		String jsonResponse = "";
		// Check if the vlan is already present
		if( getVlanByName( vlanName ) != null )
			return jsonResponse = "{\"response\": \"ERROR: VLAN " + vlanName + " already exists.\"}";
		
		// The vlan is not present yet!
		Vlan v = new Vlan( vlanName );
		vlanList.add( v );
		jsonResponse = "{\"response\": \"OK - VLAN " + v.getName() + " inserted successfully.\"}";
		
		return jsonResponse;
	}
	
	/**
	 * This method remove a existing vlan, after checking its existence.
	 * Each host is moved from the vlan to be delete to the default vlan, and the vlan list and macVlanTable are updated
	 * A empty vlan is simply deleted.
	 * The corresponding rule to the host have to be removed (that is, rules with host address either as source or destination )
	 * It does NOT create rules, if needed delete existing ones and update the data structures on the
	 * controller side (rule creation is done in the receive method!)
	**/
	public Object removeVlan( String vlanName ) {
		String jsonResponse = "";
		Vlan v = getVlanByName( vlanName );
		
		if( v == null )
			return jsonResponse = "{\"response\": \"ERROR: VLAN " + vlanName + " does not exist.\"}";
		else if ( v.getName().equalsIgnoreCase( defaultVlan.getName() ) )
			return jsonResponse = "{\"response\": \"ERROR: Impossible to delete Default VLAN\"}";		

		if( !v.getHostList().isEmpty() ) {
			//For each host in the specified vlan with moved the belonging hosts into the default vlan
			//and the delete the respective rules
			for( Host h : v.getHostList() ) {
				deleteRules( h.getAddr() );
				
				defaultVlan.addHost( h );
				
				//Update also the macVlanTable
				macVlanTable.put( h.getAddr(), defaultVlan );
			}
			
			v.getHostList().clear();//Clear the host list
		}
		
		//The VLAN can be deleted: remove the VLAN from the VLAN list
		vlanList.remove( v );
		jsonResponse = "{\"response\": \"OK - VLAN " + vlanName + " deleted successfully.\"}";
		return jsonResponse;
	}
	
	/**
	 * This method add a host to a existing vlan, after checking on the existence of the host and the vlan
	 * The host is moved from the default vlan into the destination vlan, and the vlan list and macVlanTable are updated
	 * The corresponding rule to the host have to be removed (that is, rules with host address either as source or destination )
	 * It does NOT create rules, if needed delete existing ones and update the data structures on the
	 * controller side (rule creation is done in the receive method!)
	**/
	public Object addHostToVlan( String hostName, String vlanName ) {
		String jsonResponse = "";
		Vlan currentVlan = null;
		Vlan destVlan = null;
		Host h = null;
		
		// Check if the vlan exists
		if( (destVlan = getVlanByName( vlanName)) == null )
			return jsonResponse += "{\"response\": \"ERROR: VLAN " + vlanName + " does not exist.\"}";
		
		// Check if the host exists and belongs to a certain vlan
		for( Vlan v: vlanList ) {
			if(v.isPresent( hostName ) ) {
				currentVlan = v;
				break;
			}
		}
		if( currentVlan == null ) //The host does not exist (host in worst case should be assigned to default vlan)
			return jsonResponse += "{\"response\": \"ERROR: host "+ hostName + " does not exist.\"}";
		
		//Host exists
		//Check whether the current vlan is different from the destination vlan, if they are the same we do nothing
		if( destVlan != currentVlan ) {
			h = currentVlan.getHostByName( hostName );
			destVlan.addHost( h );
			currentVlan.removeHost( h );
			//put is fine because if the host is already present in a different vlan, the operation simply update the value (putting it in a different vlan)
			//Make a simple update
			macVlanTable.put( h.getAddr(), destVlan );
		
			//We must to delete all the rules that see this mac address as destination address or as source
			deleteRules( h.getAddr() );	//delete existing rules and send the flow mod update to the switch
		
		}
		
		return jsonResponse += "{\"response\": \"OK - Host "+ hostName + " inserted in VLAN " + vlanName + " successfully.\"}";
	}
	
	/**
	 * This method simply invokes the method addHostVlan to move the host from its current VLAN to the default VLAN, as from the specifications
	**/
	public Object removeHostFromVlan( String hostName ) {
		return addHostToVlan( hostName, defaultVlan.getName() );
	}
	
/******************************************* UTILITIES METHODS *********************************************************************************
	/**
	 * This method returns the corrispoding vlan given a the name.
	**/
	private Vlan getVlanByName( String vlanName ) {
		for( Vlan v : VLANManager.vlanList )
			if( v.getName().equalsIgnoreCase( vlanName ) )
				return v;//Vlan found
		
		//Vlan not found
		return null;
	}
	
}