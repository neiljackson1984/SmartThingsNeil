/**
 *  Vision Zwave Dual Relay Module
 *
 *  Copyright 2018 Neil Jackson
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
 * *  *  MAGIC COMMENTS USED BY MY MAKEFILE FOR UPLOADING AND TESTING THE CODE:
 *  //////hubitatId=481
 *  //////hubitatIdOfTestInstance=193
 *  //////testEndpoint=
 *  //////typeOfCode=device
 *  //////urlOfHubitat=https://toreutic-abyssinian-6502.dataplicity.io
 */
metadata {
	definition (name: "Vision Zwave Dual Relay Module", namespace: "neiljackson1984", author: "Neil Jackson") {
    	//TAGGING CAPABILITIES: ('tagging' implies that these capabilities have no attributes, and have no commands)
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform SmartThings that this device has commands     
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform SmartThings that this device has attributes       
        
       //NON-TAGGING CAPABILITIES:
       capability "Switch"
       	// attributes: switch
           // commands: 'on', 'off'
        
        command("makeChildDevicesIfNeeded");
        
        //fingerprint copied from justintime/Monoprice 11990 Dual Relay Module
        fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x20, 0x27, 0x71, 0x2B, 0x2C, 0x75, 0x7A, 0x60, 0x32, 0x70"
	}


	simulator {
		// TODO: define status and reply messages here
	}

    preferences {
        // input(
            // name: "foo",
            // type: "bool",
            // title: "this is the title of foo",
            // description: "description for foo",
            // required: false,
            // defaultValue: false
        // );
    }

	tiles {
		  multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
            }
        }
        
        //childDeviceTiles("all")
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        
	}
}




def parse(String description) {
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}


def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, ep = null) {
    log.debug "BasicReport ${cmd} - ep ${ep}"
    if (ep) {
        def event
        childDevices.each {
            childDevice ->
                if (childDevice.deviceNetworkId == "$device.deviceNetworkId-ep$ep") {
                    childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
                }
        }
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each {
                n ->
                    if (n.deviceNetworkId != "$device.deviceNetworkId-ep$ep" && n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    }
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    log.debug "BasicSet ${cmd}"
    def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    return [result, response(commands(cmds))] // returns the result of reponse()
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
    log.debug "SwitchBinaryReport ${cmd} - ep ${ep}"
    if (ep) {
        def event
        def childDevice = childDevices.find {
            it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"
        }
        if (childDevice) childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each {
                n->
                    if (n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
                event = [createEvent([name: "switch", value: "off"])]
            } else {
                event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        return [result, response(commands(cmds))] // returns the result of reponse()
    }
}

def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "MultiChannelCmdEncap ${cmd}"
    def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    }
}

def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "ManufacturerSpecificReport ${cmd}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    log.debug "Unhandled Event: ${cmd}"
}


def on() {
    log.debug "on()"
    commands([
            zwave.switchAllV1.switchAllOn(),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    ])
}

def off() {
    log.debug "off()"
    commands([
            zwave.switchAllV1.switchAllOff(),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    ])
}

void childOn(String dni) {
    log.debug "childOn($dni)"
    send([
        encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni)),
        encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))
    ])
}

void childOff(String dni) {
    log.debug "childOff($dni)"
    send([
        encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni)),
        encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))
    ])
}

void childRefresh(String dni) {
    log.debug "childRefresh($dni)"
    send([encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))])
}

def poll() {
    log.debug "poll()"
    commands([
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
    ])
}

def refresh() {
    log.debug "refresh()"
    commands([
            encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
            encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
    ])
}



def installed() {
    log.debug "installed()"
    //command(zwave.manufacturerSpecificV1.manufacturerSpecificGet()) 
    // the above line was in the Inovelli 2-channel smart plug, which I used as a starting point for this device handler.  As far as I can tell, it does nothing
    // because command() simply returns a string, without producing side effects.  Perhaps, the line was left over from whatever Inovelli used as a starting point for their device handler.
    createChildDevices()
}

def updated() {
    log.debug "updated()"
    makeChildDevicesIfNeeded()
    
    
    // //RETURN a HubAction object (that's what response() returns, to tell the hub to send configurationcommands to the zwave device.
    // def cmds = []
    // cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
    // cmds << zwave.associationV2.associationGet(groupingIdentifier:1)
    // cmds << zwave.configurationV1.configurationSet(configurationValue: [ledIndicator? ledIndicator.toInteger() : 0], parameterNumber: 1, size: 1)
    // cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
    // cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff1? autoOff1.toInteger() : 0, parameterNumber: 2, size: 2)
    // cmds << zwave.configurationV1.configurationGet(parameterNumber: 2)
    // cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff2? autoOff2.toInteger() : 0, parameterNumber: 3, size: 2)
    // cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    // response(commands(cmds))
}


def makeChildDevicesIfNeeded() {
    if (!childDevices) {
        createChildDevices()
    } else if (device.label != state.oldLabel) {
        childDevices.each {
            if (it.label == "${state.oldLabel} (CH${channelNumber(it.deviceNetworkId)})") {
                def newLabel = "${device.displayName} (CH${channelNumber(it.deviceNetworkId)})"
                it.setLabel(newLabel)
            }
        }
        state.oldLabel = device.label
    }
}



private void createChildDevices() {
    state.oldLabel = device.label
    for (i in 1..2) {
       addChildDevice(
       		/* namespace:        */   "neiljackson1984"                       ,  
            /* typeName:         */   "Vision Zwave Child"                    , 
            /* deviceNetworkId:  */   "${device.deviceNetworkId}-ep${i}"      , 
            ///* hubId:            */   null                                    ,         
            /* properties:       */  
            	[
                	completedSetup: true, 
                    label: "${device.displayName} (CH${i})",
                    isComponent: false, 
                    componentName: "ep$i",  
                    componentLabel: "Channel $i"
                ]
        );
    }
}

private encap(cmd, endpoint) {
    if (endpoint) {
        zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
    } else {
        cmd
    }
}

private command(hubitat.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay = 1000) {
    delayBetween(commands.collect {
        command(it)
    }, delay)
}

/* cmds is expected to be a list of hubitat.zwave.Command objects*/
private void send(cmds) {
    sendHubCommand(
        new hubitat.device.HubMultiAction(
            commands(cmds)
        )
    )
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}