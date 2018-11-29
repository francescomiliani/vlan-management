package net.floodlightcontroller.unipi.vlanmanagement;

import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
/**
  * This simple class contains the basic info a host: its name & its port number ( corrisponding to the port number where the host 
  * is plugged into the switch )
  *
**/
public class Host {

	private String name = "H";
	private MacAddress addr;
	private OFPort port;

	//we add to the host name, its port number so we don't confuse the names with those assigned by mininet
	public Host( MacAddress addr, OFPort port ) {
		this.name = name + port.getPortNumber();
		this.addr = addr;
		this.port = port;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MacAddress getAddr() {
		return addr;
	}

	public void setAddr(MacAddress addr) {
		this.addr = addr;
	}

	public OFPort getPort() {
		return port;
	}

	public void setPort(OFPort port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "Host [name=" + name + ", addr=" + addr + ", port=" + port.getPortNumber() + "]";
	}
	
}