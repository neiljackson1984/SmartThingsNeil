/**
 *  Vision Zwave DualRelay Module
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
 */
metadata {
	definition (name: "Vision Zwave Child", namespace: "neiljackson1984", author: "Neil Jackson") {
    
        //TAGGING CAPABILITIES: ('tagging' implies that these capabilities have no attributes, and have no commands)
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform SmartThings that this device has commands     
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform SmartThings that this device has attributes       

        //NON-TAGGING CAPABILITIES:
        capability "Switch"
        // attributes: switch
        // commands: 'on', 'off'
        
        capability "Refresh"
    
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState:"turningOff"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
}

// this device is not associated with a physical zwave or zigbee device, and therefore I expect that the SmartThings platform
// will never call this device's parse() method.
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

void on() {
	parent.childOn(device.deviceNetworkId)
}

void off() {
	parent.childOff(device.deviceNetworkId)
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}