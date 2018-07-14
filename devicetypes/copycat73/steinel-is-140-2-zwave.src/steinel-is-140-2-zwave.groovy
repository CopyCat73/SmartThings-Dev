/**
 *  Steinel IS 140-2 Z-Wave
 *
 *  Copyright 2018 Nick Veenstra
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
	definition (name: "Steinel IS 140-2 Z-Wave", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Motion Sensor"
		capability "Switch"
		capability "Illuminance Measurement"
		capability "Polling"
        capability "Refresh"
		capability "Configuration"
        
        attribute "lastmotion", "string"

                
        fingerprint mfr: "0271", prod: "0002", model: "1A72",deviceJoinName: "Steinel IS 140-2 Z-Wave"
    }


	simulator {
		// TODO: define status and reply messages here
        
	}
    
    preferences {
        input name: "lightDuration", type: "number", title: "Duration of light after motion detection", description: "Value between 5 and 900 seconds", range: "5..900", required: false
        input name: "lightThreshold", type: "number", title: "Light threshold", description: "Value between 0 and 2000 lux", range: "0..1000", required: false
        input name: "sensitivity", type: "number", title: "Sensitivity", description: "Value between 2 and 100%", range: "2..100", required: false
		input name: "globalLight", type: "enum", title: "Global Light", options: ["0": "Enable", "1": "Disable"], description: "Enable/Disable", required: false      
        input name: "slaveMode", type: "enum", title: "Slave Mode", options: ["0": "Normal mode", "1": "Slave mode without gateway checking", "2": "Normal mode with lifeline error signalisation", "3": "Slave mode with gateway checking", "4": "Stupid mode - lamp permanently on"], description: "Set slave mode", required: false
        input name: "offBehaviour", type: "number", title: "Off behaviour", description: "Value between 0 and 209", range: "0..209", required: false
        input name: "onBehaviour", type: "number", title: "On behaviour", description: "Value between 0 and 209", range: "0..209", required: false
        input name: "onBehaviourTimeOver", type: "number", title: "On behaviour time over", description: "Value between 0 and 209", range: "0..209", required: false
        input name: "sequenceOnOffBehaviour", type: "number", title: "Sequence On-Off behaviour", description: "Value between 0 and 209", range: "0..209", required: false
        input name: "sequenceOffOnBehaviour", type: "number", title: "Sequence Off-On behaviour", description: "Value between 0 and 209", range: "0..209", required: false
        input name: "sequenceTiming", type: "number", title: "Sequence Timing", description: "Value between 10 and 50", range: "10..50", required: false
        input name: "motionOffBehaviour", type: "number", title: "Motion Off behaviour", description: "Value between 0 and 255", range: "0..255", required: false
    }
    

	tiles(scale: 2) {
        standardTile("motion", "device.motion", width: 6, height: 4) {
            state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
            state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        }
        standardTile("switch", "device.switch", width: 2, height: 2) {
            state "off", label: '${name}', icon: "st.switches.switch.off", backgroundColor: "#ffffff", action: "switch.on"
            state "on", label: '${name}', icon: "st.switches.switch.on", backgroundColor: "#00a0dc", action: "switch.off"
        }
	    valueTile("illuminance", "device.illuminance", width: 2, height: 2, inactiveLabel: false) {
            state "luminosity", label:'${currentValue}\nlux', unit:"lux"
        }
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main(["motion"])
        details(["motion", "switch", "illuminance", "refresh"])
    }

}


def parse(String description) {
        def result = null
        def cmd = zwave.parse(description, [0x60: 3])
        if (cmd) {
                result = zwaveEvent(cmd)
                log.debug "Parsed ${cmd} to ${result.inspect()}"
        } else {
                log.debug "Non-parsed event: ${description}"
        }
        result
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	//log.debug "sensormultilevelv5"
    def result = null
    if (cmd.sensorType == 3) {
        result = createEvent(name: "illuminance", value: cmd.scaledSensorValue.toInteger().toString(), unit: "lux")
    }
    result
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    
    def result = null
    if (cmd.notificationType == 7) {
    	switch (cmd.event) {
        	case 0:
            	if (cmd.eventParameter[0] == 3) {
					result = createEvent(name: "tamper", value: "inactive", displayed: true)
            	}
            	if (cmd.eventParameter[0] == 8) {
                	result = createEvent(name: "motion", value: "inactive", displayed: true)
                }
        		break
                
        	case 3:
            	result = createEvent(name: "tamper", value: "active", displayed: true)
            	break
                
            case 8:
        		result = createEvent(name: "motion", value: "active", displayed: true)
                break
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
        createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
        createEvent(name:"switch", value: cmd.value ? "on" : "off")
}


def ping() {
	refresh()
}

def refresh() {
	log.debug "Refresh"
	poll()
}

def poll() {
        delayBetween([
                zwave.switchBinaryV1.switchBinaryGet().format(), // get switch
                zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1).format(),  // get lux
        ], 1200)
}


def updated() {
    if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
    	log.debug "Updated"
        state.updatedLastRanAt = now()
		response(configure())
    }
}

def configure() {

	log.debug "Sending Configuration to device" 
    def cmds = []
   	if (lightDuration) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 2, scaledConfigurationValue: lightDuration.toInteger()).format() }   
    if (lightThreshold) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 2, scaledConfigurationValue: lightThreshold.toInteger()).format() } 
    if (sensitivity) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: sensitivity.toInteger()).format() }
    if (globalLight) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: globalLight.toInteger()).format() } 	
    if (slaveMode) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: slaveMode.toInteger()).format() }   
    if (offBehaviour) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 2, scaledConfigurationValue: offBehaviour.toInteger()).format() }   
    if (onBehaviour) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 2, scaledConfigurationValue: onBehaviour.toInteger()).format() }     
    if (onBehaviourTimeOver) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 12, size: 2, scaledConfigurationValue: onBehaviourTimeOver.toInteger()).format() }   
    if (sequenceOnOffBehaviour) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 13, size: 2, scaledConfigurationValue: sequenceOnOffBehaviour.toInteger()).format() }     
    if (sequenceOffOnBehaviour) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 14, size: 2, scaledConfigurationValue: sequenceOffOnBehaviour.toInteger()).format() }   
    if (sequenceTiming) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 15, size: 1, scaledConfigurationValue: sequenceTiming.toInteger()).format() }     
    if (motionOffBehaviour) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 16, size: 2, scaledConfigurationValue: motionOffBehaviour.toInteger()).format() }   
  	delayBetween(cmds,1500) 
   
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
	], 2000)
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
	], 2000)
}

