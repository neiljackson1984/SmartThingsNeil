
metadata {
	definition (name: "ddwrt-child-virtual-button", namespace: "neiljackson1984", author: "Neil Jackson") {
    
        //TAGGING CAPABILITIES: ('tagging' implies that these capabilities have no attributes, and have no commands)
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform SmartThings that this device has commands     
        // capability "Sensor"   //The "Sensor" capability is simply a marker to inform SmartThings that this device has attributes       

        //NON-TAGGING CAPABILITIES:   
		capability "Momentary"
		//attributes: (none)
        //commands:  push()

		capability "Switch"
		//attributes: switch
        //commands:  off()
		//commands: on()
		//We implement the switch capability so that Alexa can see this device and interact with it.
		//our switch will remain perpetually in the "on" state, so that
		// alexa will not report nonresponsiveness when we say "alexa, turn on router reboot"

	}


	
}

// this device is not associated with a physical zwave or zigbee device, and therefore I expect that the Hubitat platform
// will never call this device's parse() method.
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}



//LIFECYCLE FUNCTION
void installed() {
	log.debug("installed");
	initialize();
}


private void initialize(){
	log.debug("initialize");
	sendEvent(name: "switch", value: "on");
}




//command belonging to the capability "Momentary"
List<String> push(){
	log.debug("push")
	parent.childPush(device.deviceNetworkId)
	return []
}

//command belonging to the capability "Switch"
List<String> on(){
	log.debug("on")

	parent.childPush(device.deviceNetworkId)
  	//sendEvent(name: "switch", value: "on");
	sendEvent(name: "switch", value: "on", isStateChange: true);
	// sending an 'on' event makes alexa think that a response has occured.
	// actually, it does not appear to be necessary to send an event at all.

	//merely keeping the switch perpetually "on" (by sending an "on" event at some point and 
	//then forgetting about it ever after.  PROBABLY -- but it doesn't hurt to send the command just in case.
	return []
}

//command belonging to the capability "Switch"
List<String> off(){
	log.debug("off - this does nothing.")
	return []
}
