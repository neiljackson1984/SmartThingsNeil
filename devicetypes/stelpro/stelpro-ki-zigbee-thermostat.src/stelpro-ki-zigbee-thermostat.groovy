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

		
		command "setOutdoorTemperature",  ["number"]
		
		command(
			 "setCustomThermostatMode", 
			 [
				[
					 "name":"Thermostat mode*",
					 "description":"Thermostat mode to set",
					 "type":"ENUM",
					 "constraints":["auto","off","heat","emergency heat","cool", "foo"]
				]
			 ]
		)

		command "eco"
        command "runTheTestCode"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "STZB402+", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "ST218", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
	}

	preferences {
		input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
		input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
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
		message += "\t"*3 + "arguments:" + "\n";
		it.arguments.each{
			message += "\t"*4 + (it ? it.dump() : "false") + "\n";
		}
		message += "\t"*3 + "parameters:" + "\n";
		it.parameters.each{
			// message += "\t"*4 + (it ? it.dump() : "false") + "\n";
			message += "\t"*4 + (it == null ? "null" : groovy.json.JsonOutput.toJson(it)) + "\n";
		}
	}


	message += "\n\n";

	// message += "([1,2] + [3,4]): " + ([1,2] + [3,4]).dump() + "\n";

   log.debug(message);
   return respondFromTestCode(message);
}

def respondFromTestCode(message){
	sendEvent( name: 'testEndpointResponse', value: message )
	return message;
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
	installed()
	def requests = []
	requests += parameterSetting()
	response(requests)
}

def parameterSetting() {
	def lockmode = null
	def valid_lock = 0

	log.debug "lock : $settings.lock"
	if (settings.lock == "Yes") {
		lockmode = 0x01
		valid_lock = 1
	}
	else if (settings.lock == "No") {
		lockmode = 0x00
		valid_lock = 1
	}

	if (valid_lock == 1)
	{
		log.debug "lock valid"
		delayBetween([
			zigbee.writeAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x01, DataType.ENUM8, lockmode),	//Write Lock Mode
			poll(),
		], 200)
	}
	else {
		log.debug "nothing valid"
	}
}

def parse(description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = zigbee.parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0201" && descMap.attrId == "0000") {
			map.name = "temperature"
			map.unit = getTemperatureScale()
			map.value = getTemperature(descMap.value)
			if (descMap.value == "7ffd") {		//0x7FFD
				map.name = "temperatureAlarm"
				map.value = "freeze"
				map.unit = ""
			}
			else if (descMap.value == "7fff") {	//0x7FFF
				map.name = "temperatureAlarm"
				map.value = "heat"
				map.unit = ""
			}
			else if (descMap.value == "8000") {	//0x8000
				map.name = "temperatureAlarm"
				map.value = "cleared"
				map.unit = ""
			}
			
			else if (descMap.value > "8000") {
				map.value = -(Math.round(2*(655.36 - map.value))/2)
			}
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
			log.debug "HEATING SETPOINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
			map.data = [heatingSetpointRange: heatingSetpointRange]
			if (descMap.value == "8000") {		//0x8000
				map.name = "temperatureAlarm"
				map.value = "cleared"
				map.data = []
			}
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
			if (descMap.value.size() == 8) {
				log.debug "MODE"
				map.name = "thermostatMode"
				map.value = modeMap[descMap.value]
				map.data = [supportedThermostatModes: supportedThermostatModes]
			}
			else if (descMap.value.size() == 10) {
				log.debug "MODE & SETPOINT MODE"
				def twoModesAttributes = descMap.value[0..-9]
				map.name = "thermostatMode"
				map.value = modeMap[twoModesAttributes]
				map.data = [supportedThermostatModes: supportedThermostatModes]
			}
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "401c") {
			log.debug "SETPOINT MODE"
			log.debug "descMap.value $descMap.value"
			map.name = "thermostatMode"
			map.value = modeMap[descMap.value]
			map.data = [supportedThermostatModes: supportedThermostatModes]
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "0008") {
			log.debug "HEAT DEMAND"
			map.name = "thermostatOperatingState"
			if (descMap.value < "10") {
				map.value = "idle"
			}
			else {
				map.value = "heating"
			}

			if (settings.heatdetails == "No") {
				map.displayed = false
			}
		}
	}

	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}


def getModeMap() { [
	"00":"off",
	"04":"heat",
	"05":"eco"
]}


/**
  * PING is used by Device-Watch in attempt to reach the Device
**/
/* ping() is a command belonging to the capability "Health Check".  */
def ping() {
	zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000)
}

/* poll() is a command belonging to the capability "Polling".  */
def poll() {
	log.debug("poll");
	return delayBetween([
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000),	//Read Local Temperature
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0008),	//Read PI Heating State
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0012),	//Read Heat Setpoint
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C),	//Read System Mode
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, ["mfgCode": "0x1185"]),	//Read Manufacturer Specific Setpoint Mode
			zigbee.readAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0000),	//Read Temperature Display Mode
			zigbee.readAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0001)		//Read Keypad Lockout
		],100);
}



def getTemperature(value) {
	if (value != null) {
		log.debug("value $value")
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		}
		else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

/* refresh() is a command belonging to the capability "Refresh".  */
def refresh() {
	return poll();
}

/* setHeatingSetpoint() is a command belonging to the capabilities "Thermostat" and "Thermostat Heating Setpoint".  */
def setHeatingSetpoint(preciseDegrees) {
	if (preciseDegrees != null) {
		def temperatureScale = getTemperatureScale()
		def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)

		log.debug "setHeatingSetpoint({$degrees} ${temperatureScale})"
		
		
		def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)
		return [
			zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x12, DataType.INT16, hex(celsius * 100)),
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x12),	//Read Heat Setpoint
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x08),	//Read PI Heat demand
			poll()
		];
	}
}

/* setCoolingSetpoint() is a command belonging to the capability "Thermostat".  */
def setCoolingSetpoint(degrees) {
	log.trace "${device.displayName} does not support cool setpoint"
}

/* setOutdoorTemperature() is a custom command */
def setOutdoorTemperature(Double degrees) {
	def p = (state.precision == null) ? 1 : state.precision
	Integer tempToSend
	
	def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)

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



/* setThermostatMode() is a command belonging to the capabilities "Thermostat" and "Thermostat Mode".  */
def setThermostatMode(String value) {
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

	return [
		zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C, DataType.ENUM8, modeNumber),
		zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, DataType.ENUM8, setpointModeNumber, ["mfgCode": "0x1185"]),
		poll()
	];
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
def on() {}

def setCustomThermostatMode(String mode) {
   setThermostatMode(mode)
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


