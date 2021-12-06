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
	sendEvent(name: "supportedThermostatFanModes", value: [
		"on", 
		"circulate", 
		"auto"
	]);

	sendEvent(name: "supportedThermostatModes", value: [
		"auto", 
		"cool", 
		"emergencyHeat", 
		"heat", 
		"off"
	]);
	sendEvent(name: "coolingSetpoint",           value: 0       );
	sendEvent(name: "heatingSetpoint",           value: 0       );
	sendEvent(name: "thermostatSetpoint",        value: 0       );
	sendEvent(name: "schedule",                  value: {}      );
	sendEvent(name: "temperature",               value: 0       );
	sendEvent(name: "thermostatFanMode",         value: "auto"  );
	sendEvent(name: "thermostatMode",            value: "off"   );
	sendEvent(name: "thermostatOperatingState",  value: "idle"  );
	
	state.lastNonOffThermostatMode = "auto"
	initialize();
	return;
}

//LIFECYCLE FUNCTION
def updated() {
	log.debug("updated");
	initialize();

	return null;
}

def initialize() {
	log.debug("initialize() was called.")
	// sendEvent(name: "coolingSetpoint", value: );
	// sendEvent(name: "heatingSetpoint", value: );
	// sendEvent(name: "schedule", value: {});
	// sendEvent(name: "temperature", value: );
	// sendEvent(name: "thermostatFanMode", value: );
	// sendEvent(name: "thermostatMode", value: );
	// sendEvent(name: "thermostatFanMode", value: );
	// sendEvent(name: "thermostatOperatingState", value: );
	// sendEvent(name: "thermostatSetpoint", value: );
	return;
}

/** This is a virtual device, so parse will never be called */
//LIFECYCLE FUNCTION
def parse(description) {
	log.debug("parse() was called -- very weird because this driver is expected only ever to be used for virtual devices.");
	return;
}



//  COMMAND methods: I am bit ambivalent about whether to put any logic here in
// the device handler or rather to to have all the logic in the parent app; I
// believe it is possible for the parent app to subscribe to commands (not just
// to events) -- so it might be possible to have almost all logic in the parent
// app. One argumentt in favor of having logic here is that, eventually, I want
// the parent app to be able to, optionally, use a pre-existing
// not-necessarily-virtual thermostat as the master thermostat, so I want this
// virtual thermostat to behave as much like a pre-existing thermostat as
// possible as far as the parent app is concerned. In fact, I wonder whether (or
// at least, to what extent) it would be possible to write a universal virtual
// device driver, designed to be entirely controlled, specified, etc. from a
// parent app.  The idea is that the very same universal virtual device driver
// would be usable for just about any app that needs to create any kind of
// virtual child device.

/* setHeatingSetpoint() is a command belonging to the capabilities "Thermostat" and "Thermostat Heating Setpoint".  */
void setHeatingSetpoint(Number heatingSetpoint) {
	log.debug "setHeatingSetpoint(${heatingSetpoint} ${getTemperatureScale()})"
	sendEvent(name: "heatingSetpoint", value: heatingSetpoint);
	updateThermostatSetpoint( 
		device.currentState('coolingSetpoint').getNumberValue(), 
		heatingSetpoint, // device.currentState('heatingSetpoint').getNumberValue(),
		device.currentState('thermostatMode').getStringValue()
	);
	return;
}

/* setCoolingSetpoint() is a command belonging to the capability "Thermostat".  */
void setCoolingSetpoint(Number coolingSetpoint) {
	log.debug "setCoolingSetpoint(${coolingSetpoint} ${getTemperatureScale()})"
	sendEvent(name: "coolingSetpoint", value: coolingSetpoint);
	updateThermostatSetpoint( 
		coolingSetpoint, // device.currentState('coolingSetpoint').getNumberValue(), 
		device.currentState('heatingSetpoint').getNumberValue(),
		device.currentState('thermostatMode').getStringValue()
	);
	return;
}

void updateThermostatSetpoint(Number coolingSetpoint, Number heatingSetpoint, String thermostatMode) {
	// note: Number is effectively a nullable type, I think.
	// we set the thermostatSetpoint attribute value to be the average of the non-null members of [coolingSetpoint, heatingSetpoint]
	
	
	// Number coolingSetpoint = device.currentState('coolingSetpoint').getNumberValue()
	// Number heatingSetpoint = device.currentState('heatingSetpoint').getNumberValue()
	// String thermostatMode = device.currentState('thermostatMode').getStringValue()
	//
	// I suspect that we cannot rely on device.currentState(...) to reflect a state that 
	// was created by our having sent an event in this same running of the device handler, so
	// we will have to pass values explicitly.

	sendEvent(
		name: "thermostatSetpoint", 
		value: [
			( ["auto", "cool", "off"].contains(thermostatMode) ? coolingSetpoint : null ),
			( ["auto", "heat", "off", "emergencyHeat"].contains(thermostatMode) ? heatingSetpoint : null )
		].findAll({it != null}).with{sum()/size()}
	)
	return;
}


/* setThermostatFanMode() is a command belonging to the capability "Thermostat".  */
def setThermostatFanMode(String thermostatFanMode) {
	sendEvent(name: "thermostatFanMode", value: thermostatFanMode);
}

/* fanOn() is a command belonging to the capability "Thermostat".  */
def fanOn() {log.debug "fanOn"; return setThermostatFanMode("on");}

/* fanAuto() is a command belonging to the capability "Thermostat".  */
def fanAuto() {log.debug "fanAuto"; return setThermostatFanMode("auto");}

/* fanCirculate() is a command belonging to the capability "Thermostat".  */
def fanCirculate() {log.debug "fanCirculate"; return setThermostatFanMode("circulate");}



/* setThermostatMode() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def setThermostatMode(String thermostatMode) {
	sendEvent(name:"thermostatMode", value: thermostatMode);
	sendEvent(name:"switch", value: ( thermostatMode == "off" ? "off" : "on"));
	if (thermostatMode != "off") {
		state.lastNonOffThermostatMode = thermostatMode
	}
	updateThermostatSetpoint( 
		device.currentState('coolingSetpoint').getNumberValue(), 
		device.currentState('heatingSetpoint').getNumberValue(),
		thermostatMode
	);
}

/* auto() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def auto() {log.debug "auto"; return setThermostatMode("auto");}

/* off() is a command belonging to the capabilities "Switch", "Thermostat", and "Thermostat Mode"  */
def off() {log.debug "off"; return setThermostatMode("off");}

/* on() is a command belonging to the capabilities "Switch" */
def on() {log.debug "on"; return setThermostatMode(state.lastNonOffThermostatMode);}


/* heat() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def heat() {log.debug "heat"; return setThermostatMode("heat");}

/* emergencyHeat() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def emergencyHeat() {log.debug "emergencyHeat"; return setThermostatMode("emergency heat");}

/* cool() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def cool() {log.debug "cool"; 	return setThermostatMode("cool");}




//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"