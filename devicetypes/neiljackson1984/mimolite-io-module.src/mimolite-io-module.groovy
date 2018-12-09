/**
 * adapted fromt "MimoLite Garage Door Controller" by Todd Wackford
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Note: This device type is based on the work of Jit Jack (cesaldar) as posted on the SmartThings Community website.
 *
 *  This device type file will configure a Fortrezz MimoLite Wireless Interface/Bridge Module as a Garage Door 
 *  Controller. The Garage Door must be physically configured per the following diagram: 
 *    "http://www.fortrezz.com/index.php/component/jdownloads/finish/4/17?Itemid=0"
 *  for all functionality to work correctly.
 *
 *  This device type will also set the atttibute "powered" to "powerOn" or "powerOff" accordingly. This uses
 *  the alarm capability of the MimoLite and the status will be displayed to the user on a secondary tile. User
 *  can subscribe to the status of this atttribute to be notified when power drops out.
 *
 *  This device type implements a "Configure" action tile which will set the momentary switch timeout to 25ms and
 *  turn on the powerout alarm.
 *
 *  
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "MimoLite io module", namespace: "neiljackson1984", author: "neiljackson1984") {
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
        
        command("runTheTestCode");
		attribute("powered", "string")
		attribute("debugMessage", "string")
        
        attribute("zwaveCommandFromHubToDevice", "string") //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) send from the hub to the device.  We will update this attribute whenever we return somewthing from a command function (like on(), off(), refresh(), etc.)
        attribute("zwaveCommandFromDeviceToHub", "string") //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device (in practice, this means that we will update this attribute every time the platform calls our parse() function.
        attribute("zwaveCommand", "string") //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device or send to the device.
	}

	// UI tile definitions 
	tiles(scale : 1) {

        valueTile("contact", "device.contact", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", defaultState: true, label: "state of the door is unknown"
            state "open", label: 'DOOR IS OPEN', icon: "st.doors.garage.garage-open" /*icon: "st.contact.contact.open",*/ //backgroundColor: "#e86d13"
			state "closed", label: 'DOOR IS CLOSED', icon: "st.doors.garage.garage-closed" /* icon: "st.contact.contact.closed",*/ //backgroundColor: "#00A0DC"
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
        standardTile("report", "device.report", width: 6, height: 6, decoration: "flat", alignment: 'left', align: 'left', style: 'text-align:left'){
        	state "default", label:'${currentValue}', defaultState:true 
        }
        
		// main (["contact", "switch"])
		main ("contact")
		// details(["contact", "switch", "powered", "refresh", "configure"])
		details(["contact", "switch", "powered", "refresh", "configure"])
	}
}

def runTheTestCode()
{
    def debugMessage = (new Date()).format(preferredDateFormat, location.getTimeZone()) + "\n";
    debugMessage += "ahoy there friends.\n";
    debugMessage += now() + "\n";
    sendEvent(name:"debugMessage", value: debugMessage);
    //return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}

String getPreferredDateFormat()
{
	return "yyyy/MM/dd HH:mm:ss";
}

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
                (it instanceof java.lang.String) || (it instanceof org.codehaus.groovy.runtime.GStringImpl) ? 
                it : 
                it.CMD + it.payload.collect{(it != null ? String.format("%02X",it) : "notlong")}.join()
            )
        }.toString()
    );
}    

def parse(String description) {
    //logZwaveCommandFromHubToDevice(zwave.parse(description).format());
    logZwaveCommandFromDeviceToHub(zwave.parse(description));
    def debugMessage = "";
    def debugMessageDelimeter = "\n";
    debugMessage += debugMessageDelimeter*2;
    debugMessage += "parse(${description}) was called" + debugMessageDelimeter;
    debugMessage += "getApiServerUrl(): " + getApiServerUrl() + debugMessageDelimeter;
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1])
    
    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
    	log.debug "Device lost power"
    	sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
    	sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }
    
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
    
    
    try {
        cmd.class
    } catch(java.lang.SecurityException e)
    {
        debugMessage += e.getMessage() + debugMessageDelimeter;
    } catch (e)
    {
        
    }
    
    debugMessage +=  "cmd: " + cmd + debugMessageDelimeter;
    debugMessage +=  "cmd.inspect(): " + cmd.inspect() + debugMessageDelimeter;
    debugMessage +=  "cmd.format(): " + cmd.inspect() + debugMessageDelimeter;
    debugMessage +=  "groovy.json.JsonOutput.toJson(cmd): " + groovy.json.JsonOutput.toJson(cmd) + debugMessageDelimeter;
    debugMessage +=  "groovy.json.JsonOutput.toJson(zwave.parse(description)): " + groovy.json.JsonOutput.toJson(zwave.parse(description)) + debugMessageDelimeter;
    debugMessage +=  "groovy.json.JsonOutput.toJson(zwave.switchBinaryV1.switchBinaryReport(value:0)): " + groovy.json.JsonOutput.toJson(zwave.switchBinaryV1.switchBinaryReport(value:0)) + debugMessageDelimeter;
    //the class of cmd is physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport
    //log.debug "command value is: $cmd.CMD"
	// log.debug "Parse returned ${result?.descriptionText}"
	debugMessage +=  "Parse returned ${result}" + debugMessageDelimeter;
    debugMessage += "zwave.basicV1.basicGet(): " + zwave.basicV1.basicGet().inspect()  + debugMessageDelimeter;
    debugMessage += "zwave.basicV1.basicGet().format(): " + zwave.basicV1.basicGet().format().inspect()  + debugMessageDelimeter;
    debugMessage += "zwave.basicV1.basicSet(value: 0xFF): " + zwave.basicV1.basicSet(value: 0xFF).inspect()  + debugMessageDelimeter;
    debugMessage += "zwave.basicV1.basicSet(value: 0xFF).format(): " + zwave.basicV1.basicSet(value: 0xFF).format().inspect()  + debugMessageDelimeter;
    debugMessage += "zwave.switchBinaryV1.switchBinaryGet().format(): " + zwave.switchBinaryV1.switchBinaryGet().format().inspect()  + debugMessageDelimeter;
    debugMessage += "zwave.switchBinaryV1.switchBinaryReport(value:0): " + zwave.switchBinaryV1.switchBinaryReport(value:0).inspect()  + debugMessageDelimeter;
    debugMessage += "zwave.switchBinaryV1.switchBinaryReport(value:0).format(): " + zwave.switchBinaryV1.switchBinaryReport(value:0).format().inspect()  + debugMessageDelimeter;
    //the class of zwave.switchBinaryV1.switchBinaryReport(value:0) is physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport

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
    sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
    log.debug "We lost power" //we caught this up in the parse method. This method not used.
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def configure() {
	log.debug "Configuring...." //setting up to monitor power alarm and actuator duration
	return logZwaveCommandFromHubToDevice( 
        delayBetween([
            zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
            zwave.configurationV1.configurationSet(configurationValue: [25], parameterNumber: 11, size: 1).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 11).format()
        ])
    );
}

def on() {
    return logZwaveCommandFromHubToDevice(
        delayBetween([
            zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
    );
}

def off() {
	return logZwaveCommandFromHubToDevice(
        delayBetween([
            zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
    );
}

def poll() {
    return logZwaveCommandFromHubToDevice(
        zwave.switchBinaryV1.switchBinaryGet().format()
    );
}

def refresh() {
    log.debug "refresh() was run"
	// zwave.switchBinaryV1.switchBinaryGet().format()
    return logZwaveCommandFromHubToDevice(
        delayBetween([
            zwave.switchBinaryV1.switchBinaryGet().format(),
            zwave.basicV1.basicGet().format(), //the device does not seem to respond to this command
            //zwave.configurationV2.ConfigurationBulkGet(numberOfParameters:10, parameterOffset: 0).format()
            zwave.sensorBinaryV1.sensorBinaryGet().format()
        ])
    );
}
