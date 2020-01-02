metadata {
	definition (
		name: "Stelpro Ki ZigBee Thermostat", 
		namespace: "stelpro", 
		author: "Stelpro", 
		ocfDeviceType: "oic.d.thermostat",
		importUrl: "https://raw.githubusercontent.com/neiljackson1984/SmartThingsNeil/master/devicetypes/stelpro/stelpro-ki-zigbee-thermostat.src/stelpro-ki-zigbee-thermostat.groovy"
	) {
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform the platform that this device has commands     
        //attributes: (none)
        //commands:  (none)
        
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform the platform  that this device has attributes     
        //attributes: (none)
        //commands:  (none)
        
        capability "Configuration"
        //attributes: (none)
        //commands: configure()
        
		capability "Polling" 
        //deprecated
        //attributes: (none)
        //commands: poll()

		capability "Health Check"
		// Attributes
		//     checkInterval - NUMBER
		// 
		// Commands
		//     ping()

		capability "Switch"
        //attributes: enum switch ("on", "off")
        //commands: on(), off()
         
		capability "Refresh"
        //attributes: (none)
        //commands: refresh()
		
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

		capability "ThermostatSetpoint"
		// Attributes:
		// 	thermostatSetpoint - NUMBER
		// Commands:
		// 	???



		capability "SwitchLevel"
		//Attributes:
		//	level - NUMBER
		//Commands:
		//	setLevel(level, duration)
		//	level required (NUMBER) - Level to set (0 to 100)
		//	duration optional (NUMBER) - Transition duration in seconds

		// we implement the switchLevel capability as a hack to work around the problem of the hubitat not creating
		// true Thermostat devices in Alexa.
		// We will use the the level as a sort-of proxy for temperature setpoint, so we can
		// meaningfully say something like "Alexa, set the thermostat to 76"
		// Alexa will believe that she is setting the level of a dimmer switch, but we will use the command to set 
		// the setpoint of the thermostat to 76 degrees.

		
		command "setOutdoorTemperature",  ["number"]
		
		//I am overriding the setThermostatMode to accept one additional enum value: "eco", in addition to the standard thermostat modes.
		// hopefully, this sort of overriding of a command associated with a standard capability won't cause problems.
		//actually, it looks like, if there are multiple commands having the smae name, but different signatures (i.e. differnet second arrguments in the command() function),
		//then the command appears twice multiple times on the device page, once for each version of the second argument --not the desired result.
		//therefore, instead of declaring a command with the name "setThermostatMode, I will declare a command with a different name "setCustomThermostatMode"
		command(
			 "setCustomThermostatMode", 
			 [
				[
					 "name":"Thermostat mode*",
					 "description":"Thermostat mode to set",
					 "type":"ENUM",
					 "constraints":["off","heat","eco"]
				]
			 ]
		)


		//I am overriding the thermostatMode attribute to accomodate one additional enum value: "eco", in addition to the standard thermostat modes.
		// hopefully, this sort of overriding of an attribute associated with a standard capability won't cause problems.
		//to be consistent with the above comment in relation to the "setCustomThermostatMode" command, I will 
		// not declare an attribute with the name "thermostatMode", but will instead declare an attribute with the name "customThermostatMode".
		//we will regard the custom thermostat modes "heat" and "eco" as being submodes (and the only submodes) of thermosat mode "heat"
		attribute("customThermostatMode", "ENUM", ["off","heat","eco"] );

		command "eco"
        command "runTheTestCode"

		// command(
		// 	 "foobar", 
		// 	 [
		// 		[
		// 			 "name":"foo*",
		// 			 "description":"this is the description of foo",
		// 			 "type":"ENUM",
		// 			 "constraints":["auto","off","heat","emergency heat","cool", "eco"]
		// 		],
		// 		[
		// 			 "name":"bar*",
		// 			 "description":"this is the description of bar",
		// 			 "type":"ENUM",
		// 			 "constraints":["fuschia","turquise","hot","boiling","sapped"]
		// 		]
		// 	 ]
		// );



		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "STZB402+", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "ST218", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
	}

	preferences {
		input("physicalKeypadLock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
	}
}




def runTheTestCode(){
    try{
        return mainTestCode();
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
        
        log.debug(debugMessage);
        debugMessage += "\n"
        return respondFromTestCode(debugMessage);
    }
}




def mainTestCode(){
    def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);
    
   //do some test stuff here.
//    message = "\n\n" + myDateFormat.format(myDate) + ": " + "this is the message that will be returned from the curl call (to the device instance).\n"
	message = ""
	message += "\n\n";
	if(false){
		message += "zigbee.class.getDeclaredFields(): " + "\n";
		zigbee.class.getDeclaredFields().each{
			// message += it.dump() + "\n";
			message += it.toString() + "\n";
		}
		
		message += "\n\n";
		message += "zigbee.class.getMethods(): " + "\n";
		zigbee.class.getMethods().each{
			// message += it.dump() + "\n";
			message += it.toString() + "\n";

		}

		message += "\n\n";
	}

	if(false){
		message += "zigbee.getCluster().getDeclaredFields(): " + "\n";
		zigbee.getCluster().getDeclaredFields().each{
			// message += it.dump() + "\n";
			message += it.toString() + "\n";
		}
		
		message += "\n\n";
		message += "zigbee.getCluster().getMethods(): " + "\n";
		zigbee.getCluster().getMethods().each{
			// message += it.dump() + "\n";
			message += it.toString() + "\n";

		}

		message += "\n\n";
	}

	if(false){
		message += "hubitat.class.getDeclaredFields(): " + "\n";
		hubitat.class.getDeclaredFields().each{
			// message += it.dump() + "\n";
			message += it.toString() + "\n";
		}
		
		message += "\n\n";
		message += "hubitat.class.getMethods(): " + "\n";
		hubitat.class.getMethods().each{
			// message += it.dump() + "\n";
			message += it.toString() + "\n";

		}

		message += "\n\n";
	}



	if(false){
		message += "DataType: " + DataType.dump() + "\n";
		


		// message += "DataType.getProperties(): " + "\n";
		// DataType.getProperties().each{message += it.toString() + "\n";	}
		// message += "\n\n";

		message += "DataType.getMethods(): " + "\n";
		DataType.getMethods().each{ message += it.toString() + "\n"; }
		message += "\n\n";

		// message += "DataType.each: " + "\n";
		// DataType().each{ message += it.toString() + "\n"; }
		// message += "\n\n";

		// message += "DataType.getFields(): " + "\n";
		// DataType.getFields().each{message += it.toString() + "\n";	}
		// message += "\n\n";

		message += "DataType.getDeclaredFields(): " + "\n";
		DataType.getDeclaredFields().findAll{it.type == int}.each{
			// message += it.name + ": " + DataType[it.name] + " (" + "0x" + Integer.toHexString(DataType[it.name]) + ")" + "\n";	
			message += "0x" + Integer.toHexString(DataType[it.name]) + ": " + it.name + (DataType.isDiscrete(DataType[it.name]) ? " (discrete)" : "") + "\n";	
		}
		message += "\n\n";
	}


	if(false){
		message += "this: " + this.dump() + "\n";
		message += "this.class: " + this.class + "\n";

		message += "\n\n";
		
		message += "this.class.getDeclaredFields(): " + "\n";
		this.class.getDeclaredFields().each{message += it.toString() + "\n";	}
		
		message += "\n\n";
		message += "this.class.getMethods(): " + "\n";
		this.class.getMethods().each{	message += it.toString() + "\n";}

		message += "\n\n";
		message += "device.class.getMethods(): " + "\n";
		device.class.getMethods().each{	message += it.toString() + "\n";}

	}

	for(clusterInt in [0x0000, 0x0003, 0x0004, 0x0201, 0x0204, 0x0402]){
		cluster = zigbee.clusterLookup(clusterInt)
		message += "0x" + Integer.toHexString(clusterInt) + ": " + cluster?.dump() + "\n";
		// message += clusterInt + ": " + cluster.clusterEnum + "\n";
		// message += clusterInt + ": " + cluster.clusterEnum.class + "\n";
		// message += clusterInt + ": " + zigbee[cluster.clusterEnum].dump() + "\n";
		// message += (  zigbee.clusterLookup(clusterInt) as Integer) + "\n"; //throws org.codehaus.groovy.runtime.typehandling.GroovyCastException
	}

	message += "\n\n";
	message += "zigbee.temperatureConfig(1,10): " + "\n";
	zigbee.temperatureConfig(1,10).each{message += "\t" + it + "\n"};

	message += "\n\n";
	message += "zigbee.configureReporting(...): " + "\n";
	zigbee.configureReporting(
		/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
		/*attributeId*/      0x0000,                     
			//Attribute ID 0x0000 = local temperature
		/*dataType*/         DataType.INT16, 
		/*minReportTime*/    10,
			//minimum number of seconds between reports						
		/*maxReportTime*/    60,                        
			//maximum number of seconds between reports
		/*reportableChange*/ 50 
			// Amount of change needed to trigger a report. 
			// Required for analog data types. Discrete data types should always provide null for this value.	  
	).each{message += "\t" + it + "\n"};





	message += "\n\n";
	message += "zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000): " + "\n";
	zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000).each{message += "\t" + it + "\n"};

	message += "\n\n";
	message += "capabilities: " + capabilities.dump() + "\n\n";
	message += "device.capabilities: " + device.capabilities.dump() + "\n\n";
	message += "getCapabilities(): " + getCapabilities().dump() + "\n\n";
	message += "device.getCapabilities(): " + "\n";
	device.getCapabilities().each{
		message += "\t" + it.name + "\n";
		message += "\t"*2 + "commands: " + "\n";
		it.commands.each{
			message += "\t"*3 + it.name + "; " + it.arguments?.dump() + "; " + it.parameters?.dump() +  "\n";
			// message += "\t"*3 + it.class.getDeclaredFields().dump() + "\n";
		}
	};

	message += "\n\n";

	message += "\t"*1 + "device.getSupportedCommands(): " + "\n";
	device.getSupportedCommands().each{
		message += "\t"*2 + it.name + ":" + "\n";
		
		//it.arguments appears to be an array of strings, each being the name of a type (e.g. "ENUM", "NUMBER", etc.)
		if(false){
			message += "\t"*3 + "arguments:" + "\n";
			it.arguments.each{
				message += "\t"*4 + (it ? it.dump() : "false") + "\n";
			}
		}

		//it.parameters appears to be an array of maps, each being similar to this": {"name":"Thermostat mode*","description":"Thermostat mode to set","type":"ENUM","constraints":["auto","off","heat","emergency heat","cool","eco"]}
		message += "\t"*3 + "parameters:" + "\n";
		it.parameters.each{
			// message += "\t"*4 + (it ? it.dump() : "false") + "\n";
			message += "\t"*4 + (it == null ? "null" : groovy.json.JsonOutput.toJson(it) + "\t" + it.dump()) + "\n";
		}
	}


	message += "\n\n";

	// message += "thing: " + zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:2) + "\n";
	// message += "response: " + response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:2)).dump() + "\n";

	message += "e_CLD_THERMOSTAT_ATTR_ID_LOCAL_TEMPERATURE: " + e_CLD_THERMOSTAT_ATTR_ID_LOCAL_TEMPERATURE + "\n";

	// message += "([1,2] + [3,4]): " + ([1,2] + [3,4]).dump() + "\n";

   log.debug(message);
   return respondFromTestCode(message);
}

def respondFromTestCode(message){
	sendEvent( name: 'testEndpointResponse', value: message )
	// return message;
	return null;
}


def getSupportedThermostatModes() {
	modes()
}

def getThermostatSetpointRange() {
	if (getTemperatureScale() == "C") {
		[5, 30]
	}
	else {
		[41, 86]
	}
}

def getHeatingSetpointRange() {
	thermostatSetpointRange
}

def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	sendEvent(name: "supportedThermostatFanModes", value: ["auto"], displayed: false)
	sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
	sendEvent(name: "thermostatFanMode", value: "auto");
}

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






def parse(description) {
	def debugMessage = "";

	debugMessage += "Parse description $description" + "\n";

	if (description?.startsWith("read attr -")) {
		def descMap = zigbee.parseDescriptionAsMap(description);
		debugMessage +=  "zigbee.parseDescriptionAsMap(description): " +  groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(zigbee.parseDescriptionAsMap(description))) + "\n"*2;
		
		try{
			debugMessage +=  "zigbee.convertHexToInt(descMap.value): " + zigbee.convertHexToInt(descMap.value) + "\n";
		} catch (e){
			debugMessage +=  "zigbee.convertHexToInt(descMap.value): " + ee + "\n";
		}

		// def descMapWithIntegerValues = descMap.collect{ key, value -> [key, zigbee.convertHexToInt(value)]  };
		// debugMessage += "descMapWithIntegerValues: " +  groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(descMapWithIntegerValues)) + "\n"*2;
		
		debugMessage +=  "zigbee.getEvent(description): " +  groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(zigbee.getEvent(description))) + "\n"*2;
		
		if(descMap.clusterInt == zigbee.THERMOSTAT_CLUSTER){
			if (descMap.attrInt == e_CLD_THERMOSTAT_ATTR_ID_LOCAL_TEMPERATURE) {
				def rawValue = zigbee.convertHexToInt(descMap.value);
				//todo computer rawvalue as a signed twoscomplement integer (confirm that this is compaible with zigbee standard.)
				
				if (rawValue == 32765) {		//0x7FFD
					sendEvent(name:"temperatureAlarm", value: "freeze");
				}
				else if (rawValue == 32767) {	//0x7FFF
					sendEvent(name:"temperatureAlarm", value: "heat");
				}
				else if (rawValue == 32768) {	//0x8000
					sendEvent(name:"temperatureAlarm", value: "cleared");
				}
				// else if (zigbee.convertHexToInt(descMap.value) > 0x8000) {
				// 	map.value = -(Math.round(2*(655.36 - map.value))/2)
				// }
				else {
					//todo interpret rawvalue as a signed twoscomplement integer
					sendEvent(name:"temperature", value: convertTemperatureFromNativeUnitsToHumanReadableUnits(rawValue), unit: getTemperatureScale());
				}
			}
			else if (descMap.attrInt == e_CLD_THERMOSTAT_ATTR_ID_OCCUPIED_HEATING_SETPOINT) {
				debugMessage +=   "HEATING SETPOINT" + "\n";
				map.name = "heatingSetpoint"
				map.value = convertTemperatureFromNativeUnitsToHumanReadableUnits(zigbee.convertHexToInt(descMap.value))
				map.data = [heatingSetpointRange: heatingSetpointRange]
				if (zigbee.convertHexToInt(descMap.value) == 0x8000) {		//0x8000
					map.name = "temperatureAlarm"
					map.value = "cleared"
					map.data = []
				}
			}
			else if (descMap.attrInt == e_CLD_THERMOSTAT_ATTR_ID_SYSTEM_MODE) {
				if (descMap.value.size() == 8) {
					debugMessage +=   "MODE" + "\n";
					map.name = "thermostatMode"
					map.value = modeMap[zigbee.convertHexToInt(descMap.value)]
					map.data = [supportedThermostatModes: supportedThermostatModes]
				}
				else if (descMap.value.size() == 10) {
					debugMessage +=  "MODE & SETPOINT MODE" + "\n";
					def twoModesAttributes = descMap.value[0..-9]
					map.name = "thermostatMode"
					map.value = modeMap[zigbee.convertHexToInt(twoModesAttributes)]
					map.data = [supportedThermostatModes: supportedThermostatModes]
				}
			}
			else if (descMap.attrInt == e_CLD_THERMOSTAT_ATTR_ID_PI_HEATING_DEMAND) {
				debugMessage +=   "HEAT DEMAND" + "\n";
				map.name = "thermostatOperatingState"
				if (descMap.value < "10") {
					map.value = "idle"
				}
				else {
					map.value = "heating"
				}
			}
			else if (descMap.attrInt == 0x401c) {
				debugMessage +=   "SETPOINT MODE" + "\n";
				debugMessage +=   "descMap.value $descMap.value" + "\n";
				map.name = "thermostatMode"
				map.value = modeMap[zigbee.convertHexToInt(descMap.value)]
				map.data = [supportedThermostatModes: supportedThermostatModes]
			}
		}
	}

	// def result = null
	// if (map) {
	// 	result = createEvent(map)
	// }
	// debugMessage +=   "Parse returned $map" + "\n";
	// log.debug(debugMessage);
	// return result;
	return null;
}


def getModeMap() { [
	0x00:"off",
	0x04:"heat",
	0x05:"eco"
]}


/**
  * PING is used by Device-Watch in attempt to reach the Device
**/
/* ping() is a command belonging to the capability "Health Check".  */
def ping() {
	return (
		zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000)
	);
}

/* poll() is a command belonging to the capability "Polling".  */
def poll() {
	log.debug("poll");
	return (
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000)	//Read Local Temperature
			+ zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0008)	//Read PI Heating State
			+ zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0012)	//Read Heat Setpoint
			+ zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C)	//Read System Mode
			+ zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, ["mfgCode": "0x1185"])	//Read Manufacturer Specific Setpoint Mode
			+ zigbee.readAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0000)	//Read Temperature Display Mode
			+ zigbee.readAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0001)		//Read Keypad Lockout
	);
}



def convertTemperatureFromNativeUnitsToHumanReadableUnits(Number temperatureInNativeUnits) {
	Number returnValue;
	log.debug("convertTemperatureFromNativeUnitsToHumanReadableUnits(" +  groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(temperatureInNativeUnits)) +  ") was called.")
	def temperatureInDegreesCelsius = temperatureInNativeUnits / 100;
	if (getTemperatureScale() == "C") {
		returnValue = temperatureInDegreesCelsius;
	}
	else {
		returnValue = celsiusToFahrenheit(temperatureInDegreesCelsius);
	}

	
	returnValue = Math.round(returnValue * 10**temperatureReportingPrecision)/10**temperatureReportingPrecision;
	//to do: improve the above rounding strategy

	return returnValue;
}

def convertTemperatureFromHumanReadableUnitsToNativeUnits(Number temperatureInHumanReadableUnits) {
	log.debug("convertTemperatureFromHumanReadableUnitsToNativeUnits(" +  groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(temperatureInHumanReadableUnits)) +  ") was called.")
	def temperatureInDegreesCelsius = (
		getTemperatureScale() == "C" 
		? 
		temperatureInHumanReadableUnits 
		: 
		fahrenheitToCelsius(temperatureInHumanReadableUnits)
	)
	
	return temperatureInDegreesCelsius * 100;
}




/* refresh() is a command belonging to the capability "Refresh".  */
def refresh() {
	return poll();
}

/* setHeatingSetpoint() is a command belonging to the capabilities "Thermostat" and "Thermostat Heating Setpoint".  */
def setHeatingSetpoint(Number temperatureInHumanReadableUnits) {
	log.debug "setHeatingSetpoint(${temperatureInHumanReadableUnits} ${getTemperatureScale()})"
	
	return (
		zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x12, DataType.INT16, Math.round(convertTemperatureFromHumanReadableUnitsToNativeUnits(temperatureInHumanReadableUnits)).toInteger())
		+ zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x12)	//Read Heat Setpoint
		+ zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x08)	//Read PI Heat demand
		+ poll()
	);
}

/* setCoolingSetpoint() is a command belonging to the capability "Thermostat".  */
def setCoolingSetpoint(degrees) {
	log.trace "${device.displayName} does not support cool setpoint"
}

/* setOutdoorTemperature() is a custom command */
def setOutdoorTemperature(Number temperatureInHumanReadableUnits) {
	Integer tempToSend
	
	def celsius = (getTemperatureScale() == "C") ? temperatureInHumanReadableUnits : (fahrenheitToCelsius(temperatureInHumanReadableUnits) as Float).round(2)

	if (celsius < 0) {
		tempToSend = (celsius*100) + 65536
	}
	else {
		tempToSend = (celsius*100)
	}
    return zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x4001, DataType.INT16, tempToSend, ["mfgCode": "0x1185"]);
}

def modes() {
	["heat", "eco", "off"]
}


/* setThermostatFanMode() is a command belonging to the capability "Thermostat".  */
def setThermostatFanMode(fanMode) {
	if(fanMode == "auto"){
		log.debug("complying with your request to set the fan mode to auto (because auto is the only supported fan mode).");
	} else {
		log.debug("could not set fan mode to ${fanMode}.  Only auto fan mode is supported.");
	}
	return [];
}

/* fanOn() is a command belonging to the capability "Thermostat".  */
def fanOn() {log.debug "fanOn"; return setThermostatFanMode("on");}

/* fanAuto() is a command belonging to the capability "Thermostat".  */
def fanAuto() {log.debug "fanAuto"; return setThermostatFanMode("auto");}

/* fanCirculate() is a command belonging to the capability "Thermostat".  */
def fanCirculate() {log.debug "fanCirculate"; return setThermostatFanMode("circulate");}



/* setCustomThermostatMode() is a custom command.  
this is our authoritative mode-setting command, which all other mode-setting commands ultimately invoke.
*/
def setCustomThermostatMode(String value) {
	log.debug "setThermostatMode({$value})"
	def modeNumber;
	Integer setpointModeNumber;

	if (value == "heat") {
		modeNumber = 04
		setpointModeNumber = 04
	}
	else if (value == "eco") {
		modeNumber = 04
		setpointModeNumber = 05
	}
	else {
		modeNumber = 00
		setpointModeNumber = 00
	}

	// delayBetween([
	// 	"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x001C 0x30 {$modeNumber}",
    //      zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, DataType.ENUM8, setpointModeNumber, ["mfgCode": "0x1185"]),
	// 	poll()
	// ], 1000)

	return (
		zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C, DataType.ENUM8, modeNumber)
		+ zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, DataType.ENUM8, setpointModeNumber, ["mfgCode": "0x1185"])
		+ poll()
	)
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
def cool() {log.debug "cool"; return setThermostatMode("cool");}

/* eco() is a custom command.*/
def eco() {log.debug("eco"); return setThermostatMode("eco");}

/* on() is a command belonging to the capability "Switch".  */
def on(){log.debug "on"; return setThermostatMode("heat");}

/* setThermostatMode() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def setThermostatMode(String mode) {
   return setCustomThermostatMode(mode);
}

/* setLevel() is a command belonging to the capability "SwitchLevel".  */
def setLevel(level, duration=null){
	return setHeatingSetpoint(level);
}

/* configure() is a command belonging to the capability "Configuration".  */
def configure() {
	log.debug "binding to Thermostat cluster"

	return (
		// "zdo bind 0x${device.deviceNetworkId} 1 0x19 0x201 {${device.zigbeeId}} {}",
		//Cluster ID (0x0201 = Thermostat Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
		
		//zigbee.configureReporting(zigbee.THERMOSTAT_CLUSTER, 0x0000, 0x29, 10, 60, 50), 	//Attribute ID 0x0000 = local temperature, Data Type: S16BIT
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0000,                     
				//Attribute ID 0x0000 = local temperature
			/*dataType*/         DataType.INT16, 
			/*minReportTime*/    10,
				//minimum number of seconds between reports						
			/*maxReportTime*/    60,                        
				//maximum number of seconds between reports
			/*reportableChange*/ 1 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +

		//zigbee.configureReporting(zigbee.THERMOSTAT_CLUSTER, 0x0012, DataType.INT16, 1, 0, 50),  	//Attribute ID 0x0012 = occupied heat setpoint, Data Type: S16BIT
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0012,                     
				//Attribute ID 0x0012 = occupied heat setpoint
			/*dataType*/         DataType.INT16, 
			/*minReportTime*/    1,
				//minimum number of seconds between reports						
			/*maxReportTime*/    0,                        
				//maximum number of seconds between reports
			/*reportableChange*/ 50 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +

		
		//zigbee.configureReporting(zigbee.THERMOSTAT_CLUSTER, 0x001C, DataType.ENUM8, 1, 0, 1),   	//Attribute ID 0x001C = system mode, Data Type: 8 bits enum
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x001C,                     
				//Attribute ID 0x001C = system mode
			/*dataType*/         DataType.ENUM8, //this is "discrete" according to DataType.isDiscrete(DataType.ENUM8)
			/*minReportTime*/    1,
				//minimum number of seconds between reports						
			/*maxReportTime*/    0,                        
				//maximum number of seconds between reports
			/*reportableChange*/ null 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +
		
		//zigbee.configureReporting(zigbee.THERMOSTAT_CLUSTER, 0x401C, DataType.ENUM8, 1, 0, 1),   	//Attribute ID 0x401C = manufacturer specific setpoint mode, Data Type: 8 bits enum
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x401C,                     
				//Attribute ID 0x401C = manufacturer specific setpoint mode
			/*dataType*/         DataType.ENUM8, //this is "discrete" according to DataType.isDiscrete(DataType.ENUM8)
			/*minReportTime*/    1,
				//minimum number of seconds between reports						
			/*maxReportTime*/    0,                        
				//maximum number of seconds between reports
			/*reportableChange*/ null 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +
		
		//zigbee.configureReporting(zigbee.THERMOSTAT_CLUSTER, 0x0008, DataType.ENUM8, 300, 900, 5),   //Attribute ID 0x0008 = pi heating demand, Data Type: U8BIT
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0008,                     
				//Attribute ID 0x0008 = pi heating demand
			/*dataType*/         DataType.ENUM8,  //this is "discrete" according to DataType.isDiscrete(DataType.ENUM8)
			/*minReportTime*/    300,
				//minimum number of seconds between reports						
			/*maxReportTime*/    900,                        
				//maximum number of seconds between reports
			/*reportableChange*/ null 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +

		//Cluster ID (0x0204 = Thermostat Ui Conf Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
		//zigbee.configureReporting(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0000, DataType.ENUM8, 1, 0, 1),   //Attribute ID 0x0000 = temperature display mode, Data Type: 8 bits enum
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			/*attributeId*/      0x0000,                     
				//Attribute ID 0x0000 = temperature display mode
			/*dataType*/         DataType.ENUM8,  //this is "discrete" according to DataType.isDiscrete(DataType.ENUM8), 
			/*minReportTime*/    1,
				//minimum number of seconds between reports						
			/*maxReportTime*/    0,                        
				//maximum number of seconds between reports
			/*reportableChange*/ null 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +
		
		//zigbee.configureReporting(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0001, DataType.ENUM8, 1, 0, 1),   //Attribute ID 0x0001 = keypad lockout, Data Type: 8 bits enum
		zigbee.configureReporting(
			/*cluster*/          zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			/*attributeId*/      0x0001,                     
				//Attribute ID 0x0001 = keypad lockout
			/*dataType*/         DataType.ENUM8,  //this is "discrete" according to DataType.isDiscrete(DataType.ENUM8), 
			/*minReportTime*/    1,
				//minimum number of seconds between reports						
			/*maxReportTime*/    0,                        
				//maximum number of seconds between reports
			/*reportableChange*/ null 
				// Amount of change needed to trigger a report. 
				// Required for analog data types. Discrete data types should always provide null for this value.	  
		) +

		//Read the configured variables
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0000 //Read Local Temperature
		) +	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0012 //Read Heat Setpoint
		) +
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x001C //Read System Mode
		) +
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x401C,  //Read Manufacturer Specific Setpoint Mode
			/*additionalParams*/ ["mfgCode": "0x1185"]
		) +
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0008 //Read PI Heating State
		) +
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			/*attributeId*/      0x0000 //Read Temperature Display Mode
		) +
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			/*attributeId*/      0x0001 //Read Keypad Lockout
		)
	);	
	

}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

/** 
controls how many digits after the decimal point we will round to when sending temperature events.
*/
def getTemperatureReportingPrecision(){return 2;}


//useful "enum" values (should be accessible via the zigbee module)
def getE_CLD_THERMOSTAT_ATTR_ID_LOCAL_TEMPERATURE()                 {return 0x0000;}
def getE_CLD_THERMOSTAT_ATTR_ID_OUTDOOR_TEMPERATURE()               {return 0x0001;}
def getE_CLD_THERMOSTAT_ATTR_ID_OCCUPANCY()                         {return 0x0002;}
def getE_CLD_THERMOSTAT_ATTR_ID_ABS_MIN_HEAT_SETPOINT_LIMIT()       {return 0x0003;}
def getE_CLD_THERMOSTAT_ATTR_ID_ABS_MAX_HEAT_SETPOINT_LIMIT()       {return 0x0004;}
def getE_CLD_THERMOSTAT_ATTR_ID_ABS_MIN_COOL_SETPOINT_LIMIT()       {return 0x0005;}
def getE_CLD_THERMOSTAT_ATTR_ID_ABS_MAX_COOL_SETPOINT_LIMIT()       {return 0x0006;}
def getE_CLD_THERMOSTAT_ATTR_ID_PI_COOLING_DEMAND()                 {return 0x0007;}
def getE_CLD_THERMOSTAT_ATTR_ID_PI_HEATING_DEMAND()                 {return 0x0008;}
def getE_CLD_THERMOSTAT_ATTR_ID_LOCAL_TEMPERATURE_CALIBRATION ()    {return 0x0010;}
def getE_CLD_THERMOSTAT_ATTR_ID_OCCUPIED_COOLING_SETPOINT()         {return 0x0011;}
def getE_CLD_THERMOSTAT_ATTR_ID_OCCUPIED_HEATING_SETPOINT()         {return 0x0012;}
def getE_CLD_THERMOSTAT_ATTR_ID_UNOCCUPIED_COOLING_SETPOINT()       {return 0x0013;}
def getE_CLD_THERMOSTAT_ATTR_ID_UNOCCUPIED_HEATING_SETPOINT()       {return 0x0014;}
def getE_CLD_THERMOSTAT_ATTR_ID_MIN_HEAT_SETPOINT_LIMIT()           {return 0x0015;}
def getE_CLD_THERMOSTAT_ATTR_ID_MAX_HEAT_SETPOINT_LIMIT()           {return 0x0016;}
def getE_CLD_THERMOSTAT_ATTR_ID_MIN_COOL_SETPOINT_LIMIT()           {return 0x0017;}
def getE_CLD_THERMOSTAT_ATTR_ID_MAX_COOL_SETPOINT_LIMIT()           {return 0x0018;}
def getE_CLD_THERMOSTAT_ATTR_ID_MIN_SETPOINT_DEAD_BAND()            {return 0x0019;}
def getE_CLD_THERMOSTAT_ATTR_ID_REMOTE_SENSING()                    {return 0x001a;}
def getE_CLD_THERMOSTAT_ATTR_ID_CONTROL_SEQUENCE_OF_OPERATION()     {return 0x001b;}
def getE_CLD_THERMOSTAT_ATTR_ID_SYSTEM_MODE()                       {return 0x001c;}
def getE_CLD_THERMOSTAT_ATTR_ID_ALARM_MASK()                        {return 0x001d;}