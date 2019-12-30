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

		capability "Health Check"
		// Attributes
		//     checkInterval - NUMBER
		// 
		// Commands
		//     ping()
		
		
		command "switchMode"
		command "setOutdoorTemperature",  ["number"]
		command "parameterSetting"
		command "setCustomThermostatMode", ["enum"]
		command "eco"
        command "runTheTestCode"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "STZB402+", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "ST218", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
	}

	preferences {
		input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
		input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
		input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
	}

	tiles(scale : 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°')
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "increaseHeatSetpoint")
				attributeState("VALUE_DOWN", action: "decreaseHeatSetpoint")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("eco", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label:'${currentValue}°')
			}
		}
		standardTile("mode", "device.thermostatMode", width: 2, height: 2) {
			state "off", label:'${name}', action:"switchMode", nextState:"heat"
			state "heat", label:'${name}', action:"switchMode", nextState:"eco", icon:"http://cdn.device-icons.smartthings.com/Home/home29-icn@2x.png"
			state "eco", label:'${name}', action:"switchMode", nextState:"off", icon:"http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "heatingSetpoint", label:'Setpoint ${currentValue}°', backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
			]
		}
		standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main ("thermostatMulti")
		details(["thermostatMulti", "mode", "heatingSetpoint", "refresh", "configure"])
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
	if(true){
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

	if(true){
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
def ping() {
	zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000)
}

def poll() {
	return [
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0000),	//Read Local Temperature
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0008),	//Read PI Heating State
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x0012),	//Read Heat Setpoint
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C),	//Read System Mode
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, ["mfgCode": "0x1185"]),	//Read Manufacturer Specific Setpoint Mode
			zigbee.readAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0000),	//Read Temperature Display Mode
			zigbee.readAttribute(zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 0x0001)		//Read Keypad Lockout
		];
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

def refresh() {
	return poll();
}


def setHeatingSetpoint(preciseDegrees) {
	if (preciseDegrees != null) {
		def temperatureScale = getTemperatureScale()
		def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)

		log.debug "setHeatingSetpoint({$degrees} ${temperatureScale})"
		
		sendEvent(name: "heatingSetpoint", value: degrees, unit: temperatureScale, data: [heatingSetpointRange: heatingSetpointRange])
		sendEvent(name: "thermostatSetpoint", value: degrees, unit: temperatureScale, data: [thermostatSetpointRange: thermostatSetpointRange])
		
		def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)
		return [
			zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x12, DataType.INT16, hex(celsius * 100)),
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x12),	//Read Heat Setpoint
			zigbee.readAttribute(zigbee.THERMOSTAT_CLUSTER, 0x08),	//Read PI Heat demand
		];
	}
}

def setCoolingSetpoint(degrees) {
	log.trace "${device.displayName} does not support cool setpoint"
}


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

def switchMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(currentMode)
	def modeNumber;
	Integer setpointModeNumber;
	

	if (nextMode == "heat") {
		modeNumber = 04
		setpointModeNumber = 04
	}
	else if (nextMode == "eco") {
		modeNumber = 04
		setpointModeNumber = 05
	}
	else {
		modeNumber = 00
		setpointModeNumber = 00
	}
	
	
	
	// def explicitCommand = "st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x001C 0x30 {$modeNumber}";
	// def wrappedCommand = zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C, DataType.ENUM8, modeNumber)[0];

	// log.debug(
	// 	"comparison of explicit and wrapped zigbee commands in the switchMode() function: " + "\n" +
	// 	"explicit: " + explicitCommand + "\n" +
	// 	"wrapped:  " + wrappedCommand + "\n" +
	// 	(explicitCommand == wrappedCommand ? "SAME" : "DIFFERENT") + "\n"
	// )


	
	// delayBetween([
	// 	//"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x001C 0x30 {$modeNumber}",
	// 	zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C, DataType.ENUM8, modeNumber)
	// 	//the only conceivable reason for using the literal form of the command ("st wattr... ") instead of the 
	// 	// sugar-coated version (zigbee.writeAttribute(...)) is that the sugar-coated version
	// 	// outputs a "delay 2000" command after the write-attribute command, and you might want to avoid this extra delay
    //     zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, DataType.ENUM8, setpointModeNumber, ["mfgCode": "0x1185"]),
        
	// 	poll()
	// ], 1000)
	
	return [
		zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x001C, DataType.ENUM8, modeNumber),
		zigbee.writeAttribute(zigbee.THERMOSTAT_CLUSTER, 0x401C, DataType.ENUM8, setpointModeNumber, ["mfgCode": "0x1185"]),
		poll()
	];
}

def setThermostatFanMode(fanMode) {
	if(fanMode = "auto"){
		log.debug "complying with your request to set the fan mode to auto (because auto is the only supported fan mode)."
	} else {
		log.debug "could not set fan mode to ${fanMode}.  Only auto fan mode is supported."
	}
	return [];
}
def fanOn() {log.debug "fanOn"; return setThermostatFanMode("on");}
def fanAuto() {log.debug "fanAuto"; return setThermostatFanMode("auto");}
def fanCirculate() {log.debug "fanCirculate"; return setThermostatFanMode("circulate");}


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
def auto() {log.debug "auto"; return setThermostatMode("auto");}
def off() {log.debug "off"; return setThermostatMode("off");}
def heat() {log.debug "heat"; return setThermostatMode("heat");}
def emergencyHeat() {log.debug "emergencyHeat"; return setThermostatMode("emergency heat");}
def cool() {log.debug "cool"; return setThermostatMode("cool");}
def eco() {log.debug("eco"); return setThermostatMode("eco");}

def setCustomThermostatMode(mode) {
   setThermostatMode(mode)
}

def configure() {
	log.debug "binding to Thermostat cluster"

	return [
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
		),

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
		),

		
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
		),
		
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
		),
		
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
		),

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
		),
		
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
		),

		//Read the configured variables
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0000 //Read Local Temperature
		),	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0012 //Read Heat Setpoint
		),	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x001C //Read System Mode
		),	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x401C,  //Read Manufacturer Specific Setpoint Mode
			/*additionalParams*/ ["mfgCode": "0x1185"]
		),	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_CLUSTER, 
			/*attributeId*/      0x0008 //Read PI Heating State
		),	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			/*attributeId*/      0x0000 //Read Temperature Display Mode
		),	
		zigbee.readAttribute(
			/*cluster*/          zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER, 
			/*attributeId*/      0x0001 //Read Keypad Lockout
		)	
	];

}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}


