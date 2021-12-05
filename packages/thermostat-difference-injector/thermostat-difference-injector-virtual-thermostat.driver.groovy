metadata {
	definition (
		name: "Thermostat Difference Injector Virtual Thermostat", 
		namespace: "neiljackson1984", 
		author: "Neil Jackson", 
        description: "a virtual thermostat."
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
         
        capability "Temperature Measurement"
        //attributes: temperature
        //commands: (none)

		capability "Thermostat"
		//attributes:     
		//	coolingSetpoint - NUMBER
		//	heatingSetpoint - NUMBER
		//	schedule - JSON_OBJECT
		//	supportedThermostatFanModes - ENUM ["on", "circulate", "auto"]
		//	supportedThermostatModes - ENUM ["auto", "off", "heat", "emergency heat", "cool"]
		//	temperature - NUMBER
		//	thermostatFanMode - ENUM ["on", "circulate", "auto"]
		//	thermostatMode - ENUM ["auto", "off", "heat", "emergency heat", "cool"]
		//	thermostatOperatingState - ENUM ["heating", "pending cool", "pending heat", "vent economizer", "idle", "cooling", "fan only"]
		//	thermostatSetpoint - NUMBER

        //commands: 
		//   auto()
		//   cool()
		//   emergencyHeat()
		//   fanAuto()
		//   fanCirculate()
		//   fanOn()
		//   heat()
		//   off()
		//   setCoolingSetpoint(temperature)
		//       temperature required (NUMBER) - Cooling setpoint in degrees
		//   setHeatingSetpoint(temperature)
		//       temperature required (NUMBER) - Heating setpoint in degrees
		//   setSchedule(JSON_OBJECT)
		//       JSON_OBJECT (JSON_OBJECT) - JSON_OBJECT
		//   setThermostatFanMode(fanmode)
		//       fanmode required (ENUM) - Fan mode to set
		//   setThermostatMode(thermostatmode)
		//       thermostatmode required (ENUM) - Thermostat mode to set

        
		capability "Thermostat Mode"
        //attributes: thermostatMode, supportedThermostatModes
        //commands: auto, cool, emergencyHeat, heat, 'off', setThermostatMode
        
		capability "Thermostat Operating State"
        //attributes: thermostatOperatingState
        //commands: (none)
        
		capability "Thermostat Heating Setpoint"
        //attributes: heatingSetpoint
        //commands: setHeatingSetpoint
        
		capability "Thermostat Cooling Setpoint"
        //attributes: coolingSetpoint
        //commands: setCoolingSetpoint

		capability "ThermostatSetpoint"
		// Attributes:
		// 	thermostatSetpoint - NUMBER
		// Commands:
		// 	???


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

}

//LIFECYCLE FUNCTION
def updated() {
	log.debug("updated");
	return null;
}

/** This is a virtual device, so parse will never be called */
//LIFECYCLE FUNCTION
def parse(description) {
	log.debug("parse() was called -- very weird because this driver is expected only ever to be used for virtual devices.");
	return null;
}

// /* off() is a command belonging to the capabilities "Switch"  */
// def off() {log.debug "off"; sendEvent(name:"switch", value:"off"); return null;}

// /* on() is a command belonging to the capability "Switch".  */
// def on(){log.debug "on";  sendEvent(name:"switch", value:"on"); return null;}

/* setLevel() is a command belonging to the capability "SwitchLevel".  */
// def setLevel(level, duration=null){
// 	log.debug("setLevel was called with " + ((String) level));
// 	sendEvent(name: "level", value: level); return null;
// }

//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"