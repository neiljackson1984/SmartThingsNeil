metadata {
	definition (
		name: "ZWave Configuration Explorer Device", 
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
        //attributes: (none)
        //commands:  configure()
        
        command "getAssociationReport"
    	command "getVersionReport"
	    command "getCommandClassReport"
	    command "getParameterReport", [[name:"parameterNumber",type:"NUMBER", description:"Parameter Number (omit for a complete listing of parameters that have been set)", constraints:["NUMBER"]]]
	    command "setParameter",[[name:"parameterNumber",type:"NUMBER", description:"Parameter Number", constraints:["NUMBER"]],[name:"size",type:"NUMBER", description:"Parameter Size", constraints:["NUMBER"]],[name:"value",type:"NUMBER", description:"Parameter Value", constraints:["NUMBER"]]]
	

            
        attribute("zwaveCommandFromHubToDevice", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) send from the hub to the device.  
        attribute("zwaveCommandFromDeviceToHub", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device (in practice, this means that we will update this attribute every time the platform calls our parse() function.
        attribute("zwaveCommand", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device or send to the device.
        attribute("testEndpointResponse", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device or send to the device.
        

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
	message += "zwaveSecureEncap: " + zwaveSecureEncap.toString() + "\n";
	



	z0 = new hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport(value:0)
	message += "z0: " + z0.toString() + "\n";

	z1 = new hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport(value:1)
	message += "z1: " + z1.toString() + "\n";
	message += "z1.format(): " + z1.format() + "\n";
	message += "zwaveSecureEncap(z1).toString(): " + zwaveSecureEncap(z1).toString() + "\n";
	message += "zwaveSecureEncap(z1).class: " + zwaveSecureEncap(z1).getProperties()['class'] + "\n";

    message += "zwaveSecureEncap(z1.format()): " + zwaveSecureEncap(z1.format()).toString() + "\n";
	message += "zwaveSecureEncap(z1.format()).class: " + zwaveSecureEncap(z1).getProperties()['class'] + "\n";

    message += "response(z1.format()): " + response(z1.format()) + "\n";
    message += "sendHubCommand([response(z1.format())]): " + sendHubCommand([response(z1.format())]) + "\n";


	y = response(z1)
	message += "y: " + y.toString() + "\n";
	message += "y.class: " + y.getProperties()['class'] + "\n";
	message += "y.getProperties(): " + y.getProperties() + "\n";
	message += "y.action.class: " + y.action.getProperties()['class'] + "\n";

	message += "zwave.parse(y.action): " + zwave.parse(y.action) + "\n";

    def a = response(['delay 100'])
	message += "a.getProperties(): " + a.getProperties().toString() + "\n";

	message += "\n\n";

   return message;
}


//LIFECYCLE FUNCTION
void installed() {
	log.debug("installed");
    sendZwaveCommands([ zwave.manufacturerSpecificV1.manufacturerSpecificGet() ]);
}

//LIFECYCLE FUNCTION
List<String>  updated() {
	log.debug("updated");

    return null;
}

//LIFECYCLE FUNCTION
List<Map> parse(description) {
    def returnValue = []
    def cmd = zwave.parse(description, commandClassVersionMap)
    logZwaveCommandFromDeviceToHub(cmd);
    if (cmd) {
        returnValue += zwaveEvent(cmd)
    } else {
        logging("Non-parsed event: ${description}", 2)
    }
    return returnValue;
}



/* off() is a command belonging to the capability "Switch"  */
List<String> off() {
    log.debug "off"; 

    sendZwaveCommands([
        zwave.basicV1.basicSet(value: 0x00),
        zwave.switchBinaryV1.switchBinaryGet()
    ]);

    return null;

}

/* on() is a command belonging to the capability "Switch".  */
List<String> on(){
    log.debug "on";  
    
    sendZwaveCommands([
        zwave.basicV1.basicSet(value: 0xFF),
        zwave.switchBinaryV1.switchBinaryGet()
    ]);

    return null;
    
}

/* configure() is a command belonging to the capability "Confioguration"  */
List<String> configure(){
	log.debug "configure";

}


//custom command getVersionReport()
def getVersionReport(){
	sendZwaveCommands(zwave.versionV1.versionGet())		
}

//custom command setParameter()
List<String> setParameter(parameterNumber = null, size = null, value = null){
    if (parameterNumber == null || size == null || value == null) {
		log.warn "incomplete parameter list supplied..."
		log.info "syntax: setParameter(parameterNumber,size,value)"
    } else {
		return delayBetween([
	    	secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: value, parameterNumber: parameterNumber, size: size)),
	    	secureCmd(zwave.configurationV1.configurationGet(parameterNumber: parameterNumber))
		],500)
    }
}

//custom command 
List<String> getAssociationReport(){
	List<String> cmds = []
	1.upto(5, {
		cmds.add(secureCmd(zwave.associationV1.associationGet(groupingIdentifier: it)))
    })
    return delayBetween(cmds,500)	
}

//custom command 
List<String> getParameterReport(param = null){
    List<String> cmds = []
    if (param != null) {
		cmds = [secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param))]
    } else {
		0.upto(255, {
	    	cmds.add(secureCmd(zwave.configurationV1.configurationGet(parameterNumber: it)))	
		})
    }
    log.trace "configurationGet command(s) sent..."
    return delayBetween(cmds,500)
}	

//custom command 
List<String> getCommandClassReport(){
    List<String> cmds = []
    List<Integer> ic = getDataValue("inClusters").split(",").collect{ hexStrToUnsignedInt(it) }
    ic.each {
		if (it) cmds.add(secureCmd(zwave.versionV1.versionCommandClassGet(requestedCommandClass:it)))
    }
    return delayBetween(cmds,500)
}





//=============================

//this function needs to be public rather than private, so that I can invoke it by passing the function name 'sendZwaveCommands' to runIn();
public void sendZwaveCommands(Map arg){
    // log.debug "sendZwaveCommands was called with a map as the argument: ${arg}."
    sendZwaveCommands(arg.commands);
}


public void sendZwaveCommands(hubitat.zwave.Command command) {
    sendZwaveCommands([command])
}

public void sendZwaveCommands(String command) {
    sendZwaveCommands([command])
}


private String secureCmd(cmd) {
    if (getDataValue("zwaveSecurePairingComplete") == "true" && getDataValue("S2") == null) {
        return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        return zwaveSecureEncap(cmd)
    }	
}


public void sendZwaveCommands(List commands){
    //commands is expected to be a list, each element of which is either a string or a zwave command object.
    // we want to allow that the elements are strings so that we can pass in formatted commands (which are strings), delays (which are strings (for instance "delay 100")), or zwave command objects.
    
    if(commands)
    {
        List<String> formattedCommands = commands.collect{ it instanceof hubitat.zwave.Command ? secureCmd(it) : it };
        if(formattedCommands){
            logZwaveCommandFromHubToDevice(formattedCommands);
            sendHubCommand([response(formattedCommands)]); 
        }
    }
    return null;

}



//{  Z-WAVE LOGGING


    //the following two logZwaveCommand...() functions are pass-through functions -- they will return exactly what is passed to them.  we will wrap all outgoing and incoming zwave commands (in some reasonable format), respectively, in these two functions.
    //These functions accept as arguments exactly the sort of thing that is allowed to be returned from a command function (e.g. off(), on(), refresh(), etc.), namely, a string or an array whose elements are strings (or the type of thing returned by delay())
    //unfortunately, whereas the commands constructed with, for instance, zwave.basicV1.basicGet() produce a meaningful string in response to the format() method, the object returned by zwave.parse(description) in the parse() function behaves differently.
    //therefore, I have resorted to a rather hacky json serialize/deserialize process, so that the hubToDevice commands that we log are of the same type as the deviceToHub commands.
    def logZwaveCommandFromHubToDevice(x) {
        return logZwaveCommand(x, "zwaveCommandFromHubToDevice");
    }

    def logZwaveCommandFromDeviceToHub(x) {
        return logZwaveCommand(x, "zwaveCommandFromDeviceToHub");
    }

    //x is expected to be anything that would be a suitable return value for a command method - nameley a list of zwave commands and "delay nnn" directives or a single zwave command.
    //oops -- it is not instances of physicalgraph.zwave.Command that are returned from command functions but rather lists of strings that result from calling the format() method on physicalgraph.zwave.Command objects.
    // fortunately, zwave.parse(x.format()) will return something equivalent to x in the case where x is an instanceof physicalgraph.zwave.Command.  So, if we record the format()'ed strings, we won't be losing any information.
    
    //therefore, x is expected to be a string or a list of strings, and any members that happen to be instances of physicalgraph.zwave.Command will be converted into strings using physicalgraph.zwave.Command's format() method.
    def logZwaveCommand(x, attributeName) {
        if(x)
        {
            def listOfCommands = 
                (x instanceof java.util.List ? x : [x]).collect{
                    if(it instanceof hubitat.zwave.Command){
                        //return it.format();
                        //it.format() has a tenedency to throw an exception in the case where 'it' has 
                        //been constructed by calling zwave.parse(description), where description is, 
                        //for instance version 1 of some zwave command class, but zwave.parse, because 
                        //it has no way of knowing what version of the command class we are dealing with 
                        //(unless you provide a second argument), has returned a higher version of the 
                        //command class.  An exception gets thrown if the higher version of the command 
                        //class uses longer messages, in which case the lower version message, having 
                        //fewer bytes, will cause the zwave command object returned by zwave.parse() to
                        //have null values for some of its properties.  Calling format on this object 
                        //causes format() to try to convert null into a hexidecimal string, 
                        //which is what throws the exception.  To account for such a case, we put 
                        // our call to it.format() inside a try{} statement:
                        try{
                            return it.format();

                        } catch(e) {
                            return (
                                "exception encountered while logZwaveCommand tried to run the format() method of an object of class " + 
                                it.getProperties()['class'].name + 
                                ": " + e.toString()
                            );
                        }
                        return it.format();
                    } else {
                        return it;
                    }
                };
            //if we needed to distinguish between strings and zwave commands, we could do " ... instanceof physicalgraph.zwave.Command"
            sendEvent(
                name: attributeName, 
                value: groovy.json.JsonOutput.toJson(listOfCommands),
                displayed: false,
                isStateChange: true //we want to force this event to be recorded, even if the attribute value hasn't changed (which might be the case because we are sending the exact same zwave command for the second time in a row)
            );
            sendEvent(
                name: "zwaveCommand", 
                value: groovy.json.JsonOutput.toJson([direction: attributeName, commands: listOfCommands]), 
                displayed: false,
                isStateChange: true //we want to force this event to be recorded, even if the attribute value hasn't changed (which might be the case because we are sending the exact same zwave command for the second time in a row)
            );
            log.debug(
                (attributeName == "zwaveCommandFromHubToDevice" ? ">>>" : "<<<") + 
                listOfCommands.toString()
            );
        }
        return x;
    }        

//}


private getCommandClassVersionMap() {
    //we will pass this map as the second argument to zwave.parse()
    //this map tells zwave.parse() what version of the various zwave command classes to expect (and controls which version of the zwave classes the zwave.parse() method returns.
    // these values correspond to the version of the various command classes supported by the device.
    return [
            /*0x20*/ (commandClassCodes['BASIC'])          :  1, 
            /*0x25*/ (commandClassCodes['SWITCH_BINARY'])  :  1, 
            /*0x32*/ (commandClassCodes['METER'])          :  3, 
            /*0x60*/ (commandClassCodes['MULTI_CHANNEL'])  :  3, 
            /*0x70*/ (commandClassCodes['CONFIGURATION'])  :  1, 
            /*0x85*/ (commandClassCodes['ASSOCIATION'])    :  2,
            /*0x98*/ (commandClassCodes['SECURITY'])       :  1              
    ];
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


//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"