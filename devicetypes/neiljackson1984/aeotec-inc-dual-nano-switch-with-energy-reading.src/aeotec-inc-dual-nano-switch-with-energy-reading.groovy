/**
 *  MAGIC COMMENTS USED BY MY MAKEFILE FOR UPLOADING AND TESTING THE CODE:
 *  //////smartThingsId=63157b48-4ea8-4dd5-8f2a-d0661acd6b42
 *  //////smartThingsIdOfTestInstance=b92235ad-e0c3-4085-89e1-ed21b56cd4ce
 *  //////testEndpoint=runTheTestCode
 *  //////typeOfCode=device
 *  //////urlOfSmartThings=https://graph-na04-useast2.api.smartthings.com
 * 
 * donwload 2018/12/18 from https://aeotec.freshdesk.com/helpdesk/attachments/6059005046, per instructions on Aeotec's website
 *  Aeotec Inc Dual Nano Switch with Energy Reading
 *
 *  github: Eric Maycock (erocm123)
 *  Date: 2018-01-02
 *  Copyright Eric Maycock
 *
 *  Includes all configuration parameters and ease of advanced configuration.
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
 */
 
//fingerprint mfr: "0086", model: "0084" // Aeon brand
//fingerprint mfr: "0000", model: "0000" // Secure Pairing
//inClusters:"0x5E,0x25,0x27,0x32,0x81,0x71,0x60,0x8E,0x2C,0x2B,0x70,0x86,0x72,0x73,0x85,0x59,0x98,0x7A,0x5A"
//Aeotec Inc Dual Nano Switch with Energy Reading (ZW132)

/*
 We want to control some of the device's internal, non-volatile state 
(which might be described as "settings" or "configuration"), 
specifically, the values of all the registers that are accessible with 
the zwave CONFIGURATION command class, and the values of the association 
lists that are accessible with the zwave ASSOCIATION command class. As I 
understand it, these two sets of information represent all the 
configurable internal state of a zwave device (I am sure there are 
exceptions (for example: over-the-air-updateable firmware image might be 
considered another part of the configurable internal state), but at 
least the CONFIGURATION and ASSOCIATION registers should come close to 
capturing all configurable internal state for most z-wave devices.) 

We will keep track of our best knowledge of the internal configurable 
state of the device in the device handler's 'state' object, specifically 
within state.deviceConfiguration. state.deviceConfiguration will contain 
two child nodes: "configurationParameters" and "associationLists". The 
structure of the data will follow the SmartThings classes that represent 
zwave commands. the keys of 
state.deviceConfiguration.configurationParameters will be integers, 
corresponding to the configuration parameter numbers of the zwave 
configuration commands. The values will be arrays of numbers, each 
element representing one byte of the parameter value reported in the 
zwave configuration commands. Similarly, the keys of 
state.deviceConfiguration.associationLists will be integers, and the 
values will be arrays of numbers. 

We will modify state.deviceConfiguration when, and only when, we receive 
a configuration report or an association report command from the device. 

We will have a function, getPreferredDeviceConfiguration that will 
return our desired deviceConfiguration (with exactly the same structure 
as state.deviceConfiguration). based on the preferences (and possibly 
also hard-coded values). 

The goal is to act to ensure that state.deviceConfiguration is equal to 
preferredDeviceConfiguration. 

Whenever the user changes the preferences (i.e. in the updated() 
function body) and whenever we modify state.deviceConfiguration -- these 
are times, and are the only times, when state.deviceConfiguration and 
preferredDeviceConfiguration might diverge from one another. At these 
two times, we will invoke the function reconcileDeviceConfiguration(). 
This function will determine whether state.deviceConfiguration and 
preferredDeviceConfiguration are equal. The function will update the 
value of an attribute to record whether there is agreement between 
preferred and actual configuration.; If they are not equal, the function 
will send zwave commands to the device as needed to attempt to change 
the device's internal configuration to match 
preferredDeviceConfiguration. The function will also, of course, send 
zwave commands to query the state of the (hopefully updated) internal 
configuration. 

It probably makes sense to periodically (perhaps as part of the device 
health check mechanism) check whether a mismatch between preferred and 
actual configuration has existed for more than some threshhold duration, 
and, if so, 1) throw some kind of error (perhaps setting the device 
health status to degraded) and 2) take corrective action. It probably 
makes sense to design the reconcileDeviceConfiguration() configuration 
so that it can be fired as part of the periodic health check and the 
function will perform the above check. In other words, 
reconcileDeviceconfiguration() will be invoked immediately upon 
discovering a mismatch, and will also be invoked periodically to check 
for a chronic mismatch. A chronic mismatch could be caused by a 
malfunctioning device, radio interference, parts of the Smartthings 
cloud infrastructure malfunctioning, a hub malfunction, or, just as 
likely, a poorly written device handler. In any case, a chronic mismatch 
is a sign that the system is not working as expected, and is therefore 
worth recording, notifying the user about, and responding to. 


*/

private reconcileDeviceConfiguration(){
    def mismatchExists = false;
    def commandsToSend = [];
    
    preferredDeviceConfiguration.configurationParameters.each{
        if(state.deviceConfiguration?.configurationParameters?.getAt(it.key) != it.value)
        {
            mismatchExists = true;
            commandsToSend << new physicalgraph.zwave.commands.configurationv1.ConfigurationSet(parameterNumber: it.key, configurationValue: it.value).format();
            commandsToSend << new physicalgraph.zwave.commands.configurationv1.ConfigurationGet(parameterNumber: it.key).format();
        }
    }
    
    preferredDeviceConfiguration.associationLists.each{
        if(state.deviceConfiguration?.associationLists?.getAt(it.key) != it.value)
        {
            mismatchExists = true;
            
            def actualNodeList = state.deviceConfiguration?.associationLists?.getAt(it.key) ?:[];
            def preferredNodeList = it.value;
            
            def nodesToAdd = preferredNodeList - actualNodeList;
            def nodesToRemove = actualNodeList - preferredNodeList;
            
            nodesToAdd.each{nodeId -> commandsToSend << new physicalgraph.zwave.commands.associationv1.AssociationSet(groupingIdentifier: it.key, nodeId: nodeId).format();}
            nodesToRemove.each{nodeId -> commandsToSend << new physicalgraph.zwave.commands.associationv1.AssociationRemove(groupingIdentifier: it.key, nodeId: nodeId).format();}
            
            commandsToSend << new physicalgraph.zwave.commands.associationv1.AssociationGet(groupingIdentifier: it.key).format();
        }
    }
    
    return commandsToSend;
}

private Map getPreferredDeviceConfiguration(){
   return [configurationParameters: [99:[4,5,6]],associationLists: [1: [1,2,3]]]; 
}
    
private getSetting(nameOfSetting){
    return settings?.containsKey(nameOfSetting) ? settings[nameOfSetting] : getDefaultSettings()[nameOfSetting];
}

private Map getDefaultSettings(){
    return \
        [
            'preferredTriggerMappingEnabled'          :    false ,
            'preferredLowerThreshold'                :    (int) 3002  ,
            'preferredUpperThreshold'                :    (int) 4095  ,
            'preferredDigitalConfigurationFlag'       :    true  ,
            'preferredTriggerBetweenThresholdsFlag'   :    true  ,
            'preferredReportingInterval'              :    (int) 3     ,
            'preferredMomentaryDuration'              :    (int) 0             
        ];
}

metadata {
    definition (name: "Aeotec Inc Dual Nano Switch with Energy Reading", namespace: "neiljackson1984"/*"erocm123"*/, author: "Eric Maycock") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Polling"
        capability "Configuration"
        capability "Refresh"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Health Check"


        
        attribute("zwaveCommandFromHubToDevice", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) send from the hub to the device.  
        attribute("zwaveCommandFromDeviceToHub", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device (in practice, this means that we will update this attribute every time the platform calls our parse() function.
        attribute("zwaveCommand", "string"); //we will update this attribute to record a log of every zwave command that we (i.e. the device handler) receive from the device or send to the device.
        
        
        //fingerprint mfr: "0086", model: "0084" // Aeon brand
        //inClusters:"0x5E,0x25,0x27,0x32,0x81,0x71,0x60,0x8E,0x2C,0x2B,0x70,0x86,0x72,0x73,0x85,0x59,0x98,0x7A,0x5A"
        
        //something about the above fingerprint and inClusters() call is causing the IDE to throw a jdbc hibernation error upon trying to publish.  commenting the above two lines out seems to fix the problem (which is an acceptable solution for testing)
    }

    simulator {
    }

    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input (name: "aaa", type:"enum",title:"try me",options: ["a","b","c"],multiple:true);
        generate_preferences(configuration_model())
    }

    tiles {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
                attributeState "statusText", label:'${currentValue}'
            }
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} kWh'
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} W'
        }
		valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} V'
        }
        valueTile("current", "device.current", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} A'
        }
        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'reset kWh', action:"reset"
        }

        main(["switch","switch1", "switch2"])
        details(["switch", "energy", "power", "voltage", "current",
                childDeviceTiles("all"),
                "refresh","configure",
                "reset"
                ])
   }
}

 mappings {
     path("/runTheTestCode") { action: [GET:"runTheTestCode"] }
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
            for(item in e.getStackTrace())
            {
                stackTraceItems += item;
            }
            def filteredStackTrace = stackTraceItems.findAll{it['fileName']?.startsWith("script_") }.init();  //The init() method returns all but the last element.
            filteredStackTrace.each{debugMessage += " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n";   }
                 
        } catch(ee){ }
        
        // debugMessage += "filtered stack trace: \n" + 
            // groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(filteredStackTrace)) + "\n";
        
        log.debug(debugMessage);
        
        return render(
            contentType: "text/html", 
            data: debugMessage += "\n",
            status: 200
        );
    }
}

def mainTestCode1(){
    log.debug "mainTestCode() was run";
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";
    
    

    
    // debugMessage += "state: " + groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(state)) + "\n";
    
    // def theCommand = new physicalgraph.zwave.commands.configurationv1.ConfigurationSet(configurationValue: [1,2,3], parameterNumber: 14);
    // theCommand = new physicalgraph.zwave.commands.configurationv1.ConfigurationSet(configurationValue: [1,2,3,4,5], parameterNumber: 14);
    // debugMessage += "theCommand: " + theCommand + "\n";
    // debugMessage += "theCommand.format(): " + theCommand.format() + "\n";
    // debugMessage += "theCommand.payload: " + theCommand.payload + "\n";
    // debugMessage += summarize("([1,2,3] == [1,2,3])") + "\n";
    // debugMessage += summarize("([1,2,3] == [1.0,2.0,3,6])") + "\n";

    // def a = [:];
    // a[5] = "ahoy";
    // debugMessage += "a?.x: " + a?.x + "\n";
    // debugMessage += "a['x']: " + a['x'] + "\n";
    // debugMessage += "a[5]: " + a[5] + "\n";
    // debugMessage += "a['5']: " + a['5'] + "\n";
    // debugMessage += "a.'5': " + a.'5' + "\n";
    // debugMessage += "a.getAt(5): " + a.getAt(5) + "\n";
    // debugMessage += "a?.getAt(5): " + a?.getAt(5) + "\n";
    // debugMessage += "b?.getAt(5): " + b?.getAt(5) + "\n";
    // debugMessage += "a.5: " + a.5 + "\n";

    // def a = [1,2,2,3,4,5];
    // def b = [2,3,4];
    
    // debugMessage += summarize("${a} - ${b}") + "\n";
    // debugMessage += summarize("${b} - ${a}") + "\n";
    // debugMessage += summarize("${a}.intersect(${a})") + "\n";
    // debugMessage += summarize("$a + $b") + "\n";
    // debugMessage += summarize("$a << $b") + "\n";
    // debugMessage += "reconcileDeviceConfiguration(): " + reconcileDeviceConfiguration() + "\n";
    // debugMessage += "response(reconcileDeviceConfiguration()): " + response(reconcileDeviceConfiguration()) + "\n";
    // runIn(5,foo);
    // childOn("-ep1");
    // childOff("-ep1");
   
   def childDevice = childDevices.first();
   
   debugMessage += "childDevice.currentSwitch: " + childDevice.currentSwitch + "\n";
   
   def commandsToToggleSwitch1 = [
        "delay 500",
        command(encap(zwave.basicV1.basicSet(value: (childDevices[0].currentSwitch=="on" ? 0x00 : 0xFF)), channelNumber(childDevices[0].deviceNetworkId))),
        "delay 1000",
        command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(childDevices[0].deviceNetworkId)))
    ];
    

    // debugMessage += "cmds: " + cmds + "\n";
    
    
    // def startTime, endTime;
    // // // sendEvent(response(cmds));
   
    // // startTime = now();
    // // sendHubCommand([response(cmds)]); 
    // // //the time required to run sendHubCommand() is unaffected by the delay entries.  This suggests that sendHubCommand() does not block while the hub is performing
    // // // the action, but instead simply adds the commands to a queue and returns.
    // // endTime = now();
    // // debugMessage += "time required to invoke sendHubCommand([response(cmds)]): " + (endTime - startTime) + "\n";
    // def arg1 = cmds.collect{new physicalgraph.device.HubAction(it)};
    // def arg2 = [response(cmds)];
    // startTime = now();
    // sendHubCommand(arg2);
    // endTime = now();
    // debugMessage += "time required to invoke sendHubCommand(arg2): " + (endTime - startTime) + "\n";
     // //sendHubCommand does not block while the hub is acting.  This makes me think that sendHubAction is entirely equivalent to returning a list of commands from a command method.
    // sendZwaveCommands(commandsToToggleSwitch1); 
    // sendHubCommand([response(commandsToToggleSwitch1)]); 
   
   // def myHubAction = new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: (childDevices[1].currentSwitch=="on" ? 0x00 : 0xFF)), channelNumber(childDevices[1].deviceNetworkId))), callback:calledBackHandler);
   def myHubAction1 = new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: (childDevices[1].currentSwitch=="on" ? 0x00 : 0xFF)), channelNumber(childDevices[1].deviceNetworkId))));
   myHubAction1.options = [callback:calledBackHandler];
   
   // debugMessage += "myHubAction: " + myHubAction + "\n";
   // debugMessage += "myHubAction.inspect(): " + myHubAction.inspect() + "\n";
   debugMessage += "myHubAction1.getProperties(): " + myHubAction1.getProperties() + "\n";
   
   sendHubCommand(myHubAction1);
   
   
   def myHubAction2 = new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(childDevices[1].deviceNetworkId))));
   myHubAction2.options = [callback:calledBackHandler];
   
   sendHubCommand([
        new physicalgraph.device.HubAction("delay 1000"),
        myHubAction2
   ]);
   
   // sendZwaveCommands([
            // "delay 1000",
            // command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(childDevices[1].deviceNetworkId)))
        // ] );
   // def x = [];
   // x = new String("");
   // debugMessage += "x: " + x + "\n";
   // foo(x);
    // debugMessage += "x: " + x + "\n";
    // foo(x);
     // debugMessage += "x: " + x + "\n";
     
    def cmds = []; 
    // (1..2).each { endpoint ->
        // cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
        // cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
    // }
    cmds = [
           encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
           encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
        ];
    debugMessage += "cmds: " + cmds + "\n";
    debugMessage += "commands(cmds): " + commands(cmds) + "\n";
    // def formattedCommands = commands(cmds).collect{ it instanceof String ? it : command(it)  };
    def formattedCommands = commands(cmds).collect{ it instanceof physicalgraph.zwave.Command ? command(it) : it };
    // debugMessage += "" + commands(cmds).collect{it.inspect() + " is " + (it instanceof String ? "" : "not ") + "a string"} + "\n";
    // def myDelay = delayBetween(["a","b"])[1];
    // debugMessage += "myDelay: " + myDelay + "\n";
    // debugMessage += "command(myDelay): " + command(myDelay)+ "\n";
    // debugMessage += "myDelay.getProperties(): " + myDelay.getProperties() + "\n";
    // debugMessage += "" + commands(cmds).collect{it.inspect() + " is " + (it instanceof String ? "" : "not ") + "a string"} + "\n";
    
    debugMessage += "formattedCommands: " + formattedCommands + "\n";
   // sendZwaveCommands(commands(cmds));
   // debugMessage += "('600D00012502' instanceof physicalgraph.zwave.Command): " + ('600D00012502' instanceof physicalgraph.zwave.Command) + "\n";
    poll();


    return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}

def mainTestCode(){
    log.debug "mainTestCode() was run";
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";

    // updateDataValue("ahoy", "everyone")
    
    debugMessage += "data: " + data + "\n";
    debugMessage += "state: " + state + "\n";
    state['ahoy'] = "foo";

    // debugMessage += "device.getManufacturerName(): " + device.getManufacturerName() + "\n";
    // debugMessage += "device.getModelName(): " + device.getModelName() + "\n";
    debugMessage += "device.getProperties(): " + device.getProperties() + "\n";
    // debugMessage += "device.getMethods(): " + device.getMethods() + "\n";
    debugMessage += "getDataValue('ahoy'): " + getDataValue('ahoy') + "\n";
    debugMessage += "this: " + this + "\n";
    debugMessage += "this.getProperties(): " + this.getProperties() + "\n";
    // debugMessage += "getDataValues(): " + getDataValues() + "\n";
    // debugMessage += new groovy.inspect.Inspector(this).methodInfo("getDataValue") + "\n";
    // updateDataValue("ahoy", null);
    // deleteDataValue("ahoy");
    data = [:];
    return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}


def calledBackHandler(arg=null){
    log.debug "calledBackHandler(${arg}) was called."
}

def sendZwaveCommands(commands){
    //commands is expected to be a list, each element of which is either a string or a zwave command object.
    // we want to allow that the elements are strings so that we can pass in formatted commands (which are strings), delays (which are strings (for instance "delay 100")), or zwave command objects.
    def formattedCommands = commands.collect{ it instanceof physicalgraph.zwave.Command ? command(it) : it };
    logZwaveCommandFromHubToDevice(formattedCommands);
    sendHubCommand([response(formattedCommands)]); 
    return void;
}

def summarize(String expression){
    //note: pay attention to the scope that evaluate is using -- this only works for global variables.
    return expression + ": " + evaluate(expression);
}

def parse(String description) {
    def result = []
    def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x32: 3, 0x60: 3, 0x70: 1, 0x98: 1])
    logZwaveCommandFromDeviceToHub(cmd);
    if (cmd) {
        result += zwaveEvent(cmd)
        logging("Parsed ${cmd} to ${result.inspect()}", 1)
    } else {
        logging("Non-parsed event: ${description}", 2)
    }

    def statusTextmsg = ""

    result.each {
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "power") {
            statusTextmsg = "${it.value} W ${device.currentValue('energy')? device.currentValue('energy') : "0"} kWh"
        }
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "energy") {
            statusTextmsg = "${device.currentValue('power')? device.currentValue('power') : "0"} W ${it.value} kWh"
        }
    }
    if (statusTextmsg != "") sendEvent(name:"statusText", value:statusTextmsg, displayed:false)

    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logging("BasicReport ${cmd}", 2)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    logging("BasicSet ${cmd}", 2)
    def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    // return [result, response(commands(cmds))] // returns the result of reponse()
    sendZwaveCommands(commands(cmds));
    return [result];
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
    logging("SwitchBinaryReport ${cmd} , ${ep}", 2)
    if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"}
        if (childDevice)
            childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        // return [result, response(commands(cmds))] // returns the result of reponse()
        sendZwaveCommands(commands(cmds));
        return [result];
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
    logging("MeterReport $cmd : Endpoint: $ep", 2)
    def result
    def cmds = []
    if (cmd.scale == 0) {
        result = [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
    } 
    else if (cmd.scale == 1) {
        result = [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
    } 
    else if (cmd.scale == 4) {
    	result = [name: "voltage", value: cmd.scaledMeterValue, unit: "V"]
    }
    else if (cmd.scale == 5) {
    	result = [name: "current", value: cmd.scaledMeterValue, unit: "A"]
    }
    else {
        result = [name: "power", value: cmd.scaledMeterValue, unit: "W"]
    }
    if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"}
        if (childDevice)
            childDevice.sendEvent(result)
    } else {
       (1..2).each { endpoint ->
            cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
       }
       // return [createEvent(result), response(commands(cmds))]
       sendZwaveCommands(commands(cmds));
       return [createEvent(result)];
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   logging("MultiChannelCmdEncap ${cmd}", 2)
   def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
   if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
   }
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    logging("SensorMultilevelReport: $cmd", 2)
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.unit = getTemperatureScale()
            logging("Temperature Report: $map.value", 2)
            break;
        default:
            map.descriptionText = cmd.toString()
    }

    return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logging("ManufacturerSpecificReport ${cmd}", 2)
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    logging("msr: $msr", 2)
    updateDataValue("MSR", msr)
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep=null) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    logging("Unhandled Event: ${cmd}" + (ep?" ep: ${ep}":""), 2)
}

def on() {
    logging("on()", 1)
    sendZwaveCommands(
        commands([
            zwave.switchAllV1.switchAllOn(),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        ])
    );
}

def off() {
    logging("off()", 1)
    sendZwaveCommands(
        commands([
            zwave.switchAllV1.switchAllOff(),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        ])
    );
}

void childOn(String dni) {
    logging("childOn($dni)", 1)
    // def cmds = []
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni))))
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    // sendHubCommand(cmds)
    
    sendZwaveCommands(
        [
            command(encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni))),
            command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni)))
        ]
    );
}

void childOff(String dni) {
    logging("childOff($dni)", 1)
    // def cmds = []
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni))))
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    // sendHubCommand(cmds)
    
    sendZwaveCommands(
        [
            command(encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni))),
            command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni)))
        ]
    );
}

void childRefresh(String dni) {
    logging("childRefresh($dni)", 1)
    // def cmds = []
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 0), channelNumber(dni))))
    // cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 2), channelNumber(dni))))
    // sendHubCommand(cmds)
    
    sendZwaveCommands(
        [
            command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))),
            command(encap(zwave.meterV2.meterGet(scale: 0), channelNumber(dni))),
            command(encap(zwave.meterV2.meterGet(scale: 2), channelNumber(dni))),
        ]
    );
}

def poll() {
    logging("poll()", 1)
    sendZwaveCommands(
        commands([
           encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
           encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
        ])
    );
}

def refresh() {
    logging("refresh()", 1)
    sendZwaveCommands(
        commands([
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
            zwave.meterV2.meterGet(scale: 0),
            zwave.meterV2.meterGet(scale: 2),
            zwave.meterV2.meterGet(scale: 4),
            zwave.meterV2.meterGet(scale: 5),
            zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
        ])
    );
}

def reset() {
    logging("reset()", 1)
    sendZwaveCommands(
        commands([
            zwave.meterV2.meterReset(),
            zwave.meterV2.meterGet()
        ])
    );
}

def ping() {
    logging("ping()", 1)
    refresh()
}

def installed() {
    logging("installed()", 1)
    sendZwaveCommands([ command(zwave.manufacturerSpecificV1.manufacturerSpecificGet()) ]);
    createChildDevices()
}

def configure() {
    logging("configure()", 1)
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) sendZwaveCommands(commands(cmds))
}

def updated() {
    logging("updated()", 1)
    if (!childDevices) {
        createChildDevices()
    } else if (device.label != state.oldLabel) {
        childDevices.each {
            if (it.label == "${state.oldLabel} (Q${channelNumber(it.deviceNetworkId)})") {
                def newLabel = "${device.displayName} (Q${channelNumber(it.deviceNetworkId)})"
                it.setLabel(newLabel)
            }
        }
        state.oldLabel = device.label
    }
    def cmds = []
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    // if (cmds != []) response(commands(cmds))
    if (cmds != []) sendZwaveCommands(commands(cmds));
}

def generate_preferences(configuration_model) {
    def configuration = parseXml(configuration_model)

    configuration.Value.each {
        if(it.@hidden != "true" && it.@disabled != "true") {
            switch(it.@type) {
                case ["number"]:
                    input "${it.@index}", "number",
                        title:"${it.@label}\n" + "${it.Help}",
                        range: "${it.@min}..${it.@max}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}"
                    break
                case "list":
                    def items = []
                    it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                    input "${it.@index}", "enum",
                        title:"${it.@label}\n" + "${it.Help}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}",
                        options: items
                    break
                case "decimal":
                    input "${it.@index}", "decimal",
                        title:"${it.@label}\n" + "${it.Help}",
                        range: "${it.@min}..${it.@max}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}"
                    break
                case "boolean":
                    input "${it.@index}", "boolean",
                        title:"${it.@label}\n" + "${it.Help}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}"
                    break
            }
        }
    }
}

 /*  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). */
//transfers information to device.state.
def update_current_properties(cmd) {
    def currentProperties = state.currentProperties ?: [:]

    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue
    
    def parameterSettings = parseXml(configuration_model()).Value.find{it.@index == "${cmd.parameterNumber}"}

    if (settings."${cmd.parameterNumber}" != null || parameterSettings.@hidden == "true") {
        if (convertParam(cmd.parameterNumber, parameterSettings.@hidden != "true"? settings."${cmd.parameterNumber}" : parameterSettings.@value) == cmd2Integer(cmd.configurationValue)) {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        } else {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

//transfers information from device.state to the device.
def update_needed_settings() {
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]

    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
    //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1)
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
    cmds << zwave.associationV2.associationGet(groupingIdentifier: 1)
    
    configuration.Value.each {
        if ("${it.@setting_type}" == "zwave" && it.@disabled != "true") {
            if (currentProperties."${it.@index}" == null) {
                if (it.@setonly == "true") {
                    logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"), 2)
                    def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                } else {
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown", 2)
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            } else if ((settings."${it.@index}" != null || "${it.@hidden}" == "true") && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}")) {
                isUpdateNeeded = "YES"
                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}"), 2)
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}")
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            }
        }
    }

    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def convertParam(number, value) {
    def parValue
    switch (number) {
        case 110:
            if (value < 0)
                parValue = value * -1 + 1000
            else
                parValue = value
            break
        default:
            parValue = value
            break
    }
    return parValue.toInteger()
}

private def logging(message, level) {
log.debug "$message"
  /*  if (logLevel > 0) {
        switch (logLevel) {
            case "1":
                if (level > 1)
                    log.debug "$message"
                break
            case "99":
                log.debug "$message"
                break
        }
    }
  */
}

/**
* Convert byte values to integer
*/
def cmd2Integer(array) {
    switch(array.size()) {
        case 1:
            array[0]
            break
        case 2:
            ((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
            break
        case 3:
            ((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
            break
        case 4:
            ((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
            break
    }
}

def integer2Cmd(value, size) {
    switch(size) {
        case 1:
            [value]
            break
        case 2:
            def short value1 = value & 0xFF
            def short value2 = (value >> 8) & 0xFF
            [value2, value1]
            break
        case 3:
            def short value1 = value & 0xFF
            def short value2 = (value >> 8) & 0xFF
            def short value3 = (value >> 16) & 0xFF
            [value3, value2, value1]
            break
        case 4:
            def short value1 = value & 0xFF
            def short value2 = (value >> 8) & 0xFF
            def short value3 = (value >> 16) & 0xFF
            def short value4 = (value >> 24) & 0xFF
            [value4, value3, value2, value1]
            break
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "receive a configuration report"
    update_current_properties(cmd)
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'", 2)
}

private encap(cmd, endpoint) {
    if (endpoint) {
        zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
    } else {
        cmd
    }
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } 
    else {
        cmd.format()
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1, 0x32: 3, 0x60: 3, 0x70: 1, 0x98: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
    	state.sec = 1
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

private commands(commands, delay=1000) {
    delayBetween(commands.collect{ command(it) }, delay)
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

private void createChildDevices() {
    state.oldLabel = device.label
    try {
        for (i in 1..2) {
            addChildDevice("erocm123", "Metering Switch Child Device", "${device.deviceNetworkId}-ep${i}", null,
                [completedSetup: true, label: "${device.displayName} (Q${i})",
                isComponent: false, componentName: "ep$i", componentLabel: "Output $i"])
        }
    } catch (e) {
        log.debug e
        runIn(2, "sendAlert")
    }
}

private sendAlert() {
    sendEvent(
        descriptionText: "Child device creation failed. Please make sure that the \"Metering Switch Child Device\" is installed and published.",
        eventType: "ALERT",
        name: "childDeviceCreation",
        value: "failed",
        displayed: true,
    )
}

def configuration_model() {
'''
<configuration>
	<Value type="list" byteSize="1" index="3" label="3 OverCurrent Protection. " min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
        The output load will automatically turn off after 30 seconds if current exceeds 10.5A.
            0 - Disable
            1 - Enable
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="Disable" value="0" />
            <Item label="Enable" value="1" />
      </Value>
    <Value type="list" byteSize="1" index="4" label="4 Overheat Protection. " min="0" max="1" value="0" setting_type="zwave" fw="">
        <Help>
        The output load will automatically turn off after 30 seconds if temperature is over 100 C.
            0 - Disable
            1 - Enable
            Range: 0~1
            Default: 0 (Previous State)
        </Help>
            <Item label="Disable" value="0" />
            <Item label="Enable" value="1" />
      </Value>
      <Value type="list" byteSize="1" index="20" label="20 After a power outage" min="0" max="2" value="0" setting_type="zwave" fw="">
        <Help>
        Configure the output load status after re-power from a power outage.
            0 - Last status before power outage.
            1 - Always ON
            2 - Always OFF
            Range: 0~2
            Default: 0 (Previous State)
        </Help>
            <Item label="Last Status" value="0" />
            <Item label="Always On" value="1" />
            <Item label="Always Off" value="2" />
      </Value>
      <Value type="list" byteSize="1" index="80" label="80 Instant Notification" min="0" max="3" value="2" setting_type="zwave" fw="">
        <Help>
        Notification report of status change sent to Group Assocation #1 when state of output load changes. Used to instantly update status to your gateway typically.
            0 - Nothing
            1 - Hail CC (uses more bandwidth)
            2 - Basic Report CC
            3 - Hail CC when external switch is used to change status of either load.
            Range: 0~3
            Default: 2 (Previous State)
        </Help>
            <Item label="None" value="0" />
            <Item label="Hail CC" value="1" />
            <Item label="Basic Report CC" value="2" />
            <Item label="Hail when External Switch used" value="3" />
      </Value>
      <Value type="list" byteSize="1" index="81" label="81 Notification send with S1 Switch" min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
        To set which notification would be sent to the associated nodes in association group 3 when using the external switch 1 to switch the loads.
            0 = Send Nothing
            1 = Basic Set CC.
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="Nothing" value="0" />
            <Item label="Basic Set CC" value="1" />
      </Value>
      <Value type="list" byteSize="1" index="82" label="82 Notification send with S2 Switch" min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
        To set which notification would be sent to the associated nodes in association group 4 when using the external switch 2 to switch the loads.
            0 = Send Nothing
            1 = Basic Set CC.
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="Nothing" value="0" />
            <Item label="Basic Set CC" value="1" />
      </Value>
      <Value type="list" byteSize="1" index="83" label="83 State of Internal LED use" min="0" max="2" value="0" setting_type="zwave" fw="">
        <Help>
        Configure the state of LED when it is in 3 modes below:
            0 = Energy mode. The LED will follow the status (on/off).
            1 = Momentary indicate mode. When the state of Switchs load changed, the LED will follow the status (on/off) of its load, but the LED will turn off after 5 seconds if there is no any switch action.
            2 = Night light mode. The LED will remain ON state.
        </Help>
            <Item label="Energy Mode" value="0" />
            <Item label="Momentary Mode" value="1" />
            <Item label="Night Light Mode" value="2" />
      </Value>
      <Value type="list" byteSize="1" index="90" label="90 Threshold Enable/Disable" min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
       		Enables/disables parameter 91 and 92 below:
            0 = disabled
            1 = enabled
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="Disable" value="0" />
            <Item label="Enable" value="1" />
      </Value>
      <Value type="number" byteSize="4" index="91" label="91 Watt Threshold" min="0" max="60000" value="25" setting_type="zwave" fw="">
        <Help>
       		The value here represents minimum change in wattage (in terms of wattage) for a REPORT to be sent (Valid values 0-60000). 
            Range: 0~60000
            Default: 25 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="1" index="92" label="92 kWh Threshold" min="0" max="100" value="5" setting_type="zwave" fw="">
        <Help>
       		The value here represents minimum change in wattage percent (in terms of percentage %) for a REPORT to be sent. 
            Range: 0~100
            Default: 5 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="4" index="101" label="101 (Group 1) Timed Automatic Reports" min="0" max="1776399" value="12" setting_type="zwave" fw="">
        <Help>
       		Sets the sensor report for kWh, Watt, Voltage, or Current.
            Value Identifiers-
                1 = Voltage
                2 = Current
                4 = Watt
                8 = kWh
                256 = Watt on OUT1
                512 = Watt on OUT2
                2048 = kWh on OUT1
                4096 = kWh on OUT2
                65536 = V on OUT1
                131072 = V on OUT2
                524288 = A on OUT1
                1048576 = A on OUT2
            Example: If you want only Watt and kWh to report, sum the value identifiers together for Watt and kWh. 8 + 4 = 12, therefore entering 12 into this setting will give you Watt + kWh reports if set.
            Range: 0~1776399
            Default: 12 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="4" index="102" label="102 (Group 2) Timed Automatic Reports" min="0" max="1776399" value="0" setting_type="zwave" fw="">
        <Help>
       		Sets the sensor report for kWh, Watt, Voltage, or Current.
            Value Identifiers-
                1 = Voltage
                2 = Current
                4 = Watt
                8 = kWh
                256 = Watt on OUT1
                512 = Watt on OUT2
                2048 = kWh on OUT1
                4096 = kWh on OUT2
                65536 = V on OUT1
                131072 = V on OUT2
                524288 = A on OUT1
                1048576 = A on OUT2
            Example: If you want only Voltage and Current to report, sum the value identifiers together for Voltage + Current. 1 + 2 = 3, therefore entering 3 into this setting will give you Voltage + Current reports if set.
            Range: 0~1776399
            Default: 0 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="4" index="103" label="103 (Group 3) Timed Automatic Reports" min="0" max="1776399" value="0" setting_type="zwave" fw="">
        <Help>
       		Sets the sensor report for kWh, Watt, Voltage, or Current.
            Value Identifiers-
                1 = Voltage
                2 = Current
                4 = Watt
                8 = kWh
                256 = Watt on OUT1
                512 = Watt on OUT2
                2048 = kWh on OUT1
                4096 = kWh on OUT2
                65536 = V on OUT1
                131072 = V on OUT2
                524288 = A on OUT1
                1048576 = A on OUT2
            Example: If you want all values to report, sum the value identifiers together for Voltage + Current + Watt + kWh (Total, OUT1, OUT2). 1 + 2 + 4 + 8 + 256 + 512 + 2048 + 4096 + 65536 + 131072 + 524288 + 1048576 = 1776399, therefore entering 15 into this setting will give you Voltage + Current + Watt + kWh (Total, OUT1, OUT2) reports if set.
            Range: 0~1776399
            Default: 0 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="4" index="111" label="111 (Group 1) Set Report in Seconds" min="1" max="2147483647" value="240" setting_type="zwave" fw="">
        <Help>
       		Set the interval of automatic report for Report group 1 in (seconds). This controls (Group 1) Timed Automatic Reports.
            Range: 0~2147483647
            Default: 240 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="4" index="112" label="112 (Group 2) Set Report in Seconds" min="1" max="2147483647" value="3600" setting_type="zwave" fw="">
        <Help>
       		Set the interval of automatic report for Report group 2 in (seconds). This controls (Group 2) Timed Automatic Reports.
            Range: 0~2147483647
            Default: 3600 (Previous State)
        </Help>
      </Value>
      <Value type="number" byteSize="4" index="113" label="113 (Group 3) Set Report in Seconds" min="1" max="2147483647" value="3600" setting_type="zwave" fw="">
        <Help>
       		Set the interval of automatic report for Report group 3 in (seconds). This controls (Group 3) Timed Automatic Reports.
            Range: 0~2147483647
            Default: 3600 (Previous State)
        </Help>
      </Value>
      <Value type="list" byteSize="1" index="120" label="120 External Switch S1 Setting" min="0" max="4" value="0" setting_type="zwave" fw="">
        <Help>
        Configure the external switch mode for S1 via Configuration Set.
            0 = Unidentified mode.
            1 = 2-state switch mode.
            2 = 3-way switch mode.
            3 = momentary switch button mode.
            4 = Enter automatic identification mode. //can enter this mode by tapping internal button 4x times within 2 seconds.
            Note: When the mode is determined, this mode value will not be reset after exclusion.
            Range: 0~4
            Default: 0 (Previous State)
        </Help>
            <Item label="Unidentified" value="0" />
            <Item label="2-State Switch Mode" value="1" />
            <Item label="3-way Switch Mode" value="2" />
            <Item label="Momentary Push Button Mode" value="3" />
            <Item label="Automatic Identification" value="4" />
      </Value>
      <Value type="list" byteSize="1" index="121" label="121 External Switch S2 Setting" min="0" max="4" value="0" setting_type="zwave" fw="">
        <Help>
        Configure the external switch mode for S2 via Configuration Set.
            0 = Unidentified mode.
            1 = 2-state switch mode.
            2 = 3-way switch mode.
            3 = momentary switch button mode.
            4 = Enter automatic identification mode. //can enter this mode by tapping internal button 6x times within 2 seconds.
            Note: When the mode is determined, this mode value will not be reset after exclusion.
            Range: 0~4
            Default: 0 (Previous State)
        </Help>
            <Item label="Unidentified" value="0" />
            <Item label="2-State Switch Mode" value="1" />
            <Item label="3-way Switch Mode" value="2" />
            <Item label="Momentary Push Button Mode" value="3" />
            <Item label="Automatic Identification" value="4" />
      </Value>
</configuration>
'''
}

//For Later: 83-87, 123



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
                    if(it instanceof physicalgraph.zwave.Command){
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

