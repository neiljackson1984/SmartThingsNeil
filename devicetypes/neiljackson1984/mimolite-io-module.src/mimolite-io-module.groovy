/**
 * copyright 2018 by Neil Jackson
 *
 * adapted from
 *   1) "MimoLite Garage Door Controller" device handler by Todd Wackford (https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/mimolite-garage-door-controller.src/mimolite-garage-door-controller.groovy)
 *   2) "FortrezZ MIMOlite" device handler by Fortrezz (https://github.com/fortrezz/smartthings/blob/master/mimolite/devicehandler.groovy)
 *
 *
 *  
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "MimoLite io module", namespace: "neiljackson1984", author: "Neil Jackson") {
        fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x71,0x30,0x31,0x35,0x70,0x85,0x25,0x03"
        
        //TAGGING CAPABILITIES: ('tagging' implies that these capabilities have no attributes, and have no commands)
        
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform SmartThings that this device has commands     
        //attributes: (none)
        //commands:  (none)
        
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform SmartThings that this device has attributes     
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
        

		capability "Contact Sensor"
        //attributes: enum contact ("open", "closed")
        //commands: (none)
        
        capability "Voltage Measurement"
        //attributes: voltage (an object that has properties "value" and "unit"
        //commands: (none)
        
        attribute("powered", "string");
        
        command("runTheTestCode");

        attribute("configuration", "string"); //we will use this attribute to record the internal configuration data reported by the device (mainly for debugging and user interest)
		attribute("debugMessage", "string");
        attribute("zwaveCommandFromHubToDevice", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) send from the hub to the device.  We will update this attribute whenever we return somewthing from a command function (like on(), off(), refresh(), etc.)
        attribute("zwaveCommandFromDeviceToHub", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device (in practice, this means that we will update this attribute every time the platform calls our parse() function.
        attribute("zwaveCommand", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device or send to the device.
	}
    
    preferences {
       input "RelaySwitchDelay", "decimal", title: "Delay between relay switch on and off in seconds. Only Numbers 0 to 3.0 allowed. 0 value will remove delay and allow relay to function as a standard switch", description: "Numbers 0 to 3.1 allowed.", defaultValue: 0, required: false, displayDuringSetup: true
    }
    

	// UI tile definitions 
	tiles(scale : 2) {

        // valueTile("contact", "device.contact", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			// state "default", defaultState: true, label: "state of the door is unknown"
            // state "open", label: 'DOOR IS OPEN', icon: "st.doors.garage.garage-open" /*icon: "st.contact.contact.open",*/ //backgroundColor: "#e86d13"
			// state "closed", label: 'DOOR IS CLOSED', icon: "st.doors.garage.garage-closed" /* icon: "st.contact.contact.closed",*/ //backgroundColor: "#00A0DC"
		// }
        
        valueTile("contact", "device.contact", width: 2, height: 2, inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		}
        
        standardTile("switch", "device.switch", width: 2, height: 2/*, decoration: "flat"*/) {
			//state "doorClosed", label: "Closed", action: "on", icon: "st.doors.garage.garage-closed", backgroundColor: "#00A0DC"
            //state "doorOpen", label: "Open", action: "on", icon: "st.doors.garage.garage-open", backgroundColor: "#e86d13"
            //state "doorOpening", label: "Opening", action: "on", icon: "st.doors.garage.garage-opening", backgroundColor: "#e86d13"
            //state "doorClosing", label: "Closing", action: "on", icon: "st.doors.garage.garage-closing", backgroundColor: "#00A0DC"
            state "default", defaultState: true, label: "state of switch is unknown"
            state "on", label: "sending a pulse (click here to stop)", action: "switch.off",  icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "attemptingToStopThePulse" 
			state "off", label: "Send a pulse", action: "switch.on", nextState: "attemptingToSendAPulse" //, icon: "st.switches.switch.off" , backgroundColor: "#ffffff"
            state "attemptingToSendAPulse", label: "attempting to send a pulse..."
            state "attemptingToStopThePulse", label: "attempting to stop the pulse..."
            
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("powered", "device.powered", inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        
		// main (["contact", "switch"])
		main ("contact")
		// details(["contact", "switch", "powered", "refresh", "configure"])
		details(["contact", "switch", "powered", "refresh", "configure"])
	}
}



def updated() {
	log.debug "Settings Updated..."
    //device.configure();
    //return logZwaveCommandFromHubToDevice(getCommandsForConfigure()); //I don't think the platform interprets the return value of the updated() function as commands to be sent out.
    sendHubCommand(
        logZwaveCommandFromHubToDevice(getCommandsForConfigure()).collect{new physicalgraph.device.HubAction(it)}
    );
}


//==============parsing incoming commands and helper functions
def parse(String description) {
    //logZwaveCommandFromHubToDevice(zwave.parse(description).format());
    logZwaveCommandFromDeviceToHub(zwave.parse(description));
    def debugMessage = "";
    def debugMessageDelimeter = "\n";
    debugMessage += debugMessageDelimeter*2;
    debugMessage += "parse(${description}) was called" + debugMessageDelimeter;
    
	def result = null
	def cmd = zwave.parse(
        description, 
        //this map tells zwave.parse() what version of the various zwave command classes to expect
        [
            /*0x20*/ (commandClassCodes['BASIC']        ) : 1, 
            /*0x84*/ (commandClassCodes['WAKE_UP']      ) : 1, 
            /*0x30*/ (commandClassCodes['SENSOR_BINARY']) : 1, 
            /*0x70*/ (commandClassCodes['CONFIGURATION']) : 1
        ]
    );
    
    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
    	log.debug "Device lost power"
    	sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
    	sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }
    
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
    
    
    // try {
        // cmd.class
    // } catch(java.lang.SecurityException e)
    // {
        // debugMessage += e.getMessage() + debugMessageDelimeter;
    // } catch (e)
    // {
        
    // }
    
    // debugMessage +=  "cmd: " + cmd + debugMessageDelimeter;
    debugMessage +=  "cmd.inspect(): " + cmd.inspect() + debugMessageDelimeter;
    // debugMessage +=  "cmd.format(): " + cmd.inspect() + debugMessageDelimeter;
    // debugMessage +=  "groovy.json.JsonOutput.toJson(cmd): " + groovy.json.JsonOutput.toJson(cmd) + debugMessageDelimeter;
    // debugMessage +=  "groovy.json.JsonOutput.toJson(zwave.parse(description)): " + groovy.json.JsonOutput.toJson(zwave.parse(description)) + debugMessageDelimeter;
    // debugMessage +=  "groovy.json.JsonOutput.toJson(zwave.switchBinaryV1.switchBinaryReport(value:0)): " + groovy.json.JsonOutput.toJson(zwave.switchBinaryV1.switchBinaryReport(value:0)) + debugMessageDelimeter;
    // //the class of cmd is physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport
    // //log.debug "command value is: $cmd.CMD"
	// // log.debug "Parse returned ${result?.descriptionText}"
	// debugMessage +=  "Parse returned ${result}" + debugMessageDelimeter;
    // debugMessage += "zwave.basicV1.basicGet(): " + zwave.basicV1.basicGet().inspect()  + debugMessageDelimeter;
    // debugMessage += "zwave.basicV1.basicGet().format(): " + zwave.basicV1.basicGet().format().inspect()  + debugMessageDelimeter;
    // debugMessage += "zwave.basicV1.basicSet(value: 0xFF): " + zwave.basicV1.basicSet(value: 0xFF).inspect()  + debugMessageDelimeter;
    // debugMessage += "zwave.basicV1.basicSet(value: 0xFF).format(): " + zwave.basicV1.basicSet(value: 0xFF).format().inspect()  + debugMessageDelimeter;
    // debugMessage += "zwave.switchBinaryV1.switchBinaryGet().format(): " + zwave.switchBinaryV1.switchBinaryGet().format().inspect()  + debugMessageDelimeter;
    // debugMessage += "zwave.switchBinaryV1.switchBinaryReport(value:0): " + zwave.switchBinaryV1.switchBinaryReport(value:0).inspect()  + debugMessageDelimeter;
    // debugMessage += "zwave.switchBinaryV1.switchBinaryReport(value:0).format(): " + zwave.switchBinaryV1.switchBinaryReport(value:0).format().inspect()  + debugMessageDelimeter;
    // //the class of zwave.switchBinaryV1.switchBinaryReport(value:0) is physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport

    debugMessage += debugMessageDelimeter;
    log.debug debugMessage
    
    
	return result;
}


def sensorValueEvent(Short value) {
    // sendEvent(name: "contact", value: value ? "open" : "closed")
    return [name: "contact", value: value ? "open" : "closed"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
    //the device is deisgned to send a BasicSet command to devices in association group 1 in response to the sensor value changing
    //the idea is that the device could be controlling other zwave devices in response to the sensor value.
    //This is a bit confusing because basicSet is the command that we send to the device to set the relay state.
    sensorValueEvent(cmd.value)
    
    //the device does not even seem to respond to a basicGet command, which is a bit odd -- it seems like it would make sense for the device to respond in some meaningful way to a BasicGet command.
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	log.debug "received a binary sensor report"
    sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
    log.debug "We lost power" //we caught this up in the parse method. This method not used.
}

def zwaveEvent (physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) // sensorMultilevelReport is used to report the value of the analog voltage for SIG1
{
	log.debug "received a SensorMultilevelReport"
	def ADCvalue = cmd.scaledSensorValue
    sendEvent(name: "voltageCounts", value: ADCvalue)
   
    return(CalculateVoltage(cmd.scaledSensorValue));
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug("Un-parsed Z-Wave message ${cmd}")
	[:]
}

def zwaveEvent (physicalgraph.zwave.commands.configurationv1.ConfigurationReport  cmd) 
{
    def configuration;
    try{
        configuration = device.currentState("configuration").getJsonValue();
    } catch(e) 
    {
        log.debug "initializing the 'configuration' attribute.";
        sendEvent(name: 'configuration', value: "{}"); //we initialize to an empty json object.
        configuration = device.currentState("configuration").getJsonValue();
        log.debug "after initializing, configuration is " + configuration;
    }
    
    
    //update the configuration attribute as needed
    configuration.(cmd.parameterNumber) = cmd.configurationValue;
    
    
    sendEvent(name: 'configuration', value: groovy.json.JsonOutput.toJson(configuration));

}


def CalculateVoltage(ADCvalue)
{
	 def map = [:]
     
     def volt = (((1.5338*(10**-16))*(ADCvalue**5)) - ((1.2630*(10**-12))*(ADCvalue**4)) + ((3.8111*(10**-9))*(ADCvalue**3)) - ((4.7739*(10**-6))*(ADCvalue**2)) + ((2.8558*(10**-3))*(ADCvalue)) - (2.2721*(10**-2)))

    //def volt = (((3.19*(10**-16))*(ADCvalue**5)) - ((2.18*(10**-12))*(ADCvalue**4)) + ((5.47*(10**-9))*(ADCvalue**3)) - ((5.68*(10**-6))*(ADCvalue**2)) + (0.0028*ADCvalue) - (0.0293))
	//log.debug "$cmd.scale $cmd.precision $cmd.size $cmd.sensorType $cmd.sensorValue $cmd.scaledSensorValue"
	def voltResult = volt.round(1)// + "v"
    
	map.name = "voltage"
    map.value = voltResult
    map.unit = "v"
    return map
}


//============================ command methods
def configure() {
    log.debug "Configuring.... " //setting up to monitor power alarm and actuator duration
    
	return logZwaveCommandFromHubToDevice(getCommandsForConfigure());
}

def on() {
    return logZwaveCommandFromHubToDevice(
        delayBetween([
           zwave.basicV1.basicSet(value: 0xFF).format(),
           getCommandsForRefresh()
        ])
    );
}

def off() {
	return logZwaveCommandFromHubToDevice(
        delayBetween([
           zwave.basicV1.basicSet(value: 0x00).format(), 
           getCommandsForRefresh()
        ])
    );
}

def poll() {
    log.debug "poll() was run";
    return logZwaveCommandFromHubToDevice(getCommandsForRefresh());
}

def refresh() {
    log.debug "refresh() was run"
	// zwave.switchBinaryV1.switchBinaryGet().format()
    return logZwaveCommandFromHubToDevice(getCommandsForRefresh());
}

def runTheTestCode()
{
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";
    debugMessage += "settings: " + settings + "\n";
    


    
    sendEvent(name:"debugMessage", value: debugMessage);
    //return logZwaveCommandFromHubToDevice(getCommandsForConfigurationDump());
    return null;
}



// =========================== other 


//the following two logZwaveCommand...() functions are pass-through functions -- they will return exactly what is passed to them.  we will wrap all outgoing and incoming zwave commands (in some reasonable format), respectively, in these two functions.
//These functions accept as arguments exactly the sort of thing that is allowed to be returned from a command function (e.g. off(), on(), refresh(), etc.), namely, a string or an array whose elements are strings (or the type of thing returned by delay())
//unfortunately, whereas the commands constructed with, for instance, zwave.basicV1.basicGet() produce a meaningful string in response to the format() method, the object returned by zwave.parse(description) in the parse() function behaves differently.
//therefore, I have resorted to a rather hacky json serialize/deserialize process, so that the hubToDevice commands that we log are of the same type as the deviceToHub commands.
def logZwaveCommandFromHubToDevice(x) {
    logZwaveCommand(x, "zwaveCommandFromHubToDevice");
    return x;
}

def logZwaveCommandFromDeviceToHub(x) {
    logZwaveCommand(x, "zwaveCommandFromDeviceToHub");
    return x;
}

def logZwaveCommand(x, attributeName) {
    def listOfCommands = (x instanceof java.util.List ? x : [x]);
    sendEvent(
        name: attributeName, 
        value: groovy.json.JsonOutput.toJson(listOfCommands)
    );
    sendEvent(
        name: "zwaveCommand", 
        value: groovy.json.JsonOutput.toJson([direction: attributeName, commands: listOfCommands])
    );
    log.debug(
        (attributeName == "zwaveCommandFromHubToDevice" ? ">>>" : "<<<") + 
        listOfCommands.collect{
            (
                it
                // (it instanceof java.lang.String) || (it instanceof org.codehaus.groovy.runtime.GStringImpl) ? 
                // it : 
                // it.CMD + it.payload.collect{(it != null ? String.format("%02X",it) : "notlong")}.join()
            )
        }.toString()
    );
}    

def getCommandsForConfigure() {
    return delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(), // the Smartthings platform will have already set this setting, but we set it again here just to be sure. (and we might even want to remove the hub from association group 1, because the information that the device sends to the hub by virtue of the hub being in association group 1 is entirely redundant with the other association groups (I think))
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(), // 	FYI: Group 3: If a power dropout occurs, the MIMOlite will send an Alarm Command Class report 	(if there is enough available residual power)
        zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format(), // periodically send a multilevel sensor report of the ADC analog voltage to the input
        zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format(), // when the input is digitally triggered or untriggered, snd a binary sensor report
        zwave.configurationV1.configurationSet(configurationValue: [(settings.RelaySwitchDelay*10).toInteger()], parameterNumber: 11, size: 1).format(), // configurationValue for parameterNumber means how many 100ms do you want the relay
        																										// to wait before it cycles again / size should just be 1 (for 1 byte.)
        getCommandsForConfigurationDump()                                                                                                        
        //zwave.configurationV1.configurationGet(parameterNumber: 11).format() // gets the new parameter changes. not currently needed. (forces a null return value without a zwaveEvent funciton
	]);
}

def getCommandsForConfigurationDump()
{
     return delayBetween([
        //parameter 1: not used.
        zwave.configurationV1.configurationGet(parameterNumber: 1).format(),

        //parameter 2: Clear Pulse Meter Counts (actual value is “don’t care”; count gets reset whenever
        // this command is received regardless of value)
        // (I think that this only ever has any effect when we are setting it, not sure what it will return when we get it
        zwave.configurationV1.configurationGet(parameterNumber: 2).format(),

        //parameter 3: Trigger Mapping: 1 = SIG1 triggered/untriggered sets or clears Relay1 (Default=0x00;
        // Refer to description in User Manual under section, Input to Relay Mapping) Note
        // that neither a Basic Report nor a Binary Switch Report is sent when relay is
        // automatically set or cleared by Trigger Mapping.
        zwave.configurationV1.configurationGet(parameterNumber: 3).format(),

        //parameter 4: Lower Threshold, High (Default=0xBB; must be less than Upper Threshold Low and
        // greater than Lower Threshold Low)
        zwave.configurationV1.configurationGet(parameterNumber: 4).format(),

        //parameter 5: Lower Threshold, Low (Default=0xAB)
        zwave.configurationV1.configurationGet(parameterNumber: 5).format(),

        //parameter 6: Upper Threshold, High (Default=0xFF)
        zwave.configurationV1.configurationGet(parameterNumber: 6).format(),

        //parameter 7: Upper Threshold, Low (Default = 0xFE; must be greater than Lower Threshold High and less than Upper Threshold High)
        zwave.configurationV1.configurationGet(parameterNumber: 7).format(),

        //parameter 8: flags 
        // (
        //      Bit1 : Digital-Configuration flag
        //          1=Set Trigger levels for this channel to ‘digital’ thresholds (approx. 1V); Default
        //          0=Set Trigger levels to analog thresholds (see parameters 4 through 7)
        //      Bit0 : Trigger-Between-Thresholds flag (see below)
        //          1 = Set to ‘triggered’ when input falls between thresholds; Default
        //          0 = Set to ‘triggered’ when input falls outside of thresholds
        // )
        zwave.configurationV1.configurationGet(parameterNumber: 8).format(),

        //parameter 9: Periodic send interval of Multilevel Sensor Reports (Association Group 2) and/or
        // Pulse Count Reports (Association Group 5) for SIG1. This parameter has a resolution
        // of 10 seconds; for example, 1 = 10 seconds, 2 = 20 seconds, 3 = 30 seconds (Default),
        // …, 255 = 2550 seconds = 42.5 minutes. A value of 0 disables automatic reporting.
        zwave.configurationV1.configurationGet(parameterNumber: 9).format(),

        //parameter 10: not used
        zwave.configurationV1.configurationGet(parameterNumber: 10).format(),

        //parameter 11: Momentary Relay1 output enable/disable. 0 = disable (Default)
        // 1..255 = enable / value sets the approximate momentary on time in increments
        // of 100msec.
        zwave.configurationV1.configurationGet(parameterNumber: 11).format()
    ]);  
}


//we use this sequence of zwave commands numerous places above, not just in the refresh command,
// so I have encapsulated them in their own function (rather than calling the refresh() function all over the place)
// so that the zwave command logging scheme will be consistent -- we should only ever call the logZwaveCommandFromHubToDevice() when we are
//actually returning a list of events from a command method.
def getCommandsForRefresh()  {
    return delayBetween([
            zwave.switchBinaryV1.switchBinaryGet().format(), //requests a report of the relay to make sure that it changed (the report is used elsewhere, look for switchBinaryReport()
            //zwave.basicV1.basicGet().format(), //the device does not seem to respond to this command
            zwave.sensorMultilevelV5.sensorMultilevelGet().format(), // requests a report of the anologue input voltage
            (new physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryGet()).format()
        ]);
}





// borrowed from https://github.com/codersaur/SmartThings/blob/master/devices/zwave-tweaker/zwave-tweaker.groovy
/**
 *  getCommandClassNames()
 *
 *  Returns a map of command class names. Used by toCcNames().
 **/
private getCommandClassNames() {
    return [
        0x00: 'NO_OPERATION',
        0x20: 'BASIC',
        0x21: 'CONTROLLER_REPLICATION',
        0x22: 'APPLICATION_STATUS',
        0x23: 'ZIP',
        0x24: 'SECURITY_PANEL_MODE',
        0x25: 'SWITCH_BINARY',
        0x26: 'SWITCH_MULTILEVEL',
        0x27: 'SWITCH_ALL',
        0x28: 'SWITCH_TOGGLE_BINARY',
        0x29: 'SWITCH_TOGGLE_MULTILEVEL',
        0x2A: 'CHIMNEY_FAN',
        0x2B: 'SCENE_ACTIVATION',
        0x2C: 'SCENE_ACTUATOR_CONF',
        0x2D: 'SCENE_CONTROLLER_CONF',
        0x2E: 'SECURITY_PANEL_ZONE',
        0x2F: 'SECURITY_PANEL_ZONE_SENSOR',
        0x30: 'SENSOR_BINARY',
        0x31: 'SENSOR_MULTILEVEL',
        0x32: 'METER',
        0x33: 'SWITCH_COLOR',
        0x34: 'NETWORK_MANAGEMENT_INCLUSION',
        0x35: 'METER_PULSE',
        0x36: 'BASIC_TARIFF_INFO',
        0x37: 'HRV_STATUS',
        0x38: 'THERMOSTAT_HEATING',
        0x39: 'HRV_CONTROL',
        0x3A: 'DCP_CONFIG',
        0x3B: 'DCP_MONITOR',
        0x3C: 'METER_TBL_CONFIG',
        0x3D: 'METER_TBL_MONITOR',
        0x3E: 'METER_TBL_PUSH',
        0x3F: 'PREPAYMENT',
        0x40: 'THERMOSTAT_MODE',
        0x41: 'PREPAYMENT_ENCAPSULATION',
        0x42: 'THERMOSTAT_OPERATING_STATE',
        0x43: 'THERMOSTAT_SETPOINT',
        0x44: 'THERMOSTAT_FAN_MODE',
        0x45: 'THERMOSTAT_FAN_STATE',
        0x46: 'CLIMATE_CONTROL_SCHEDULE',
        0x47: 'THERMOSTAT_SETBACK',
        0x48: 'RATE_TBL_CONFIG',
        0x49: 'RATE_TBL_MONITOR',
        0x4A: 'TARIFF_CONFIG',
        0x4B: 'TARIFF_TBL_MONITOR',
        0x4C: 'DOOR_LOCK_LOGGING',
        0x4D: 'NETWORK_MANAGEMENT_BASIC',
        0x4E: 'SCHEDULE_ENTRY_LOCK',
        0x4F: 'ZIP_6LOWPAN',
        0x50: 'BASIC_WINDOW_COVERING',
        0x51: 'MTP_WINDOW_COVERING',
        0x52: 'NETWORK_MANAGEMENT_PROXY',
        0x53: 'SCHEDULE',
        0x54: 'NETWORK_MANAGEMENT_PRIMARY',
        0x55: 'TRANSPORT_SERVICE',
        0x56: 'CRC_16_ENCAP',
        0x57: 'APPLICATION_CAPABILITY',
        0x58: 'ZIP_ND',
        0x59: 'ASSOCIATION_GRP_INFO',
        0x5A: 'DEVICE_RESET_LOCALLY',
        0x5B: 'CENTRAL_SCENE',
        0x5C: 'IP_ASSOCIATION',
        0x5D: 'ANTITHEFT',
        0x5E: 'ZWAVEPLUS_INFO',
        0x5F: 'ZIP_GATEWAY',
        0x60: 'MULTI_CHANNEL',
        0x61: 'ZIP_PORTAL',
        0x62: 'DOOR_LOCK',
        0x63: 'USER_CODE',
        0x64: 'HUMIDITY_CONTROL_SETPOINT',
        0x65: 'DMX',
        0x66: 'BARRIER_OPERATOR',
        0x67: 'NETWORK_MANAGEMENT_INSTALLATION_MAINTENANCE',
        0x68: 'ZIP_NAMING',
        0x69: 'MAILBOX',
        0x6A: 'WINDOW_COVERING',
        0x6B: 'IRRIGATION',
        0x6C: 'SUPERVISION',
        0x6D: 'HUMIDITY_CONTROL_MODE',
        0x6E: 'HUMIDITY_CONTROL_OPERATING_STATE',
        0x6F: 'ENTRY_CONTROL',
        0x70: 'CONFIGURATION',
        0x71: 'NOTIFICATION',
        0x72: 'MANUFACTURER_SPECIFIC',
        0x73: 'POWERLEVEL',
        0x74: 'INCLUSION_CONTROLLER',
        0x75: 'PROTECTION',
        0x76: 'LOCK',
        0x77: 'NODE_NAMING',
        0x7A: 'FIRMWARE_UPDATE_MD',
        0x7B: 'GROUPING_NAME',
        0x7C: 'REMOTE_ASSOCIATION_ACTIVATE',
        0x7D: 'REMOTE_ASSOCIATION',
        0x80: 'BATTERY',
        0x81: 'CLOCK',
        0x82: 'HAIL',
        0x84: 'WAKE_UP',
        0x85: 'ASSOCIATION',
        0x86: 'VERSION',
        0x87: 'INDICATOR',
        0x88: 'PROPRIETARY',
        0x89: 'LANGUAGE',
        0x8A: 'TIME',
        0x8B: 'TIME_PARAMETERS',
        0x8C: 'GEOGRAPHIC_LOCATION',
        0x8E: 'MULTI_CHANNEL_ASSOCIATION',
        0x8F: 'MULTI_CMD',
        0x90: 'ENERGY_PRODUCTION',
        0x91: 'MANUFACTURER_PROPRIETARY',
        0x92: 'SCREEN_MD',
        0x93: 'SCREEN_ATTRIBUTES',
        0x94: 'SIMPLE_AV_CONTROL',
        0x95: 'AV_CONTENT_DIRECTORY_MD',
        0x96: 'AV_RENDERER_STATUS',
        0x97: 'AV_CONTENT_SEARCH_MD',
        0x98: 'SECURITY',
        0x99: 'AV_TAGGING_MD',
        0x9A: 'IP_CONFIGURATION',
        0x9B: 'ASSOCIATION_COMMAND_CONFIGURATION',
        0x9C: 'SENSOR_ALARM',
        0x9D: 'SILENCE_ALARM',
        0x9E: 'SENSOR_CONFIGURATION',
        0x9F: 'SECURITY_2',
        0xEF: 'MARK',
        0xF0: 'NON_INTEROPERABLE'
    ]
}

private getCommandClassCodes() {
    //I constructed the below list using groovy as follows:
    // debugMessage += "[" + "\n";
    // getCommandClassNames().collectEntries { key, value -> [value, key] }.sort().each{
        // key, value ->
        // debugMessage += "     " + key.inspect() + ": " + String.format("0x%02X",value) + "," + "\n"
    // }   
    // debugMessage += "]" + "\n";
    
    return [
        'ANTITHEFT': 0x5D,
        'APPLICATION_CAPABILITY': 0x57,
        'APPLICATION_STATUS': 0x22,
        'ASSOCIATION': 0x85,
        'ASSOCIATION_COMMAND_CONFIGURATION': 0x9B,
        'ASSOCIATION_GRP_INFO': 0x59,
        'AV_CONTENT_DIRECTORY_MD': 0x95,
        'AV_CONTENT_SEARCH_MD': 0x97,
        'AV_RENDERER_STATUS': 0x96,
        'AV_TAGGING_MD': 0x99,
        'BARRIER_OPERATOR': 0x66,
        'BASIC': 0x20,
        'BASIC_TARIFF_INFO': 0x36,
        'BASIC_WINDOW_COVERING': 0x50,
        'BATTERY': 0x80,
        'CENTRAL_SCENE': 0x5B,
        'CHIMNEY_FAN': 0x2A,
        'CLIMATE_CONTROL_SCHEDULE': 0x46,
        'CLOCK': 0x81,
        'CONFIGURATION': 0x70,
        'CONTROLLER_REPLICATION': 0x21,
        'CRC_16_ENCAP': 0x56,
        'DCP_CONFIG': 0x3A,
        'DCP_MONITOR': 0x3B,
        'DEVICE_RESET_LOCALLY': 0x5A,
        'DMX': 0x65,
        'DOOR_LOCK': 0x62,
        'DOOR_LOCK_LOGGING': 0x4C,
        'ENERGY_PRODUCTION': 0x90,
        'ENTRY_CONTROL': 0x6F,
        'FIRMWARE_UPDATE_MD': 0x7A,
        'GEOGRAPHIC_LOCATION': 0x8C,
        'GROUPING_NAME': 0x7B,
        'HAIL': 0x82,
        'HRV_CONTROL': 0x39,
        'HRV_STATUS': 0x37,
        'HUMIDITY_CONTROL_MODE': 0x6D,
        'HUMIDITY_CONTROL_OPERATING_STATE': 0x6E,
        'HUMIDITY_CONTROL_SETPOINT': 0x64,
        'INCLUSION_CONTROLLER': 0x74,
        'INDICATOR': 0x87,
        'IP_ASSOCIATION': 0x5C,
        'IP_CONFIGURATION': 0x9A,
        'IRRIGATION': 0x6B,
        'LANGUAGE': 0x89,
        'LOCK': 0x76,
        'MAILBOX': 0x69,
        'MANUFACTURER_PROPRIETARY': 0x91,
        'MANUFACTURER_SPECIFIC': 0x72,
        'MARK': 0xEF,
        'METER': 0x32,
        'METER_PULSE': 0x35,
        'METER_TBL_CONFIG': 0x3C,
        'METER_TBL_MONITOR': 0x3D,
        'METER_TBL_PUSH': 0x3E,
        'MTP_WINDOW_COVERING': 0x51,
        'MULTI_CHANNEL': 0x60,
        'MULTI_CHANNEL_ASSOCIATION': 0x8E,
        'MULTI_CMD': 0x8F,
        'NETWORK_MANAGEMENT_BASIC': 0x4D,
        'NETWORK_MANAGEMENT_INCLUSION': 0x34,
        'NETWORK_MANAGEMENT_INSTALLATION_MAINTENANCE': 0x67,
        'NETWORK_MANAGEMENT_PRIMARY': 0x54,
        'NETWORK_MANAGEMENT_PROXY': 0x52,
        'NODE_NAMING': 0x77,
        'NON_INTEROPERABLE': 0xF0,
        'NOTIFICATION': 0x71,
        'NO_OPERATION': 0x00,
        'POWERLEVEL': 0x73,
        'PREPAYMENT': 0x3F,
        'PREPAYMENT_ENCAPSULATION': 0x41,
        'PROPRIETARY': 0x88,
        'PROTECTION': 0x75,
        'RATE_TBL_CONFIG': 0x48,
        'RATE_TBL_MONITOR': 0x49,
        'REMOTE_ASSOCIATION': 0x7D,
        'REMOTE_ASSOCIATION_ACTIVATE': 0x7C,
        'SCENE_ACTIVATION': 0x2B,
        'SCENE_ACTUATOR_CONF': 0x2C,
        'SCENE_CONTROLLER_CONF': 0x2D,
        'SCHEDULE': 0x53,
        'SCHEDULE_ENTRY_LOCK': 0x4E,
        'SCREEN_ATTRIBUTES': 0x93,
        'SCREEN_MD': 0x92,
        'SECURITY': 0x98,
        'SECURITY_2': 0x9F,
        'SECURITY_PANEL_MODE': 0x24,
        'SECURITY_PANEL_ZONE': 0x2E,
        'SECURITY_PANEL_ZONE_SENSOR': 0x2F,
        'SENSOR_ALARM': 0x9C,
        'SENSOR_BINARY': 0x30,
        'SENSOR_CONFIGURATION': 0x9E,
        'SENSOR_MULTILEVEL': 0x31,
        'SILENCE_ALARM': 0x9D,
        'SIMPLE_AV_CONTROL': 0x94,
        'SUPERVISION': 0x6C,
        'SWITCH_ALL': 0x27,
        'SWITCH_BINARY': 0x25,
        'SWITCH_COLOR': 0x33,
        'SWITCH_MULTILEVEL': 0x26,
        'SWITCH_TOGGLE_BINARY': 0x28,
        'SWITCH_TOGGLE_MULTILEVEL': 0x29,
        'TARIFF_CONFIG': 0x4A,
        'TARIFF_TBL_MONITOR': 0x4B,
        'THERMOSTAT_FAN_MODE': 0x44,
        'THERMOSTAT_FAN_STATE': 0x45,
        'THERMOSTAT_HEATING': 0x38,
        'THERMOSTAT_MODE': 0x40,
        'THERMOSTAT_OPERATING_STATE': 0x42,
        'THERMOSTAT_SETBACK': 0x47,
        'THERMOSTAT_SETPOINT': 0x43,
        'TIME': 0x8A,
        'TIME_PARAMETERS': 0x8B,
        'TRANSPORT_SERVICE': 0x55,
        'USER_CODE': 0x63,
        'VERSION': 0x86,
        'WAKE_UP': 0x84,
        'WINDOW_COVERING': 0x6A,
        'ZIP': 0x23,
        'ZIP_6LOWPAN': 0x4F,
        'ZIP_GATEWAY': 0x5F,
        'ZIP_NAMING': 0x68,
        'ZIP_ND': 0x58,
        'ZIP_PORTAL': 0x61,
        'ZWAVEPLUS_INFO': 0x5E
    ];
}
