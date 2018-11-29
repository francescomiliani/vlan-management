package net.floodlightcontroller.unipi.vlanmanagement;

import net.floodlightcontroller.core.module.IFloodlightService;
/**
 * This is the interface which declare the method that have to be implemented
 * These methods are called by the Restful resources
**/
public interface VlanManagementInterface extends IFloodlightService {
	
	public Object addVlan(String vlanName);
	
	public Object removeVlan(String vlanName);
	
	public Object getVlansInfo();
	
	public Object addHostToVlan(String hostName, String vlanName);
	
	public Object removeHostFromVlan(String hostName);
	
	public Object getVlanHosts(String vlanName);
}
