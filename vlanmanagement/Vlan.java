package net.floodlightcontroller.unipi.vlanmanagement;

import java.util.*;
import org.projectfloodlight.openflow.types.MacAddress;
/**
 * This class describes a vlan and basically contains the list of belonging hosts
**/
public class Vlan {
	
	private Set<Host> hostList;
	
	private static final String DEFAULT_NAME = "VLAN_";
	private String name = DEFAULT_NAME;
	private static int id_counter = -1;
	
	private int id;
	
	public Vlan( String namePar ) {
		this.id_counter++;
		if( namePar.equalsIgnoreCase( "" ) ) //If no name are specified by the user, an incremental VLAN name is assigned
			namePar = DEFAULT_NAME;
		if( namePar.equalsIgnoreCase( DEFAULT_NAME ))
			namePar += id_counter;//Example VLAN_1, VLAN_2, ...
		
		this.name = namePar;
		this.id = this.id_counter;
		this.hostList = Collections.synchronizedSet( new HashSet<>());
	}
	
	//Constructor: simply recall the above constructor
	public Vlan() {
		this( DEFAULT_NAME );
	}
	
	public Set<Host> getHostList() {
		return hostList;
	}

	public void setHostList(Set<Host> hostList) {
		this.hostList = new HashSet<>( hostList );
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static int getId_counter() {
		return id_counter;
	}

	public static void setId_counter(int id_counter) {
		Vlan.id_counter = id_counter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getSize() {
		return hostList.size();
	}
	
	public Host getHostByName(String hostName) {
		for( Host h: hostList )
			if( h.getName().equalsIgnoreCase( hostName ) ) return h;
		//Not found
		return null;
	}
	
	public Host getHostByMacAddress( MacAddress mac ) {
		for( Host h: hostList )
			if( h.getAddr().equals( mac ) ) return h;
		//Not found
		return null;
	}
	
	public boolean addHost( Host newhost ) {
		return hostList.add(newhost);
	}
	
	public boolean removeHost( Host host ) {
		return hostList.remove(host);
	}
	
	//This method returns a string like this: ["H1","H2"]
	public String getHostListAsString( ) {
		String s = "[";
		Iterator<Host> hostIt = hostList.iterator();
		Host h = null;
		while( hostIt.hasNext() ) {
			h = hostIt.next();
			s += "\"" + h.getName() + "\"";
			if( hostIt.hasNext() ) s += ", ";
		}
		return s += "]";
	}
	
	public boolean isPresent(String hostName) {
		for( Host h: hostList )
			if( h.getName().equalsIgnoreCase(hostName) )
				return true;
		return false;
	}
	
	@Override
	public String toString() {
		String ret = "VLAN Name: " + name +"\n\tID: "+ id + "\n\thost list:  " ;
		ret += getHostListAsString();
		return ret;
	}
}
