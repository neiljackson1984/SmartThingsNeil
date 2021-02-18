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

		capability "Switch"
		// attributes: switch
        // commands: 'on', 'off'


		capability "Configuration"
        
        command "getAssociationReport"
    	command "getVersionReport"
	    command "getCommandClassReport"
	    command "getParameterReport", [[name:"parameterNumber",type:"NUMBER", description:"Parameter Number (omit for a complete listing of parameters that have been set)", constraints:["NUMBER"]]]
	    command "setParameter",[[name:"parameterNumber",type:"NUMBER", description:"Parameter Number", constraints:["NUMBER"]],[name:"size",type:"NUMBER", description:"Parameter Size", constraints:["NUMBER"]],[name:"value",type:"NUMBER", description:"Parameter Value", constraints:["NUMBER"]]]
	

    }

	preferences {
		
	}  
}

def mainTestCode(){
	def message = ""

	message += "\n\n";

    message += "this.class: " + this.class + "\n";
    
    
	x = createEvent(name: "switch", value:  "on");
	message += "x.class: " + x.class + "\n";
	message += "x.class: " + x.getProperties()['class'] + "\n";
	message += "x: " + x.toString() + "\n";
	



	z0 = new hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport(value:0)
	message += "z0: " + z0.toString() + "\n";

	z1 = new hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport(value:1)
	message += "z1: " + z1.toString() + "\n";
	message += "z1.format(): " + z1.format() + "\n";

	y = response(z1)
	message += "y: " + y.toString() + "\n";
	message += "y.class: " + y.getProperties()['class'] + "\n";
	message += "y.getProperties(): " + y.getProperties() + "\n";
	message += "y.action.class: " + y.action.getProperties()['class'] + "\n";

	message += "zwave.parse(y.action): " + zwave.parse(y.action) + "\n";

	message += "\n\n";

   return message;
}


//LIFECYCLE FUNCTION
void installed() {
	// off();
	log.debug("installed");
}

//LIFECYCLE FUNCTION
List<String>  updated() {
	log.debug("updated");
}

//LIFECYCLE FUNCTION
List<Map> parse(description) {return null;}



/* off() is a command belonging to the capability "Switch"  */
List<String> off() {log.debug "off"; sendEvent(name:"switch", value:"off"); return null;}

/* on() is a command belonging to the capability "Switch".  */
List<String> on(){log.debug "on";  sendEvent(name:"switch", value:"on"); return null;}

/* configure() is a command belonging to the capability "Confioguration"  */
List<String> configure(){
	log.debug "configure";

}


//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"