metadata {
	definition (
		name: "scratchpad1", 
		namespace: "neiljackson1984", 
		author: "Neil Jackson", 
        description: "scratchpad1 driver"
	) {
        attribute("testEndpointResponse", "string"); //this is for debugging.
    }

	preferences {
	}  
}

def mainTestCode(){
	// log.debug("mainTestCode() was called.")
    String message = ""

	message += "\n\n";

    Map<Short, Short> commandClassVersions = [
        0x20: 1,	// Basic
        0x25: 1,	// Switch Binary
        0x55: 1,	// Transport Service
        0x59: 1,	// AssociationGrpInfo
        0x5A: 1,	// DeviceResetLocally
        0x27: 1,	// Switch All
        0x5E: 2,	// ZwaveplusInfo
        0x6C: 1,	// Supervision
        0x70: 1,	// Configuration
        0x7A: 2,	// FirmwareUpdateMd
        0x72: 2,	// ManufacturerSpecific
        0x73: 1,	// Powerlevel
        0x85: 2,	// Association
        0x86: 1,	// Version (2)
        0x8E: 2,	// Multi Channel Association
        0x98: 1,	// Security S0
        0x9F: 1		// Security S2
    ]

    String description = "zw device: 06, command: 6C01, payload: 01 03 25 03 00 , isMulticast: false"
    def command = zwave.parse(
        description,
        commandClassVersions
    )

    hubitat.zwave.Command encapsulatedCommand = command.encapsulatedCommand(commandClassVersions)

    message += "command: ${command}" + "\n"
    message += "command.getProperties()['class']: ${command.getProperties()['class']}" + "\n"
    message += "encapsulatedCommand: ${encapsulatedCommand}" + "\n"
    message += "encapsulatedCommand.getProperties()['class']: ${encapsulatedCommand.getProperties()['class']}" + "\n"

   return message;
}


  


//LIFECYCLE FUNCTION
void installed() {
	log.debug("installed");
}

//LIFECYCLE FUNCTION
List<String>  updated() {
	log.debug("updated");
    return [];
}


//LIFECYCLE FUNCTION
List<Map> parse(description) {
    log.debug("parse was called with description ${description}.  This should not have happened, because I am a virtual device.");
    return [];
}


//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"