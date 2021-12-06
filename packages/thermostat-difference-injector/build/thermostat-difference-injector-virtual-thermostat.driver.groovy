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
	
		// capability "Switch"
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
	updateThermostatSetpoint( device.currentValue('coolingSetpoint'), heatingSetpoint )
	return;
}

/* setCoolingSetpoint() is a command belonging to the capability "Thermostat".  */
void setCoolingSetpoint(Number coolingSetpoint) {
	log.debug "setCoolingSetpoint(${coolingSetpoint} ${getTemperatureScale()})"
	sendEvent(name: "coolingSetpoint", value: coolingSetpoint);
	updateThermostatSetpoint( coolingSetpoint, device.currentValue('heatingSetpoint') )
	return;
}

void updateThermostatSetpoint(Number coolingSetpoint, Number heatingSetpoint) {
	// note: Number is effectively a nullable type, I think.
	// we set the thermostatSetpoint attribute value to be the average of the non-null members of [coolingSetpoint, heatingSetpoint]
	sendEvent(
		name: "thermostatSetpoint", 
		value: [coolingSetpoint, heatingSetpoint].findAll({it != null}).with{sum()/size()}
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
}

/* auto() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def auto() {log.debug "auto"; return setThermostatMode("auto");}

/* off() is a command belonging to the capabilities "Switch", "Thermostat", and "Thermostat Mode"  */
def off() {log.debug "off"; return setThermostatMode("off");}

/* heat() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def heat() {log.debug "heat"; return setThermostatMode("heat");}

/* emergencyHeat() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def emergencyHeat() {log.debug "emergencyHeat"; return setThermostatMode("emergency heat");}

/* cool() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def cool() {log.debug "cool"; 	return setThermostatMode("cool");}




//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).

/**

 * If you are including this in an app, you must have the following mapping

 * declared: 

 * mappings {

 *      path("/runTheTestCode") { action: [GET:"runTheTestCode"] }

 * }

 * 

 * 

 * If you are including this in a driver, you must declare the following command:

 * command "runTheTestCode"

 * //Actually, it appears not to be necessary to declare "runTheTestCode" as a command,

 * // but still not a bad idea.

**/


////    
////    
////    switch(this.class.name){
////        case "com.hubitat.hub.executor.AppExecutor":
////            mappings {
////                path("/runTheTestCode") { action: [GET:"runTheTestCode"] }
////            }
////            break;
////        case "com.hubitat.hub.executor.DeviceExecutor": 
////            // do nothing
////            break;
////        default: break;
////    }
//
//    Somewhat miraculously, the above system of evaluating this.class.name to
//    figure out whether we are in an app or a driver actually does seem to work
//    (it is not too surpirsing that this works inside a method, but it is
//    surprising that this works at the main level of the script). However, it
//    would be better not to rely on this (undocumented?) behavior within the
//    hubitat that I have no control over, and instead accomplish the selective
//    insertion of the call to the mappings() function by means of a
//    preprocessing macro, over which I have complete control.
//

    //this component is a driver, so we do not need anything special here for debugging.

def runTheTestCode(){
    try{
        return respondFromTestCode(mainTestCode());
    } catch (e)
    {
        def debugMessage = ""
        debugMessage += "\n\n" + "================================================" + "\n";
        debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";
        debugMessage += "encountered an exception: \n${e}\n"
        
        try{
            def stackTraceItems = [];
            
            // in the case where e is a groovy.lang.GroovyRuntimeException, invoking e.getStackTrace() causes a java.lang.SecurityException 
            // (let's call it e1) to be 
            // thrown, saying that 
            // we are not allowed to invoke methods on class groovy.lang.GroovyRuntimeException.
            // The good news is that we can succesfully call e1.getStackTrace(), and the 
            // returned value will contain all the information that we had been hoping to extract from e.getStackTrace().
            // oops -- I made a bad assumption.  It turns out that e1.getStackTrace() does NOT contain the information that we are after.
            // e1.getStackTrace() has the file name and number of the place where e.getStackTrace(), but not of anything before that.
            //So, it looks like we are still out of luck in our attempt to get the stack trace of a groovy.lang.GroovyRuntimeException.

            def stackTrace;
            try{ stackTrace = e.getStackTrace();} catch(java.lang.SecurityException e1) {
                stackTrace = e1.getStackTrace();
            }

            for(item in stackTrace)
            {
                stackTraceItems << item;
            }


            def filteredStackTrace = stackTraceItems.findAll{ it['fileName']?.startsWith("user_") };
			
			//the last element in filteredStackTrace will always be a reference to the line within the runTheTestCode() function body, which
			// isn't too interesting, so we get rid of the last element.
			if(!filteredStackTrace.isEmpty()){
				filteredStackTrace = filteredStackTrace.init();  //The init() method returns all but the last element. (but throws an exception when the iterable is empty.)
			}
            
            // filteredStackTrace.each{debugMessage += it['fileName'] + " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n";   }
            filteredStackTrace.each{debugMessage += " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n";   }
                 
        } catch(ee){ 
            debugMessage += "encountered an exception while trying to investigate the stack trace: \n${ee}\n";
            // debugMessage += "ee.getProperties(): " + ee.getProperties() + "\n";
            // debugMessage += "ee.getProperties()['stackTrace']: " + ee.getProperties()['stackTrace'] + "\n";
            debugMessage += "ee.getStackTrace(): " + ee.getStackTrace() + "\n";
            
            
            // // java.lang.Throwable x;
            // // x = (java.lang.Throwable) ee;
            
            // //debugMessage += "x: \n${prettyPrint(x.getProperties())}\n";
            // debugMessage += "ee: \n" + ee.getProperties() + "\n";
            // // debugMessage += "ee: \n" + prettyPrint(["a","b","c"]) + "\n";
            // //debugMessage += "ee: \n${prettyPrint(ee.getProperties())}\n";
        }
        
        // debugMessage += "filtered stack trace: \n" + 
            // groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(filteredStackTrace)) + "\n";
    
        debugMessage += "\n"
        return respondFromTestCode(debugMessage);
    }
}

def respondFromTestCode(message){
    switch(this.class.name){
        case "com.hubitat.hub.executor.AppExecutor":
            return  render( contentType: "text/html", data: message, status: 200);
            break;
        case "com.hubitat.hub.executor.DeviceExecutor": 
            sendEvent( name: 'testEndpointResponse', value: message )
            return null;
            break;
        default: break;
    }
}
