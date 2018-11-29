# VLAN Management with Software Defined Networking

**Project Description**

The objective of this project is to implement an SDN controller that exposes a northbound interface to control the network behavior from an external application. The controller has to expose a RESTful northbound interface to allow an external application to create VLANs and assign the hosts of the SDN network to VLANs. An example application has to be written in order to test the implementation through an SDN network emulated using Mininet.
Assumptions
Before delving into the project development, the following assumptions have been made:
• We assume a static configuration of the network topology which is composed by a single switch, connected to the controller, and a certain number of hosts. This assumption follows from the use of the Mininet command:
```
sudo mn --topo single,6 --mac --switch ovsk --controller remote,ip=127.0.0.1,port=6653,protocols=OpenFlow13 --ipbase=10.0.0.0
```
---

However, our application is able to handle any number of hosts.
The controller discovers new hosts as soon as the switch receives a packet from such hosts; in that
case, the switch can’t find any matching rule in the Flow Table, therefore it sends a PACKET_IN (containing the address of the new host, among the other pieces of information) to the controller and the latter registers the new host in its internal data structures
At network startup, in Mininet, it is possible to perform a pingall action to get to know all the hosts immediately
 - Each host can belong only to a single VLAN at a time (at the beginning every host is associated to a default VLAN)
 - We assume that communication between different VLANs is not in the scope of our project, therefore we don’t have to deal with inter-VLAN communication issues (i.e. hosts in a given VLAN can communicate only with hosts from the same VLAN).

**Northbound Interface**

The focus of our project is to configure the controller in such a way that it can provide an API using the RESTful paradigm.
The API will provide the following functionalities:
 - Add a new VLAN with a name.
 - Delete an existing VLAN by name.
 - Show the list of all VLANs with their belonging hosts.
 - Assign an existing host to a certain VLAN.
 - Remove an existing host from its current VLAN (reassigning it to the default VLAN).
 - Show the list of all the hosts belonging to a certain VLAN

Example Application
In order to test our API, we want to develop an external application providing a Graphical User Interface (GUI) offering an easy way for the network administrator to configure VLANs.
Control Layer
The controller maintains a list of the existing VLANs and, for each one of them, a list of hosts belonging to that VLAN. VLANs are represented to the network manager (the user of our application) using symbolic names.
When a new host joins the network, it is added to the default VLAN, named “Default”.
When a host is moved from a VLAN to another by the network manager, the controller updates its internal data structures accordingly. Moreover, it deletes the rules on the switch related to that host. The switch can obtain updated rules from the controller when needed, i.e. when a packet from/to that host has to be forwarded.
When a host is removed from its VLAN, it is re-assigned to the default VLAN, the controller data structures are updated accordingly and the related rules on the switch are deleted.
The hard timeout for the rules will be set to infinity, since we expect that a rule is valid until the network manager decides to change a host-VLAN assignment.
The idle timeout for the rules, instead, will be set to a bounded value (10s in our case), so that unused rules are removed from the Flow Table, thus reducing its size.
---

**Testing**

We propose the following test-plan for the application:
 - Launch the controller, the GUI and Mininet with the command
   sudo mn --topo single,6 --mac --switch ovsk --controller remote,ip=127.0.0.1,port=6653,protocols=OpenFlow13 --ipbase=10.0.0.0
 - The controller immediately gets to know about all the hosts due to exchange of LLDP packets; however, it is useful to execute a pingall on Mininet both to facilitate this objective and to verify that everything is fine after startup
 - On the GUI, press the View VLANs button, to verify the initial configuration
 - Press the Add VLAN button with argument “v1”, then again with argument “v2” and then the View
VLANs button to verify the creation of the two VLANs
 - Press the Add VLAN button with argument “v1” to verify that an error message is returned, since it
is not allowed to create more VLANs with the same name
 - Press the Add VLAN button a couple of times, with no arguments, to verify that new VLANs with
default names in the form “VLAN_i” are created
 - Using the Add Host to VLAN button, assign host “h1” and “h2” to “v1” and host “h3” and “h4” to
“v2”; use the View VLANs button to verify the assignments
 - Press the View VLAN Hosts button with parameter “v1” to verify the assignments limited to the
VLAN “v1” only
 - Press the View VLAN Hosts button with parameter “v3” to verify that an error message is returned,
since this VLAN does not exist
 - On Mininet, let h1 ping first h2, to verify that intra-VLAN communication is successful, and then h3,
to verify that inter-VLAN communication is precluded
In order to have a complete picture of the communication capabilities inside the network,
it is possible to use, again, a pingall command
In order to test the broadcast communication capabilities inside the network, first
configure all the hosts to respond to ICMP_ECHO_REQUESTS addressed to the broadcast address (10.0.0.255 in this case) with the command
```
h1 echo “0” > /proc/sys/net/ipv4/icmp_echo_ignore_broadcasts
```
(repeated for all the host), then
```
h1 ping 10.0.0.255
```
 - On the GUI, using the Add Host to VLAN button, assign host “h1” to “v3” to verify that an error message is returned, since VLAN “v3” does not exist
 - On the GUI, using the Add Host to VLAN button, assign host “h7” to “v2” to verify that an error message is returned, since host “h7” does not exist
 - On the GUI, using the Add Host to VLAN button, assign host “h1” to “v2”; use the View VLANs button to verify the assignments
 - On Mininet, repeat the pinging procedure to verify that h1 cannot reach h2 anymore, but it can well reach h3 and h4, i.e. the rules have been updated successfully
 - On the GUI, press the Remove Host from VLAN button with parameter “h7” and verify that an error message is returned, since host “h7” does not exist
 - On the GUI, press the Remove Host from VLAN button with parameter “h2”, to remove h2 from “v1” and assign it to the default VLAN, then use the View VLANs button to verify the assignments
 - On the GUI, press the Remove VLAN button with parameter “v3” and verify that an error message is returned, since this VLAN does not exist
 - On the GUI, press the Remove VLAN button with parameter “Default” and verify that an error message is returned, since the default VLAN cannot be deleted
 - On the GUI, press the Remove VLAN button with parameter “v1” and use the View VLANs button to verify that “v1” was correctly removed
 - On the GUI, press the Remove VLAN button with parameter “v2” and use the View VLANs button to verify that “v2” was correctly removed and all of its hosts have been moved to the default VLAN