metadata {
	definition (
		name: "Personal Logger Child Device", 
		namespace: "neiljackson1984", 
		author: "Neil Jackson", 
        description: "a virtual dimmer to serve as an input for the user to create a log entry."
		// importUrl: "https://raw.githubusercontent.com/neiljackson1984/SmartThingsNeil/master/devicetypes/stelpro/stelpro-ki-zigbee-thermostat.src/stelpro-ki-zigbee-thermostat.groovy"
	) {
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform the platform that this device has commands     
        //attributes: (none)
        //commands:  (none)
        
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform the platform  that this device has attributes     
        //attributes: (none)
        //commands:  (none)

		capability "Switch"
        //attributes: enum switch ("on", "off")
        //commands: on(), off()

		capability "SwitchLevel"
		//Attributes:
		//	level - NUMBER
		//Commands:
		//	setLevel(level, duration)
		//	level required (NUMBER) - Level to set (0 to 100)
		//	duration optional (NUMBER) - Transition duration in seconds

    }

	preferences {
		
	} 
}

def mainTestCode(){
	def message = ""

	message += "\n\n";

	// message += device.events(max:5).collect{
	// 	event -> 
	// 	event.dump()
	// }.join("\n"*3);

    message += "this: " + this.dump() + "\n";
    message += "this.class: " + this.class + "\n";

    message += "\n\n";
    
    message += "this.class.getDeclaredFields(): " + "\n";
    this.class.getDeclaredFields().each{message += it.toString() + "\n";	}
    
    message += "\n\n";
    message += "this.class.getMethods(): " + "\n";
    this.class.getMethods().each{	message += it.toString() + "\n";}
	message += "\n\n";
    message += "this.class: " + this.class + "\n";
    message += "this.class.name: " + this.class.name + "\n";
    message += "this is an AppExecutor: " + (this.class.name == "com.hubitat.hub.executor.AppExecutor").toString() + "\n";
    message += "this is a DeviceExecutor: " + (this.class.name == "com.hubitat.hub.executor.DeviceExecutor").toString() + "\n";
    // message += "this is an AppExecutor: " + (this instanceof com.hubitat.hub.executor.AppExecutor).toString() + "\n";

    message += "\n\n";
    
    // message += "this.class.class.getDeclaredFields(): " + "\n";
    // this.class.class.getDeclaredFields().each{message += it.toString() + "\n";	}
    
    // message += "\n\n";
    // message += "this.class.class.getMethods(): " + "\n";
    // this.class.class.getMethods().each{	message += it.toString() + "\n";}

   return message;
}


//LIFECYCLE FUNCTION
def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	sendEvent(name: "supportedThermostatFanModes", value: ["auto"], displayed: false)
	sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
	sendEvent(name: "thermostatFanMode", value: "auto");
}

//LIFECYCLE FUNCTION
def updated() {
	log.debug("updated");
	installed()
	def returnValue = [];
	// requests += parameterSetting();
	
	if(settings.physicalKeypadLock == "Yes" || settings.physicalKeypadLock == "No"){
		returnValue +=  zigbee.writeAttribute(
			zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			0x01,
			DataType.ENUM8, 
			["Yes":1, "No":0][settings.physicalKeypadLock]
		) + poll(); 	//Write Lock Mode
	} 

	log.debug("returnValue: " + groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(  returnValue )));
	
	// sendEvent(name: "blabbedyTest", value: "AHOY THERE");
	
	//with requests being an array of strings, the following two statements produce equvialent effects:
	//  sendHubCommand(response(returnValue));
	//  return returnValue;

	// but the following statement has no effect:
	// return(response(returnValue)); //this is the statement that was in the original device handler code published by Stelpro.

	// in other words, the platform expects that the updated() function will return an array of strings (commands to be sent to the device).
	// This is a different type of return alue than is expected from the parse function.  
	// The parse function is expected to return an array whose elements 
	// are, by default, interpreted as "events" (equivalent to sendEvent()),
	// but can be HubAction objects, in order to cause the hub radio to send a command out to the devices.
	// The updated() function is expected to return the same type of thing that the command functions return, namely, an array of strings 
	// that are interpreted as zwave or zigbee commands to be transmitted by the hub's radios.  I am not sure if there is any type of object that
	// can be inlcuded in the array returned from a command funciton to cause an event to be thrown.

	// This whole business of commanding radio transmissions and throwing events by means of return values from functions
	// is unnecessarily confusing.  It would make much more sens to have every command to be sent by the hub's radio 
	// be achieved by calling one function (sendHubCommand()), and every event to be thrown be achieved by
	// calling a different function (sendEvent())

	// I suspect (and hope) that there is no functional difference between, on the one hand, passing events and radio command requests as return values from the parse (and other) functions, and
	// , on the other hand, calling sendHubCommand() and sendEvent() and returning null from the functions. 

	return returnValue;
}

/** This is a virtual device, so parse will never be called */
//LIFECYCLE FUNCTION
def parse(description) {return null;}

/* off() is a command belonging to the capabilities "Switch"  */
def off() {log.debug "off"; sendEvent(name:"switch", value:"off"); return null;}

/* on() is a command belonging to the capability "Switch".  */
def on(){log.debug "on";  sendEvent(name:"switch", value:"on"); return null;}

/* setLevel() is a command belonging to the capability "SwitchLevel".  */
def setLevel(level, duration=null){sendEvent(name: "level", value: level); return null;}

//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"