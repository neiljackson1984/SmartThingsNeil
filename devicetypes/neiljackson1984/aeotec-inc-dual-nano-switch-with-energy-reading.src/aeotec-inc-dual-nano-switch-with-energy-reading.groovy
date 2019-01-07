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

//if you leave arg blank, all config parameters and association groups will be reconciled.
// alternatively, you can pass a 'command' argument, which, if it is either a zwave configuration report command or a zwave association report command,
// will cause us to only attempt to set the particular configuration parameter or assocation group that the report refers to. (although we will still walk through 
// preferredDeviceConfiguration in order to keep the deviceConfigurationMatchesPreferredConfiguration and deviceConfigurationItemsConflicting attributes consistent.
public void reconcileDeviceConfiguration(Map arg = [:]){
    log.debug "attempting to reconcile deviceConfiguration..."
    def deviceConfigurationMatchesPreferredConfiguration = true;
    // def deviceConfigurationItemsConflicting = device.currentState('deviceConfigurationItemsConflicting')?.getJsonValue() ?: ['configurationParameters':[], 'associationLists':[]];
    // def deviceConfigurationItemsConflicting = ['configurationParameters':[], 'associationLists':[]];
    def deviceConfigurationItemsConflicting = [ : ];
    def settingCommandsToSend = [];
    def gettingCommandsToSend = [];
    def preferredDeviceConfiguration = getPreferredDeviceConfiguration();
    
    preferredDeviceConfiguration.configurationParameters.each{
        if(getDeviceConfiguration_configurationParameters(it.key) != it.value)
        {
            //record the conflict
            deviceConfigurationMatchesPreferredConfiguration = false;
            if(!deviceConfigurationItemsConflicting.configurationParameters){deviceConfigurationItemsConflicting.configurationParameters = [];} 
            deviceConfigurationItemsConflicting.configurationParameters << it.key;
            
            //attempt to fix the conflict
            if(!arg.command || ((arg.command instanceof physicalgraph.zwave.commands.configurationv1.ConfigurationReport) && arg.command.parameterNumber == it.key))
            {
                settingCommandsToSend << new physicalgraph.zwave.commands.configurationv1.ConfigurationSet(parameterNumber: it.key, configurationValue: it.value).format();
                gettingCommandsToSend << new physicalgraph.zwave.commands.configurationv1.ConfigurationGet(parameterNumber: it.key).format();
            };
        }
    }
    
    preferredDeviceConfiguration.associationLists.each{
        if(getDeviceConfiguration_associationLists(it.key) != it.value)
        {
            //record the conflict
            deviceConfigurationMatchesPreferredConfiguration = false;
            if(!deviceConfigurationItemsConflicting.associationLists){deviceConfigurationItemsConflicting.associationLists = [];} 
            deviceConfigurationItemsConflicting.associationLists << it.key;
            
            
            //attempt to fix the conflict
            if(!arg.command || ((arg.command instanceof physicalgraph.zwave.commands.associationv2.AssociationReport) && arg.command.groupingIdentifier == it.key))
            {
                def actualNodeList = getDeviceConfiguration_associationLists(it.key) ?:[];
                def preferredNodeList = it.value;
                
                def nodesToAdd = preferredNodeList - actualNodeList;
                def nodesToRemove = actualNodeList - preferredNodeList;

                nodesToRemove.each{nodeId -> settingCommandsToSend << new physicalgraph.zwave.commands.associationv2.AssociationRemove(groupingIdentifier: it.key, nodeId: nodeId).format();}
                nodesToAdd.each{nodeId -> settingCommandsToSend << new physicalgraph.zwave.commands.associationv2.AssociationSet(groupingIdentifier: it.key, nodeId: nodeId).format();}
                gettingCommandsToSend << new physicalgraph.zwave.commands.associationv2.AssociationGet(groupingIdentifier: it.key).format();
            }
        }
    }
    sendEvent(name:'deviceConfigurationMatchesPreferredConfiguration', value: deviceConfigurationMatchesPreferredConfiguration);
    
    if(!deviceConfigurationMatchesPreferredConfiguration){
        runIn(20, reconcileDeviceConfiguration, [overwrite: true]);
        //this runIn (with overwrite true) ensures that whenever there is a mismatch between actual and preferred device configuration, we will schedule 
        // a run of reconcileDeviceConfiguration() in 20 seconds.  This will guard against failing to realize that a configuration paramter has been properly set 
        // due to multiple simultaneous executions of the device handler in response to configuration report commands.
        // the overwrite: true ensures that when we are trying  to set a bunch of configuration parameters all at once, only one final running of reconcileDeviceConfiguration() wiill occur.
    }
    
    sendEvent(name:'deviceConfigurationItemsConflicting', value: deviceConfigurationItemsConflicting);
    sendEvent(name:'numberOfConflictingConfigurationItems', value: 
        (deviceConfigurationItemsConflicting.configurationParameters?.size() ?: 0) + 
        (deviceConfigurationItemsConflicting.associationLists?.size() ?: 0)
    );
    sendZwaveCommands(settingCommandsToSend);
    //in the case where the setting of the configuration parameter fails to change the configuration parameter to the preferred value, there will be an infinite 
    // repetition of attempts to set the configuration parameter.  We want to limit the rate of this repetition- therefore we wait 15 seonds before 
    // sending the commands to read the configuration paramter (the response to whcih will trigger the next iteration ofthe attempt to set the parameter).  that is why the 
    // below csending of the gettingCommandsToSend (i.e. the commands that will 'get' the value of the configuration parameter) is wrapped in the runIn(15,...) function.
    //  The reason for allowing the 'command' argument to be passed in to this function, which restricts our attempts at fixing the conflict to to only one particular 
    // parameter, as specified in the ;command' argument, is that we do not want toattempt to fix parameters that we have already have attempted to fix, but have not yet 
    // heard back from the device that the fix has taken effect.  If we did not restrict the fixing attempts to one paarticular parameter, we could get into an infinite loop
    // where, for instance, 
    // we send commands to the device to set and report back on parameters 1 and 2.
    // the device responds with a report on parameter 1 (up to relabeling) (probably on the first one that we attempted to set, but the device might send the responses in any order
    // (and the smarththings platform might permute the order in the process of delivering the the responses to this device handler)
    // in response to the response, we update state.deviceConfiguration and then invoke reconcileDeviceConfiguration();   If we did not restrict reconcileDeviceConfiguration()'s
    // fixing attempt to only the parameter that the device just reported on, reconcileDeviceConfiguration() would, believing that parameter 2 is still conflicted, attempt to fix parameter 2 again.
    // In the best case, we end up sending a lot more zwave commands than is necessary (this inneficency gets worse as the number of parameters increases), and clutter the airwaves (and cause the reconciliation
    // to take longer than necessary..
    // In the worst case (where the device defies expectation and reports that the value of the parameter that we attempted to set is something other than the value we tried to set it to),
    // we could end up getting hung-up in a loop trying to fix one unfixable conflicting parameter and neglect all the rest.
    // To avoid such problems, we allow the function to receive a 'command' argument which, if present, is expectted to be a configuration report or an association list report,
    // and, if present, will cause reconcileDeviceConfiguration() to send setting and getting zwave commands only for the parameter or association list related to the 'command' argument.
    
    if(gettingCommandsToSend)
    {
        def waitingPeriod = 5 + Math.round(15*Math.random());
        log.debug "waiting ${waitingPeriod} seconds before issuing the getting commands.";
        runIn(waitingPeriod, sendZwaveCommands, [data: [commands: delayBetween(gettingCommandsToSend, 2000)], overwrite: false]);
    }
    
    //what will happen in the case where we have sent out zwave setting commands to set configuration paramters 1 and 2, and we are now waiting for the getting commands to be sent out and for the device toCcNames
    // respond.  Some time later, one (or maybe both) of the getting cmmands has been sent to the device, and we receive the device's response reporting
    // on the new value for parameter 1 (and let us supose that the new value is the correct one, the one that matches the preferred value) .  During the execution of the device handler
    // that is handling the report about paramter 1, the report about parameter 2 arrives and the platform starts up a second execution of the device handler in response to that zwave commmand
    // before the first execution has had a chance to update stat.deviceConfiguration and finish running.  The second execution would leave the deviceConfigurationMatchesPreferredConfiguration 
   // attribute set to false.  the first command would leave that attribute set to false as well.  By the time both executions had finished, the deviceConfigurationMatchesPreferredConfiguration
   // attribute would remain false, even though all the device's onboard configuration parameters do match the preferrences, and the device has sent reports to indicate this.
   // How can we guard against such a possibility.  Is some sort of semaphore system, possibly involving atomicState, in order?  Alternatively, perhaps we should take pains to ensure that
   // we wait a good long time between sending 'get' commands, to give the response long enough to propoagate back through the system and to alow time for everything to equilibrate
   // before the next 'get' command is sent.
   
   // I know that the smartthings platform is pretty goood about syncing changes in the top-level of the structure of the state object.  For example, one execution can set state.a , and another execution 
   // can set state.b, and even if the two executions run simultanously, both of the executions' changes to state end up in the final state object.  Does this simultanesous change-syncing
   // system extend to levels of the state hierarchy below the first level.  For instance, if one execution where attempting to set state.a.a, and another execution were simultaneously setting state.a.b,
  // would both of the executions' changes to state end up in the final value of state.  I suspect that the answer is 'no', because the values of the state entries are serialized to strings.  I suspect that 
  // the serialization occurrs at the first level of the hierarchy, and the deeper levels of the hierarchy are not part of the change-syncing algorithm.
}


private Map getPreferredDeviceConfiguration(){
   /*
    This function constructs (and returns) a map of the same form as the state.deviceConfiguration 
    map that we are maintaining elsewhere in this smart app.  The map that this function returns is
    based entirely on the user preferences, as stored in the global settings object.
   */
   
   
   def preferredDeviceConfiguration = [configurationParameters: [:],associationLists: [:]];
   //I trust that this locally-decalred preferredDeviceConfiguration will override the groovy/smartthings magic getter 
   // system that would otherwise 
   // translate 'preferredDeviceconfiguration' to 'getPreferredDeviceConfiguration()'
   // preferredDeviceconfiguration is, within this function body, the map that we are constructing.
   getConfigurationModel().each{
       it.value.apply(getSetting(it.key), preferredDeviceConfiguration);
   }
   
   return preferredDeviceConfiguration;
}
    
private getSetting(nameOfSetting){
    return settings?.containsKey(nameOfSetting) ? settings[nameOfSetting] : getDefaultSettings()[nameOfSetting];
}

private Map getDefaultSettings(){
    def defaultSettings =  [:];
    
    //fill in all the default settings that are declared in the configurationModel.
    configurationModel.each{defaultSettings[it.key] = it.value.defaultValue};
    
    //explicitly declare any other default settings
    // it is expected that the default settings that you explicitly declare here are not directly 
    // related to the device's internal non-volatile settings.
    //defaultSettings['foo'          :    15];
    
    return defaultSettings;
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
        
        attribute("deviceConfigurationMatchesPreferredConfiguration", "boolean"); //technically, there is no attribute type called "boolean".  In practice, declaring an attribute type as "boolean" is equivalent to "string"
        attribute("deviceConfigurationItemsConflicting", "string"); 
        attribute("numberOfConflictingConfigurationItems", "number"); 

        
        //fingerprint mfr: "0086", model: "0084" // Aeon brand
        //inClusters:"0x5E,0x25,0x27,0x32,0x81,0x71,0x60,0x8E,0x2C,0x2B,0x70,0x86,0x72,0x73,0x85,0x59,0x98,0x7A,0x5A"
        
        //something about the above fingerprint and inClusters() call is causing the IDE to throw a jdbc hibernation error upon trying to publish.  commenting the above two lines out seems to fix the problem (which is an acceptable solution for testing)
    }

    simulator {
    }

    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph"//, element: "paragraph"
        generatePreferences();
        // generate_preferences(configuration_model())
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
        standardTile("configure", "device.deviceConfigurationMatchesPreferredConfiguration", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "true" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "false", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
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
            def filteredStackTrace = stackTraceItems.findAll{ it['fileName']?.startsWith("script_") }.init();  //The init() method returns all but the last element.

            
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
        
        return render(
            contentType: "text/html", 
            data: debugMessage += "\n",
            status: 200
        );
    }
}

def prettyPrint(x)
{
    return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(x));
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

def mainTestCode2(){
    log.debug "mainTestCode() was run";
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";

    // // updateDataValue("ahoy", "everyone")
    
    // debugMessage += "data: " + data + "\n";
    // debugMessage += "state: " + state + "\n";
    // //state['ahoy'] = "foo";

    // // debugMessage += "device.getManufacturerName(): " + device.getManufacturerName() + "\n";
    // // debugMessage += "device.getModelName(): " + device.getModelName() + "\n";
    // debugMessage += "device.getProperties(): " + device.getProperties() + "\n";
    // // debugMessage += "device.getMethods(): " + device.getMethods() + "\n";
    // debugMessage += "getDataValue('ahoy'): " + getDataValue('ahoy') + "\n";
    // debugMessage += "this: " + this + "\n";
    // debugMessage += "this.getProperties(): " + this.getProperties() + "\n";
    // // debugMessage += "getDataValues(): " + getDataValues() + "\n";
    // // debugMessage += new groovy.inspect.Inspector(this).methodInfo("getDataValue") + "\n";
    // // updateDataValue("ahoy", null);
    // // deleteDataValue("ahoy");
    //data = [:];
    return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}

def mainTestCode3(){
    log.debug "mainTestCode() was run";
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";

    // def preferenceValue;
    // preferenceValue = getSetting('time1');
    // // preferenceValue = "2015-01-09T15:50:32.000-0600";
    // // preferenceValue = (new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).format(new Date());
    
    // debugMessage += "preferenceValue: " + preferenceValue + "\n";
    // debugMessage += "preferenceValue.getProperties()['class']: " + preferenceValue.getProperties()['class']    + "\n";
    // Date theDate = preferenceTimeStringToDate(preferenceValue); 
    // java.util.GregorianCalendar theCalendar = new java.util.GregorianCalendar();
    // //theCalendar.setTimeZone(location.getTimeZone());
    // theCalendar.setTime(theDate);
    // debugMessage += "theCalendar.get(Calendar.HOUR_OF_DAY): " + theCalendar.get(Calendar.HOUR_OF_DAY) + "\n";
    // debugMessage += "theCalendar.get(Calendar.MINUTE): " + theCalendar.get(Calendar.MINUTE) + "\n";
    
    // def deviceConfiguration = ['configurationParameters': [:],'associationLists': [:]];
    // configurationModel.find{it.name=="led nightlight turn-on time"}.apply(getSetting('time1'),deviceConfiguration);
    // configurationModel["led nightlight turn-on time"].apply(getSetting('time1'),deviceConfiguration);
    // debugMessage += "deviceConfiguration: " + deviceConfiguration + "\n";
    //debugMessage += "defaultSettings: " + prettyPrint(defaultSettings) + "\n";
    // debugMessage += "configurationModel.find{it.name=='led nightlight turn-on time'}.apply: " + configurationModel.find{it.name=='led nightlight turn-on time'}.apply + "\n";
    debugMessage += "preferredDeviceConfiguration: " + prettyPrint(preferredDeviceConfiguration) + "\n";
    // debugMessage += "getSetting('led nightlight turn-on time'): " + getSetting('led nightlight turn-on time') + "\n";
    // // debugMessage += "state: " + prettyPrint(state) + "\n";
    // // debugMessage += "getSetting('4'): " + getSetting('4').inspect() + "\n";
    // // debugMessage += "getSetting('4').getProperties()['class']: " + getSetting('4').getProperties()['class'] + "\n";
    
    // def myMap = [
        // 0: "disabled",
        // 1: "enabled"
    // ];
    
    // // debugMessage += "myMap.find{it.value == 'enabled'}: " + myMap.find{it.value == 'enabled'}.inspect() + "\n";
    // // debugMessage += "myMap.find{it.value == 'enabled'}.getProperties()['class']: " + myMap.find{it.value == 'enabled'}.getProperties()['class'] + "\n";
    // // debugMessage += "myMap.find{it.value == 'enabled'}.key: " + myMap.find{it.value == 'enabled'}.key + "\n";
    // // debugMessage += "myMap.find{it.value == 'enabled'}.value: " + myMap.find{it.value == 'enabled'}.value + "\n";
    // debugMessage += "myMap.values(): " + myMap.values() + "\n";
    // // debugMessage += "myMap.keys(): " + myMap.keys() + "\n";
    
    // // debugMessage += "devices: " + devices + "\n";
    // // debugMessage += "getSetting('aaa')" + getSetting('aaa').inspect() + "\n";
    // // debugMessage += "getSetting('aaa').getProperties()['class']" + getSetting('aaa').getProperties()['class'] + "\n";
    // // debugMessage += "location.getProperties()['class']: " + location.getProperties()['class'] + "\n";
    // // debugMessage += "location.getProperties(): " + location.getProperties() + "\n";
    // // debugMessage += "location.helloHome.getProperties(): " + location.helloHome.getProperties() + "\n";
    // // debugMessage += "location.helloHome.getProperties()['id']: " + location.helloHome.getProperties()['id'] + "\n";
    // // debugMessage += "location.helloHome.app.getProperties()['id']: " + location.helloHome.app.getProperties()['id'] + "\n";
    // // debugMessage += "location.helloHome.parent.getProperties(): " + location.helloHome.parent.getProperties() + "\n";
    // // debugMessage += "location.helloHome.parent.app.getProperties(): " + location.helloHome.parent.app.getProperties() + "\n";
    // // debugMessage += "this.getProperties(): " + this.getProperties() + "\n";
    
    // // [
        // // log:null, 
        // // deviceSvc:null, 
        // // incidentService:null, 
        // // incidentClipService:null, 
        // // currentMode:Home, 
        // // locationSvc:null, 
        // // id:5181c463-0e76-4061-b263-d04ec3a693c6, 
        // // longitude:-122.38228600, 
        // // allHubs:[Neil's condo Hub, Virtual Hub], 
        // // eventSvc:null, 
        // // installedSmartApp:null, 
        // // location:Neil's condo, 
        // // name:Neil's condo, 
        // // modes:[Away, Night, Home], 
        // // timeZone:sun.util.calendar.ZoneInfo[id="America/Los_Angeles",offset=-28800000,dstSavings=3600000,useDaylight=true,transitions=185,lastRule=java.util.SimpleTimeZone[id=America/Los_Angeles,offset=-28800000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=3,startMonth=2,startDay=8,startDayOfWeek=1,startTime=7200000,startTimeMode=0,endMode=3,endMonth=10,endDay=1,endDayOfWeek=1,endTime=7200000,endTimeMode=0]], 
        // // preferenceTypeConversionSvc:null, 
        // // class:class physicalgraph.app.LocationWrapper, 
        // // mode:Home, 
        // // contactBookEnabled:false, 
        // // version:351, 
        // // zipCode:98119, 
        // // temperatureScale:F, 
        // // country:USA, 
        // // channelName:wwst, 
        // // hubs:[Neil's condo Hub], 
        // // latitude:47.64677400, 
        // // helloHome:physicalgraph.app.InstalledSmartAppWrapper@5396f4ef, 
        // // installedSmartAppSvc:null
        // // ]
        
    // // location.helloHome.getProperties(): [
        // // showEasingCards:null, 
        // // incidentService:null, 
        // // executorBase:null, 
        // // _isParent:false, 
        // // locationId:5181c463-0e76-4061-b263-d04ec3a693c6, 
        // // id:04019369-dc3a-4eb5-bef8-5887ecbae286, 
        // // eventService:null, 
        // // deviceService:null, 
        // // allChildApps:[
            // // physicalgraph.app.InstalledSmartAppWrapper@55d9c4ff, 
            // // physicalgraph.app.InstalledSmartAppWrapper@4c146d61, 
            // // physicalgraph.app.InstalledSmartAppWrapper@5119224c, 
            // // physicalgraph.app.InstalledSmartAppWrapper@491ad52c, 
            // // physicalgraph.app.InstalledSmartAppWrapper@a16133, 
            // // physicalgraph.app.InstalledSmartAppWrapper@62f45b97, 
            // // physicalgraph.app.InstalledSmartAppWrapper@18a94942
        // // ], 
        // // namespace:Hello Home, 
        // // class:class physicalgraph.app.InstalledSmartAppWrapper, 
        // // executionIsModeRestricted:false, 
        // // installedSmartAppService:null, 
        // // moduleId:null, 
        // // appSettings:[:], 
        // // callerExecutionContext:null, 
        // // incidentClipService:null, 
        // // app:physicalgraph.app.InstalledSmartAppWrapper@120898b4, 
        // // eventSubscriptionService:null, 
        // // virtualDevices:[], 
        // // preferenceTypeConversionService:null, 
        // // installedSmartApp:Hello Home, 
        // // name:Hello Home, 
        // // childDevices:[], 
        // // subscriptions:[
            // // physicalgraph.app.EventSubscriptionWrapper@72615aab
        // // ], 
        // // executionContext:null, 
        // // accountId:8bf7214f-9cb6-4a11-8f1e-169d83ce8e8c, 
        // // serverConfigService:null, 
        // // installationState:COMPLETE, 
        // // label:Hello Home, 
        // // smartAppId:247d3c26-6c0a-4f67-83c5-956cbc275cec, 
        // // executableModes:[], 
        // // root:physicalgraph.app.InstalledSmartAppWrapper@5587cb37, 
        // // parent:physicalgraph.app.InstalledSmartAppWrapper@462eb3bf
    // // ]

    // // location.helloHome.parent.getProperties(): [
        // // incidentService:null, 
        // // executorBase:null, 
        // // _isParent:true, 
        // // id:null, 
        // // inactiveIncidents:[], 
        // // eventService:null, 
        // // deviceService:null, 
        // // class:class physicalgraph.app.InstalledSmartAppWrapper, 
        // // installedSmartAppService:null, 
        // // incidents:[], 
        // // callerExecutionContext:null, 
        // // incidentClipService:null, 
        // // app:physicalgraph.app.InstalledSmartAppWrapper@32c9509b, 
        // // eventSubscriptionService:null, 
        // // preferenceTypeConversionService:null, 
        // // installedSmartApp:null, 
        // // executionContext:null, 
        // // serverConfigService:null, 
        // // label:null, 
        // // parent:physicalgraph.app.InstalledSmartAppWrapper@3ecb0a29, 
        // // activeIncidents:[]
    // // ]      
    
    // def bitNumber = 1;
    // def currentValue = 0;
    // def theSetting = true;
    // def newValue = ((currentValue ?: 0) & ~(1 << bitNumber)) | ((theSetting ? 1 : 0) << bitNumber);
    
    // debugMessage += "getSetting('scheduled turn-on Monday'): " + getSetting('scheduled turn-on Monday').inspect() + "\n";

    // debugMessage += "settings['scheduled turn-on Monday']: " + settings['scheduled turn-on Monday'].inspect() + "\n";
    // debugMessage += "newValue: " + newValue.inspect() + "\n";
    // debugMessage += "getSetting('bbb'): " + getSetting('bbb').inspect() + "\n";
    // debugMessage += "bbb: " + bbb.inspect() + "\n";
    // debugMessage += "5.toString(): " + 5.toString() + "\n";
    // debugMessage += "((int) 5).toString(): " + ((int) 5).toString() + "\n";
    // debugMessage += "((Integer) 5).toString(): " + ((Integer) 5).toString() + "\n";
    
    return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}

def mainTestCode4(){
    def startTime = now();
    log.debug "mainTestCode() was run";
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";

    // debugMessage += "preferredDeviceConfiguration: " + prettyPrint(preferredDeviceConfiguration) + "\n";
    
    // def a = ([1] * 3);
    // def b = [1,1,1];
    
    // debugMessage += "a.getProperties()['class']: " + a.getProperties()['class'] + "\n";
    // debugMessage += "b.getProperties()['class']: " + b.getProperties()['class'] + "\n";
    
    // debugMessage += "it is : " + ([1] * 3) +  "\n";
    
    // debugMessage += "preferredDeviceConfiguration: " + prettyPrint(preferredDeviceConfiguration) + "\n";
    // debugMessage += "device.getProperties(): " + device.getProperties() + "\n";
    // debugMessage += "device.rawDescription: " + device.rawDescription + "\n";
    // debugMessage += "zwaveInfo: " + zwaveInfo + "\n";
    // // debugMessage += "zwave: " + zwave + "\n";
    // // debugMessage += "zwave.getProperties(): " + zwave.getProperties() + "\n";
    // // debugMessage += "zwave.getProperties(): " + prettyPrint(zwave.getProperties().collectEntries{it.value = "" + it.value; it}) + "\n";
    // // debugMessage += "zwave.basicV1.getProperties(): " + prettyPrint(zwave.basicV1.getProperties().collectEntries{it.value = "" + it.value; it}) + "\n";
    // debugMessage += "zwaveInfo.cc: " + prettyPrint(zwaveInfo.cc.collect{"(0x${it}) "  + commandClassNames[evaluate("0x${it}")]}) + "\n";
    // debugMessage += "zwaveInfo.ccOut: " + prettyPrint(zwaveInfo.ccOut.collect{"(0x${it}) "  + commandClassNames[evaluate("0x${it}")]}) + "\n";
    // debugMessage += "zwaveInfo.endpointInfo[0].cc: " + prettyPrint(zwaveInfo.endpointInfo[0].cc.collect{"(0x${it}) "  + commandClassNames[evaluate("0x${it}")]}) + "\n";
    // debugMessage += "zwaveHubNodeId: " + zwaveHubNodeId + "\n";
    // debugMessage += "'45'.toInteger(): " + '45'.toInteger() + "\n";
    // debugMessage += "' 45 '.toInteger(): " + ' 45 '.toInteger() + "\n";
    // // debugMessage += "'0x45.toInteger(): " + '0x45'.toInteger() + "\n";
    
    // sendEvent(name:'a', value:true);
    // sendEvent(name:'b', value:true);
    //def startTime = now();
    //def p = preferredDeviceConfiguration;
    //def endTime = now();
    //debugMessage += "time required to compute preferredDeviceConfiguration: " +  ((endTime - startTime)/1000) + "seconds" + "\n";
    //debugMessage += "p: " + prettyPrint(p) + "\n";
    // //debugMessage += "state: " + prettyPrint(state) + "\n";
    // def x = [1:'a', 2:'b', 3:'c'];
    // def y = [1:99, 2:98, 3:97];
    // //state.clear();
    // if(!state['x']){state['x'] = x;}
    // if(!state['y']){state['y'] = y;}
    // debugMessage += "state['x']: " + state['x'].inspect() + "\n";
    // debugMessage += "state['y']: " + state['y'].inspect() + "\n";
    // debugMessage += "x: " + x.inspect() + "\n";
    // debugMessage += "y: " + y.inspect() + "\n";
    // state.clear();
    //each state entry is persisted as a json string.  therefore, the keys in the maps are necessarily strings.  
    // attempting to invoke .get(x) on a state value, where x is an Integer, for instance, causes an excception to thrown complaining that a string cannot be cast to an integer.
    //The fix is to ensure that we only ever get or set map values within a state value using a string as a key.
    // debugMessage += "before clearing, state: " + prettyPrint(state) + "\n";
    //debugMessage += "before clearing, state: " + state + "\n";
    //state.clear();
    //debugMessage += "after clearing, state: " + state + "\n";
    // debugMessage += "state: " + prettyPrint(state) + "\n";
    state.clear();
    reconcileDeviceConfiguration();
    // debugMessage += "state: " + prettyPrint(state) + "\n";

    // debugMessage += "(getSetting).getProperties()['class']: " + (getSetting).getProperties()['class'] + "\n";
    // debugMessage += "(convertParam).getProperties()['class']: " + (convertParam).getProperties()['class'] + "\n";
    // debugMessage += "on.getProperties()['class']: " + on.getProperties()['class'] + "\n";
    // debugMessage += "device.on.getProperties()['class']: " + device.on.getProperties()['class'] + "\n";
    // runIn(3, runIt, [data:[nameOfFunction:"callback", argument: "ahoy"], overwrite: false]) 
    // runIn(3, runIt, [data:[nameOfFunction:"log.debug", argument: "ahoy"], overwrite: false]) 
    //it looks like runIn serializes data, and expects a string as the second argument . (function symbols get converted into strings somehow by the interpreter)
   //alos runIn expects the data option to be a map --- i tried setting data to be an Integer,
   // and the platform passsed null as the argument to the callback.
    
    
    
    def endTime = now();
    debugMessage += "runtime of mainTestCode(): " + ((endTime - startTime)/1000) + " seconds" + "\n";
    return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}


def mainTestCode(){
    log.debug "mainTestCode() was run";
    def debugMessage = ""
    debugMessage += "\n\n" + "================================================" + "\n";
    debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";
    // state.deviceConfiguration.configurationParameters.each{debugMessage += "key: " + it.key.inspect() + "\n" };
    
    // debugMessage += "state.deviceConfiguration: " + prettyPrint(
        // state.deviceConfiguration.collectEntries{it.value = it.value.sort{x -> x.key.toInteger() }; return it;}
    // ) + "\n";
    // def preferredDeviceConfiguration =  getPreferredDeviceConfiguration();
    // debugMessage += "preferredDeviceConfiguration: " + prettyPrint(preferredDeviceConfiguration) + "\n";

    // debugMessage += "state.deviceConfiguration.getProperties()['class']: " + state.deviceConfiguration.getProperties()['class'] + "\n";
    //debugMessage += "state.deviceConfiguration.getAt(0).getProperties()['class']: " + state.deviceConfiguration.getAt(0).getProperties()['class'] + "\n";
   //state.clear();
   
   //fetchDeviceConfiguration();
   //fetchDeviceConfiguration(fetchAll:true);
   //reconcileDeviceConfiguration();
   // debugMessage += "getSetting('association group 1 members'): " + getSetting('association group 1 members') + "\n";
   // debugMessage += "settings " + settings + "\n";
   
   // debugMessage += "getDeviceConfiguration_configurationParameters(): " +  getDeviceConfiguration_configurationParameters() + "\n";
   //debugMessage += "state: " +  prettyPrint(state) + "\n";
   debugMessage += "getDeviceConfiguration(): " +  prettyPrint(getDeviceConfiguration()) + "\n";
   
   
    return  render( contentType: "text/html", data: debugMessage  + "\n", status: 200);
}

def setDeviceConfiguration_configurationParameters(Integer parameterNumber, value)
{
    state['deviceConfiguration.configurationParameters.' + parameterNumber.toString()] = value;
    return value;
}

def getDeviceConfiguration_configurationParameters(Integer parameterNumber)
{
    return state['deviceConfiguration.configurationParameters.' + parameterNumber.toString()];
}

def getDeviceConfiguration_configurationParameters()
{
    def configurationParameters = [:];
    state.each{
        def myMatcher = (it.key =~ /deviceConfiguration.configurationParameters.(\d+)/);
        if(myMatcher){
            configurationParameters[myMatcher[0][1].toInteger()] = it.value;
        }
    }
    
    return configurationParameters.sort();
}


def setDeviceConfiguration_associationLists(Integer groupingIdentifier, value)
{
    state['deviceConfiguration.associationLists.' + groupingIdentifier.toString()] = value;
    return value;
}

def getDeviceConfiguration_associationLists(Integer groupingIdentifier)
{
    return state['deviceConfiguration.associationLists.' + groupingIdentifier.toString()];
}


def getDeviceConfiguration_associationLists()
{
    def associationLists = [:];
    state.each{
        def myMatcher = (it.key =~ /deviceConfiguration.associationLists.(\d+)/);
        if(myMatcher){
            associationLists[myMatcher[0][1].toInteger()] = it.value;
        }
    }
    
    return associationLists.sort();
}

def getDeviceConfiguration()
{
    return [
        'associationLists' : getDeviceConfiguration_associationLists() ,
        'configurationParameters' : getDeviceConfiguration_configurationParameters()
    ];
}


void runIt(arg)
{
    log.debug "runIt(${arg}) was called.";
    this."${arg['nameOfFunction']}"(arg.argument)
    // arg['x']();
}

private void callback(arg)
{
    log.debug "callback(${arg}) was called.";
}

//converts the string that the smartthings plpatform stores as the value of a preference input of type "time" into a Date object.
Date preferenceTimeStringToDate(String preferenceTimeString){
    return (new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).parse(preferenceTimeString);
}

//this function needs to be public rather than private, so that I can invoke it by passing the function name 'sendZwaveCommands' to runIn();
public void sendZwaveCommands(Map arg){
    // log.debug "sendZwaveCommands was called with a map as the argument: ${arg}."
    sendZwaveCommands(arg.commands);
}

public void sendZwaveCommands(List commands){
    //commands is expected to be a list, each element of which is either a string or a zwave command object.
    // we want to allow that the elements are strings so that we can pass in formatted commands (which are strings), delays (which are strings (for instance "delay 100")), or zwave command objects.
    if(commands)
    {
        def formattedCommands = commands.collect{ it instanceof physicalgraph.zwave.Command ? command(it) : it };
        if(formattedCommands){
            logZwaveCommandFromHubToDevice(formattedCommands);
            sendHubCommand([response(formattedCommands)]); 
        }
    }
}


def parse(String description) {
    def result = []
    def cmd = zwave.parse(description, commandClassVersionMap)
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

    //sendZwaveCommands(commands(cmds));
    
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "receive a configuration report"
    //update_current_properties(cmd)
    //if(!state.deviceConfiguration){state.deviceConfiguration = [:]};
    //if(!state.deviceConfiguration.configurationParameters){state.deviceConfiguration.configurationParameters = [:]};
    
    setDeviceConfiguration_configurationParameters(cmd.parameterNumber, cmd.configurationValue);
    

    reconcileDeviceConfiguration(command:cmd);
    
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'", 2)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    log.debug "receive an association report"
    //if(!state.deviceConfiguration){state.deviceConfiguration = [:]};
    //if(!state.deviceConfiguration.associationLists){state.deviceConfiguration.associationLists = [:]};
    
    setDeviceConfiguration_associationLists(cmd.groupingIdentifier, cmd.nodeId);
    reconcileDeviceConfiguration(command:cmd);
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
    fetchDeviceConfiguration();   
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
    // def cmds = []
    // cmds = update_needed_settings()
    // if (cmds != []) sendZwaveCommands(commands(cmds))
    fetchDeviceConfiguration();   
    reconcileDeviceConfiguration();
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
    //def cmds = []
    // cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    // sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    // if (cmds != []) response(commands(cmds))
    // if (cmds != []) sendZwaveCommands(commands(cmds));
    reconcileDeviceConfiguration();
}

//sends z-wave get commands for all configuration parameters and all association lists
def fetchDeviceConfiguration(arg = [:]) {

    
    //configuration parameter getting commands:
    sendZwaveCommands(
        delayBetween(
            (arg.fetchAll ? (0..255) : preferredDeviceConfiguration.configurationParameters.keySet()).collect{parameterNumber -> 
                new physicalgraph.zwave.commands.configurationv1.ConfigurationGet(parameterNumber: parameterNumber).format()
            } + 
            (1..4).collect{groupingIdentifier -> 
                new physicalgraph.zwave.commands.associationv2.AssociationGet(groupingIdentifier: groupingIdentifier).format()
            },
            2000
        )
    );
}

def xxxgenerate_preferences(configuration_model) {
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
def xxxupdate_current_properties(cmd) {
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
def xxxupdate_needed_settings() {
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
        case 0:
            0
            break
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
            1 = Momentary indicate mode. When the state of Switch?s load changed, the LED will follow the status (on/off) of its load, but the LED will turn off after 5 seconds if there is no any switch action.
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

// The getConfigurationModel() function is inspired by CyrilPeponnet's similar system
// getConfigurationModel() (which I am really using as a constant global variable that might be called 'configurationModel')
// is a single central manifest of information about the device's configuration parameters (exposed via the zwave CONFIGURATION command class.)
// and the device's association lists (exposed via the zwave ASSOCIATION command class.)
// AND the preferred default values (which is something that the writer of this device handler gets to specify -- not something that the manfuacturer of the device specifies.)

//the top-level structure of the configurationModel object is based on the logical parameters that can be set, rather than
// thge specifics of how those parameters are encoded in the zwave CONFIUGURATION parameters.  for instance, in the case where several conceptually independent 
// boolean settings happen to be encoded as bits in one byte of a zwave configuration parameter, those settings will appear as independent top-level items
// in the configurationModel object.

//roughly speaking, each of the top-level items should correspond to one 'input()' item in the smartthings preferences page.


private getConfigurationModel() {
    // using the delegate property of the 'apply' closures below is a bit tricky --
    //     it would be much cleaner to create a true class, but 
    // smartthings does not allow to create classes.

    Map configurationModel = [:];

    configurationModel['over-current protection']= [
        type: "enum",
        allowedValues: [
                0: "disabled",
                1: "enabled"
            ],
        defaultValue: "enabled",
        description: 
            "Output load will be turned off after 30 " + 
            "seconds if the current exceeds 10.5 amps.",
        apply: makeEnumApplicator(parameterNumber: 3)
    ];
    
    configurationModel['over-heat protection'] =  [
        type: "enum",
        allowedValues: [
                0: "disabled",
                1: "enabled"
            ],    
        defaultValue: "disabled",
        description: 
            "Output load will be turned off after 30 " + 
            "seconds if the temperature inside the " +
            "product exceeds 100 degrees C.",
        apply: makeEnumApplicator(parameterNumber: 4)   
    ];
    
    configurationModel['power-up behavior'] = [
        type: "enum",
        allowedValues: [
                0: "the last state before the power outage",
                1: "always on",
                2: "always off"
            ],
        defaultValue: "the last state before the power outage",
        description: "Configure the output load status after re-power on."    ,
        apply: makeEnumApplicator(parameterNumber: 20)
    ];
    
    configurationModel['notification sent to group 1'] = [ 
        type: "enum",
        allowedValues: [
                0: "nothing",
                1: "hail command class",
                2: "basic report command class",
                3: "hail when external switch used"
            ], 
        defaultValue: "hail when external switch used",
        description: 
            "To set which notification would be sent to the " +
            "associated nodes in association group 1 when the " + 
            "state of output load is changed.  Note: When just " + 
            "only one channel load state is changed, the report " + 
            "message Hail CC or Basic Report CC would be Multi " +
            "Channel encapsulated.",
        apply: makeEnumApplicator(parameterNumber: 80)
    ];
    
    configurationModel['notification sent to group 3'] = [
        type: "enum",
        allowedValues: [
                0: "nothing",
                1: "basic set"
            ],
        defaultValue: "basic set",
        description: 
            "To set which notification would be sent to the" + 
            "associated nodes in association group 3 when using " + 
            " the external switch 1 to switch the loads.",
        apply: makeEnumApplicator(parameterNumber: 81)
    ];
    
    configurationModel['notification sent to group 4'] = [
        type: "enum",
        allowedValues: [
                0: "nothing",
                1: "basic set"
            ],
        defaultValue: "basic set",
        description: 
            "To set which notification would be sent to the" + 
            " associated nodes in association group 4 when using" + 
            " the external switch 2 to switch the loads.",
        apply: makeEnumApplicator(parameterNumber: 82)
    ];
    
    configurationModel['led behavior'] = [
        type: "enum",
        allowedValues: [
                0: "mirror the output",
                1: "momentarily illuminate when output changes",
                2: "night-light mode"
            ],
        defaultValue: "mirror the output",
        description: "controls the behavior of the onboard led.",
        apply: makeEnumApplicator(parameterNumber: 83)
    ];

    //I am thinking about refactoring this so that it appears as two integer preferecne inputs rather than a "time" preference input.
    // This would be less likely to confuse the user with time zones.
    configurationModel['led nightlight turn-on time'] = [
        type: "time", //really a time of day.
        defaultValue: "2015-01-09T18:00:00.000-0000",
        description: "specify the time of day when you want the led to turn on (in the case where" +
            "the led behavior is set to night light mode.",
        apply: {value, deviceConfiguration ->
            def parameterNumber=84;
            Date theDate = preferenceTimeStringToDate(value);
            java.util.GregorianCalendar theCalendar = new java.util.GregorianCalendar();
            //theCalendar.setTimeZone(location.getTimeZone()); //we will let the time zone be the default, which is UTC+0, and we will set the onboard clock to UTC+0 time.
            theCalendar.setTime(theDate);
            if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [0] * zwaveConfigurationParameterSizes[parameterNumber];}
            deviceConfiguration.configurationParameters[parameterNumber][0] = theCalendar.get(Calendar.HOUR_OF_DAY);
            deviceConfiguration.configurationParameters[parameterNumber][1] = theCalendar.get(Calendar.MINUTE);
        }
    ];
    
    
    //I am thinking about refactoring this so that it appears as two integer preferecne inputs rather than a "time" preference input.
    // This would be less likely to confuse the user with time zones.
    configurationModel['led nightlight turn-off time'] = [
        type: "time", //really a time of day.
        defaultValue: "2015-01-09T08:00:00.000-0000",
        description: "specify the time of day when you want the led to turn off (in the case where" +
            "the led behavior is set to night light mode.",
        apply: {value, deviceConfiguration ->
            def parameterNumber=84;
            Date theDate = preferenceTimeStringToDate(value);
            java.util.GregorianCalendar theCalendar = new java.util.GregorianCalendar();
            //theCalendar.setTimeZone(location.getTimeZone()); //we will let the time zone be the default, which is UTC+0, and we will set the onboard clock to UTC+0 time.
            theCalendar.setTime(theDate);
            if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [0] * zwaveConfigurationParameterSizes[parameterNumber];}
            deviceConfiguration.configurationParameters[parameterNumber][2] = theCalendar.get(Calendar.HOUR_OF_DAY);
            deviceConfiguration.configurationParameters[parameterNumber][3] = theCalendar.get(Calendar.MINUTE);
        }
    ];


    //scheduled turn-on (parameterNumber: 86)
    //I am thinking about refactoring this so that it appears as two integer preferecne inputs rather than a "time" preference input.
    // This would be less likely to confuse the user with time zones.
    configurationModel['scheduled turn-on time'] = [
        type: "time", //really a time of day.
        defaultValue: "2015-01-09T18:00:00.000-0000",
        description: "specify the time of day when you want the ouput to turn on",
        apply: {value, deviceConfiguration ->
            def parameterNumber = 86;
            Date theDate = preferenceTimeStringToDate(value);
            java.util.GregorianCalendar theCalendar = new java.util.GregorianCalendar();
            //theCalendar.setTimeZone(location.getTimeZone()); //we will let the time zone be the default, which is UTC+0, and we will set the onboard clock to UTC+0 time.
            theCalendar.setTime(theDate);
            if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [0] * zwaveConfigurationParameterSizes[parameterNumber];}
            deviceConfiguration.configurationParameters[parameterNumber][2] = theCalendar.get(Calendar.HOUR_OF_DAY);
            deviceConfiguration.configurationParameters[parameterNumber][3] = theCalendar.get(Calendar.MINUTE);
        }
    ];
    
    configurationModel['enable scheduled turn-on'] = [
        type: "bool", 
        defaultValue: false,
        description: "control whether the output turns on according to a schedule.",
        apply: makeBooleanApplicator(parameterNumber: 86, bitNumber:24)
    ];

    configurationModel += makeConfigurationModelItemsForBitmask([name: {bit -> "enable scheduled turn-on on " + bit.name + "s"}, parameterNumber: 86,
        describe: {bit ->  "" },
        bits: [
             16: [name: "Monday"     , defaultValue: false, description: ""],
             17: [name: "Tuesday"    , defaultValue: false, description: ""],
             18: [name: "Wednesday"  , defaultValue: false, description: ""],
             19: [name: "Thursday"   , defaultValue: false, description: ""],
             20: [name: "Friday"     , defaultValue: false, description: ""],
             21: [name: "Saturday"   , defaultValue: false, description: ""],
             22: [name: "Sunday"     , defaultValue: false, description: ""]
        ]
    ]);
    
    //scheduled turn-off (parameterNumber: 87)
    //I am thinking about refactoring this so that it appears as two integer preferecne inputs rather than a "time" preference input.
    // This would be less likely to confuse the user with time zones.
    configurationModel['scheduled turn-off time'] = [
        type: "time", //really a time of day.
        defaultValue: "2015-01-09T23:00:00.000-0000",
        description: "specify the time of day when you want the ouput to turn off",
        apply: {value, deviceConfiguration ->
            def parameterNumber = 87;
            Date theDate = preferenceTimeStringToDate(value);
            java.util.GregorianCalendar theCalendar = new java.util.GregorianCalendar();
            //theCalendar.setTimeZone(location.getTimeZone()); //we will let the time zone be the default, which is UTC+0, and we will set the onboard clock to UTC+0 time.
            theCalendar.setTime(theDate);
            if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [0] * zwaveConfigurationParameterSizes[parameterNumber];}
            deviceConfiguration.configurationParameters[parameterNumber][2] = theCalendar.get(Calendar.HOUR_OF_DAY);
            deviceConfiguration.configurationParameters[parameterNumber][3] = theCalendar.get(Calendar.MINUTE);
        }
    ];
    
    configurationModel['enable scheduled turn-off'] = [
        type: "bool",    
        defaultValue: false,
        description: "control whether the output turns off according to a schedule.",
        apply: makeBooleanApplicator(parameterNumber: 87, bitNumber:24)
    ];

    configurationModel += makeConfigurationModelItemsForBitmask([name: {bit -> "enable scheduled turn-off on " + bit.name + "s"},parameterNumber: 87,
        describe: {bit ->  "" },
        bits: [
             16: [name: "Monday"     , defaultValue: false, description: ""],
             17: [name: "Tuesday"    , defaultValue: false, description: ""],
             18: [name: "Wednesday"  , defaultValue: false, description: ""],
             19: [name: "Thursday"   , defaultValue: false, description: ""],
             20: [name: "Friday"     , defaultValue: false, description: ""],
             21: [name: "Saturday"   , defaultValue: false, description: ""],
             22: [name: "Sunday"     , defaultValue: false, description: ""]
        ]
    ]);

    //automatic meter reports
    configurationModel['automatic meter reports'] = [
        type: "enum",
        allowedValues: [
                0: "disabled",
                1: "enabled"
            ],    
        defaultValue: "disabled",
        description: "A meter report will be sent automatically whenever the " + 
            "meter reading changes by more than a threshold amount " + 
            "(the thresholds are set in the following two settings)",
        apply: makeEnumApplicator(parameterNumber: 90)
    ];
    
    configurationModel['automatic meter report power change threshold'] = [
        type:          "number",
        allowedRange:  "0..60000",    
        defaultValue:  25.toInteger(),
        description:   "A meter report will be sent automatically " +
            "whenever the power reading changes by more than this many watts.",
        apply :        makeIntegerApplicator(parameterNumber: 91)
    ];
   
    configurationModel['automatic meter report power fraction change threshold'] =  [
        type:          "number",
        allowedRange:  "0..100",    
        defaultValue:  (Integer) 5,
        description:   "A meter report will be sent automatically " + 
            "whenever the power reading changes by more than this percentage.",
        apply:         makeIntegerApplicator(parameterNumber: 92)
    ];
       
    configurationModel += makeConfigurationModelItemsForBitmask([name: {bit -> "for report group 1, include " + bit.name }, parameterNumber: 101,
        describe: {bit ->  
            "In the periodic meter report that gets sent to report group 1, " +
            "include " + bit.name + (bit.description? "(" + bit.description + ")" : "") 
        },
        bits: [
             0: [name: "combined energy"     , defaultValue: false, description: "the combination (not sure if this is sum or average) of the energy readings for channels 1 and 2"],
             1: [name: "combined power"      , defaultValue: false, description: "the combination (not sure if this is sum or average) of the power readings for channels 1 and 2"],
             2: [name: "combined voltage"    , defaultValue: false, description: "the combination (not sure if this is sum or average) of the voltage readings for channels 1 and 2"],
             3: [name: "combined amperage"   , defaultValue: false, description: "the combination (not sure if this is sum or average) of the amperage readings for channels 1 and 2"],
            11: [name: "channel 1 energy"    , defaultValue: false, description: ""],
             8: [name: "channel 1 power"     , defaultValue: false, description: ""],
            16: [name: "channel 1 voltage"   , defaultValue: false, description: ""],
            19: [name: "channel 1 amperage"  , defaultValue: false, description: ""],
            12: [name: "channel 2 energy"    , defaultValue: false, description: ""],
             9: [name: "channel 2 power"     , defaultValue: false, description: ""],
            17: [name: "channel 2 voltage"   , defaultValue: false, description: ""],
            20: [name: "channel 2 amperage"  , defaultValue: false, description: ""]
        ]
    ]);
    
    configurationModel += makeConfigurationModelItemsForBitmask([name: {bit -> "for report group 2, include " + bit.name }, parameterNumber: 102,
        describe: {bit ->  
            "In the periodic meter report that gets sent to report group 1, " +
            "include " + bit.name + (bit.description? "(" + bit.description + ")" : "") 
        },
        bits: [
             0: [name: "combined energy"     , defaultValue: false, description: "the combination (not sure if this is sum or average) of the energy readings for channels 1 and 2"],
             1: [name: "combined power"      , defaultValue: false, description: "the combination (not sure if this is sum or average) of the power readings for channels 1 and 2"],
             2: [name: "combined voltage"    , defaultValue: false, description: "the combination (not sure if this is sum or average) of the voltage readings for channels 1 and 2"],
             3: [name: "combined amperage"   , defaultValue: false, description: "the combination (not sure if this is sum or average) of the amperage readings for channels 1 and 2"],
            11: [name: "channel 1 energy"    , defaultValue: false, description: ""],
             8: [name: "channel 1 power"     , defaultValue: false, description: ""],
            16: [name: "channel 1 voltage"   , defaultValue: false, description: ""],
            19: [name: "channel 1 amperage"  , defaultValue: false, description: ""],
            12: [name: "channel 2 energy"    , defaultValue: false, description: ""],
             9: [name: "channel 2 power"     , defaultValue: false, description: ""],
            17: [name: "channel 2 voltage"   , defaultValue: false, description: ""],
            20: [name: "channel 2 amperage"  , defaultValue: false, description: ""]
        ]
    ]);
    
    configurationModel += makeConfigurationModelItemsForBitmask([name: {bit -> "for report group 3, include " + bit.name }, parameterNumber: 103,
        describe: {bit ->  
            "In the periodic meter report that gets sent to report group 1, " +
            "include " + bit.name + (bit.description? "(" + bit.description + ")" : "") 
        },
        bits: [
             0: [name: "combined energy"     , defaultValue: false, description: "the combination (not sure if this is sum or average) of the energy readings for channels 1 and 2"],
             1: [name: "combined power"      , defaultValue: false, description: "the combination (not sure if this is sum or average) of the power readings for channels 1 and 2"],
             2: [name: "combined voltage"    , defaultValue: false, description: "the combination (not sure if this is sum or average) of the voltage readings for channels 1 and 2"],
             3: [name: "combined amperage"   , defaultValue: false, description: "the combination (not sure if this is sum or average) of the amperage readings for channels 1 and 2"],
            11: [name: "channel 1 energy"    , defaultValue: false, description: ""],
             8: [name: "channel 1 power"     , defaultValue: false, description: ""],
            16: [name: "channel 1 voltage"   , defaultValue: false, description: ""],
            19: [name: "channel 1 amperage"  , defaultValue: false, description: ""],
            12: [name: "channel 2 energy"    , defaultValue: false, description: ""],
             9: [name: "channel 2 power"     , defaultValue: false, description: ""],
            17: [name: "channel 2 voltage"   , defaultValue: false, description: ""],
            20: [name: "channel 2 amperage"  , defaultValue: false, description: ""]
        ]
    ]);
    
    configurationModel['report group 1 reporting period'] = [
        type: "number",
        allowedRange: "0..4294967295",    
        defaultValue: 0xA.toInteger(),
        description: "a meter report will be sent to group 1 " + 
            "at intervals of this many seconds (I think the units are seconds)",
        apply: makeIntegerApplicator(parameterNumber: 111)
    ];
   
    configurationModel['report group 2 reporting period'] = [
        type: "number",
        allowedRange: "0..4294967295",    
        defaultValue: 0x258.toInteger(),
        description: "a meter report will be sent to group 2 at " + 
            "intervals of this many seconds (I think the units are seconds)",
        apply: makeIntegerApplicator(parameterNumber: 112)
    ];
                        
    configurationModel['report group 3 reporting period'] = [
        type: "number",
        allowedRange: "0..4294967295",    
        defaultValue: 0x258.toInteger(),
        description: "a meter report will be sent to group 3 " + 
            "at intervals of this many seconds (I think the units are seconds)",
        apply: makeIntegerApplicator(parameterNumber: 113)
    ];
   
    configurationModel['switch 1 mode'] = [
        type: "enum",
        allowedValues: [
                0: "automatic identification",
                1: "2 state switch mode",
                2: "3 way switch mode",
                3: "push button mode",
            ],    
        defaultValue: "automatic identification",
        description:  "set the mode of switch 1",
        apply: makeEnumApplicator(parameterNumber: 120)
    ];
    
    configurationModel['switch 2 mode'] = [
        type: "enum",
        allowedValues: [
                0: "automatic identification",
                1: "2 state switch mode",
                2: "3 way switch mode",
                3: "push button mode",
            ],    
        defaultValue: "automatic identification",
        description: "set the mode of switch 2",
        apply: makeEnumApplicator(parameterNumber: 121)  
    ];
       
    configurationModel['switch control destination'] = [
        type: "enum",
        allowedValues: [
                0: "none", // thhis value is not officially documented, but I think it will have useful effects.
                1: "control the output loads of itself",
                2: "control the other nodes",
                3: "control the output loads of itself and other nodes"
            ],    
        defaultValue: "control the output loads of itself and other nodes",
        description: "set the control destination for external switch",
        apply: makeEnumApplicator(parameterNumber: 122)
    ];
    
    configurationModel['switch control destination 2'] = [
        type: "enum",
        allowedValues: [
                0: "none", // thhis value is not officially documented, but I think it will have useful effects.
                1: "control the output loads of itself",
                2: "control the other nodes",
                3: "control the output loads of itself and other nodes"
            ],    
        defaultValue: "control the output loads of itself and other nodes",
        description: "set the control destination for external switch",
        apply: makeEnumApplicator(parameterNumber: 123)
    ];
    
    configurationModel['association group 1 members'] = [
        type: "text",    
        defaultValue: "hub",
        description: "set the members of association group 1.  This is expected to be" +
            " a comma-delimeted list, having 5 or fewer members - it is allowable to " +
            "have zero members, but because Smartthings does not distinguish between a preference having an empty string " +
            " (or a space) as a value and the absence of such a preference setting, if you want to indicate an empty" +
            " set, use the word 'empty'.  Each member should be an integer (in decimal notation) " +
            "or 'hub', which will be treated as the zwave address of the hub.  The members " +
            "are zwave addresses of devices.",
        apply: makeAssociationGroupApplicator(groupingIdentifier: 1)
    ];
    
    configurationModel['association group 2 members'] = [
        type: "text",    
        defaultValue: "hub",
        description: "set the members of association group 2.  This is expected to be" +
            " a comma-delimeted list, having 5 or fewer members - it is allowable to " +
            "have zero members.  Each member should be an integer (in decimal notation) " +
            "or 'hub', which will be treated as the zwave address of the hub.  The members " +
            "are zwave addresses of devices.",
        apply: makeAssociationGroupApplicator(groupingIdentifier: 2)
    ];
    
    configurationModel['association group 3 members'] = [
        type: "text",    
        defaultValue: "hub",
        description: "set the members of association group 3.  This is expected to be" +
            " a comma-delimeted list, having 5 or fewer members - it is allowable to " +
            "have zero members.  Each member should be an integer (in decimal notation) " +
            "or 'hub', which will be treated as the zwave address of the hub.  The members " +
            "are zwave addresses of devices.",
        apply: makeAssociationGroupApplicator(groupingIdentifier: 3)
    ];
    
    configurationModel['association group 4 members'] = [
        type: "text",    
        defaultValue: "hub",
        description: "set the members of association group 4.  This is expected to be" +
            " a comma-delimeted list, having 5 or fewer members - it is allowable to " +
            "have zero members.  Each member should be an integer (in decimal notation) " +
            "or 'hub', which will be treated as the zwave address of the hub.  The members " +
            "are zwave addresses of devices.",
        apply: makeAssociationGroupApplicator(groupingIdentifier: 4)
    ];
    
    configurationModel.each{it.value.apply.delegate = it.value;}
    
    return configurationModel;
}

//perhaps I should have the name of the 'apply' property of the members of configurationModel be a noun (e.g. 'applicator') instead of a verb.
//this function returns a closure that is suitable for use as the 'apply' property of an element of configurationModel.
//arg must have an entry with key=='parameterNumber', and can optionally have an entry with key=='numberOfBytes', whose value, if not specified, will be assumed to be 1,
def makeEnumApplicator(Map arg)
{
    int parameterNumber = arg['parameterNumber'];
    int numberOfBytes = arg['numberOfBytes'] ?: (zwaveConfigurationParameterSizes[parameterNumber] ?: 1);
    
    
    return {String value, Map deviceConfiguration ->
        if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [];}
        deviceConfiguration.configurationParameters[parameterNumber] = integer2Cmd( delegate.allowedValues.find{it.value == value}.key,  numberOfBytes);
    };    
    
    //I am trusting that the parameterNumber and numberOfBytes variables that get bound to this closure are unique every time the function runs.
}

def makeIntegerApplicator(Map arg)
{
    int parameterNumber = arg['parameterNumber'];
    int numberOfBytes = arg['numberOfBytes'] ?: (zwaveConfigurationParameterSizes[parameterNumber] ?: 1);

    
    return {Integer value, Map deviceConfiguration ->
        if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [];}
        deviceConfiguration.configurationParameters[parameterNumber] = integer2Cmd(value,numberOfBytes);
    };  
    
    //I am trusting that the parameterNumber and numberOfBytes variables that get bound to this closure are unique every time the function runs.
}

def makeAssociationGroupApplicator(Map arg)
{
    int groupingIdentifier = arg['groupingIdentifier'];
    
    return {String value, Map deviceConfiguration ->
        if(!deviceConfiguration.associationLists[groupingIdentifier]){deviceConfiguration.associationLists[groupingIdentifier] = [];}
        
        
        deviceConfiguration.associationLists[groupingIdentifier] = value.split(',').findAll{it.toLowerCase() != 'empty'}.collect{
            if(it.toLowerCase() == "hub"){
                return zwaveHubNodeId;
            } else {
                return it.toInteger();
            }
        };
    };  
    
    //I am trusting that the parameterNumber and numberOfBytes variables that get bound to this closure are unique every time the function runs.
}

def makeBooleanApplicator(Map arg)
{
    int parameterNumber = arg['parameterNumber'];
    int numberOfBytes = arg['numberOfBytes'] ?: (zwaveConfigurationParameterSizes[parameterNumber] ?: 1);
    int bitNumber = arg['bitNumber'] ?: 0;
    int defaultInitialValue = arg['defaultInitialValue'] ?: 0; //if deviceConfiguration does not already contain an entry for the relevant zwavve configuration parameter, 
    // we will assume that the initial value of the parameter is defaultInitialValue.

    
    return {boolean value, Map deviceConfiguration -> 
        if(!deviceConfiguration.configurationParameters[parameterNumber]){deviceConfiguration.configurationParameters[parameterNumber] = [];} 
        deviceConfiguration.configurationParameters[parameterNumber] = 
            integer2Cmd(
                (
                    (deviceConfiguration.configurationParameters[parameterNumber] ? 
                        cmd2Integer(deviceConfiguration.configurationParameters[parameterNumber]) : 
                        defaultInitialValue
                    ) & 
                    ~(1 << bitNumber)
                ) | 
                ((value ? 1 : 0) << bitNumber),
                numberOfBytes
            );
    }
    
    //I am trusting that the parameterNumber and numberOfBytes variables that get bound to this closure are unique every time the function runs.
}

/*
    The Smartthings device handler does not (properly) support the multiple:true option for enum preference inputs 
    in a device handler.  Whereas we would hope and expect (based on the behavior of multiple:true in enum preference inputs
    in smartapps, which do work properly) that setting multiple:true in an enum preference input would cause the value of that preference to
    be an array of zero or more of the enum values, the actual result of setting multiple:true in an enum preference input
    of a device handler is that the preference value is a single enum value.
    If the multiple:true option for enum preference inputs worked properly, that would be the obvious way
    to represent bitmask configuration parameters.  
    The next best strategy is to have one boolean preference input (type:"bool") for each bit of a bitmask 
    configuration parameter.  The function makeConfigurationModelItemsForBitmask() handles the tedium of making 
    a configurationModel entry for each bit of a bitmask parameter.  It returns a map, which is expected to be merged into the rest of the
    configurationModel.
*/
def makeConfigurationModelItemsForBitmask(Map arg)
{
    int parameterNumber = arg['parameterNumber'];
    int numberOfBytes = arg['numberOfBytes'] ?: (zwaveConfigurationParameterSizes[parameterNumber] ?: 1);
    Map bits = arg['bits'];
    def name = arg['name']; // 'name' should be thought of as a verb
    def describe = arg['describe'];

    def partialConfigurationModel = [:];
    
    bits.each{bitNumber, bit -> 
        partialConfigurationModel[name(bit)] = [
            type: "bool",
            defaultValue: bit.defaultValue,
            description: describe(bit),
            apply: makeBooleanApplicator(parameterNumber: parameterNumber, bitNumber:bitNumber, numberOfBytes:numberOfBytes)
        ];
    }
    
    return partialConfigurationModel;
}


def generatePreferences() {
    configurationModel.each {
        def inputArg = [:];
        inputArg['name'] = it.key;
        inputArg['type'] = it.value.type;
        inputArg['defaultValue'] = getSetting(it.key).toString(); //note: in the context in which the platform invokes the input() function, the settings object does not exist.  Therefore, in this context, getSetting(it.key) is equivalent to getDefaultSettings()[it.key]
        inputArg['title'] = 
            it.key + "\n" + 
            it.value.description + "\n" + 
            (it.value.allowedRange ? "allowed range: "  + it.value.allowedRange + "\n" : "") +
            "default value: " +  it.value.defaultValue;
        inputArg['description'] = getSetting(it.key).toString();
        if(it.value.type == "enum"){
            inputArg['options'] = it.value.allowedValues.values();
        }
        
        if(it.value.type == "number" || it.value.type == "decimal"){
            if(it.value.allowedRange){inputArg['range'] = it.value.allowedRange;}
        }
        input(inputArg);
    }
}

//returns the numberof bytes in the specified parameter.
// for now, this is hard-coded based on manufacturer's documentation.
//eventually, parameter sizes ought to be determined dynamically by querying the device (a strategy which I susopect is supported by zwave)
private getZwaveConfigurationParameterSizes(){
    return [
        3:1,
        4:1,
        20:1,
        80:1,
        81:1,
        82:1,
        83:1,
        84:4,
        86:4,
        87:4,
        90:1,
        91:2,
        92:1,
        100:1,
        101:4,
        102:4,
        103:4,
        110:1,
        111:4,
        112:4,
        113:4,
        120:1,
        121:1,
        122:1,
        122:1,
        252:1,
        255:4 //curiously, aeotec's documentation describes two possible sizes for parameter 255 -- that is strange.
    ];
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
