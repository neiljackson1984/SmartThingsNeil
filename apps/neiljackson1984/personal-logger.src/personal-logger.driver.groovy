metadata {
	definition (
		name: "Personal Logger Child Device", 
		namespace: "neiljackson1984", 
		author: "Neil Jackson", 
        description: "a virtual dimmer to serve as an input for the user to create a log entry."
	) {
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform the platform that this device has commands     
        //attributes: (none)
        //commands:  (none)
        
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform the platform  that this device has attributes     
        //attributes: (none)
        //commands:  (none)

		// capability "Switch"
        // //attributes: enum switch ("on", "off")
        // //commands: on(), off()

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

    message += "this.class: " + this.class + "\n";
    message += "\n\n";
    


   return message;
}


//LIFECYCLE FUNCTION
def installed() {
	// off();
	setLevel(0);
}

//LIFECYCLE FUNCTION
def updated() {
	log.debug("updated");
	return null;
}

/** This is a virtual device, so parse will never be called */
//LIFECYCLE FUNCTION
def parse(description) {return null;}

// /* off() is a command belonging to the capabilities "Switch"  */
// def off() {log.debug "off"; sendEvent(name:"switch", value:"off"); return null;}

// /* on() is a command belonging to the capability "Switch".  */
// def on(){log.debug "on";  sendEvent(name:"switch", value:"on"); return null;}

/* setLevel() is a command belonging to the capability "SwitchLevel".  */
def setLevel(level, duration=null){
	log.debug("setLevel was called with " + ((String) level));
	sendEvent(name: "level", value: level); return null;
}

//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"