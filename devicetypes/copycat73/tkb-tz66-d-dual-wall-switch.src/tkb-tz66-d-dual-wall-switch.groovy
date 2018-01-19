/**
 *  TKB TZ66-D Dual wall switch
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
 *
 *	Note: device id's and models for these TKB home devices are a mess: re-used id's and/or no real certification. Just use one of the switch or dimmer versions 
 *  and change to the proper DTH needed after including. 
 */
 
metadata {
	definition (name: "TKB TZ66-D Dual wall switch", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
        
        //zw:L type:1001 mfr:0118 prod:0102 model:1020 ver:1.03 zwv:2.97 lib:06 cc:25,85,27,70,73,86,72
        fingerprint mfr:"0118", prod:"0102", model:"1020", deviceJoinName: "TKB TZ66-D Dual wall switch"
        
	}


	simulator {
		// TODO: define status and reply messages here
	}

    preferences {
    	
    	input name: "includeGroup1", type: "text",  title: "Include devices in group 1 - tap left button once (Enter Device Network Id's comma separated)"
        input name: "includeGroup2", type: "text",  title: "Include devices in group 2 - tap right button once (Enter Device Network Id's comma separated)"
        input name: "includeGroup3", type: "text",  title: "Include devices in group 3 - tap right button twice (Enter Device Network Id's comma separated)"
		input name: "includeGroup4", type: "text",  title: "Include devices in group 4 - devices follow load (Enter Device Network Id's comma separated)"
		input name : "ignoreStartLevelBit", type: "enum", options: ["0" : "Do not ingnore start level","1" : "Ignore start level"], title: "Set Ignore when transmitting dim commands", description: "The dimmer either ignore the start level and start dimming from its current level or use the start level"
		input name : "disableTransmittingToGroup4", type:"enum", options: ["0" : "Do not transmit", "1": "Transmit"], title:"Disable transmitting to group 4", description : "Control transmission to devices that are associated into Group 4."
		input name : "nightLight", type: "enum", options: ["0" : "LED is ON when the load attached is OFF", "1" : "LED is ON when the load attached is ON"], title: "Night light", description: "The LED behaviour based on the load attached."
		input name : "invertSwitch", type: "enum", options: ["0": "Top button = On", "1": "Top button = Off"], title: "Invert switch", description: "Switch on/off behaviour"
		input name : "enableShadeControlGroup2", type: "enum", options: ["0": "Disable", "1": "Enable"], title: "Enable shade control Group 2", description: "The device can operate shade control devices via its Group 2 if this option is activated" 
		input name : "enableShadeControlGroup3", type: "enum", options: ["0": "Disable", "1": "Enable"], title: "Enable shade control Group 3", description: "The device can operate shade control devices via its Group 3 if this option is activated" 
		input name : "ledTransmissionIndication", type: "enum", options: ["0": "No flicker","1": "1 second", "2": "entire transmission duration"], title: "LED Transmission indication", discription: "The device can flicker its LED when it is transmitting to any of its 4 groups."
        input name : "pollGroup2Interval", type: "number", title: "Poll Group 2 Interval (minutes)", discription: "Defines the poll interval in minutes for group 2 if polling is activated."
		input name : "pollGroup2", type: "enum", options: ["0" : "Do not poll","1" : "Poll"], title: "Poll Group 2", discription: "If this option is disabled, the device will not poll Group 2. If enabled, the device will poll Group 2 at the interval set in the previous option."
	}
    
    tiles(scale: 2) {
  	
        // Multi Tile:
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
        }
		standardTile("refresh", "device.switch", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
                        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
                }
        standardTile("configure", "device.switch", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
        				state "default", label:"", action:"configure", icon:"st.secondary.configure"
                }        
        main(["switch"])
        details(["switch", "refresh", "configure"])
    
	}
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
	], 2000)
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
	], 2000)
}


def ping() {
	refresh()
}

def refresh() {
	log.debug "Refresh"
	poll()
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format() //dimmer
	//zwave.switchBinaryV1.switchBinaryGet().format() //switch
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
    //versies checken V1 of V2
    if (ignoreStartLevelBit) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: ignoreStartLevelBit.toInteger()).format() }   
    if (disableTransmittingToGroup4) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: disableTransmittingToGroup4.toInteger()).format() } 
    if (nightLight) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: nightLight.toInteger()).format() }
    if (invertSwitch) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: invertSwitch.toInteger()).format() } 	
    if (enableShadeControlGroup2) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 14, size: 1, scaledConfigurationValue: enableShadeControlGroup2.toInteger()).format() }   
    if (enableShadeControlGroup3) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 15, size: 1, scaledConfigurationValue: enableShadeControlGroup3.toInteger()).format() }   
    if (ledTransmissionIndication) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 19, size: 1, scaledConfigurationValue: ledTransmissionIndication.toInteger()).format() }     
    if (pollGroup2Interval) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 20, size: 1, scaledConfigurationValue: pollGroup2Interval.toInteger()).format() }     
    if (pollGroup2) { cmds << zwave.configurationV1.configurationSet(parameterNumber: 22, size: 1, scaledConfigurationValue: pollGroup2.toInteger()).format() }    
    cmds << zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: []).format()
    if (includeGroup1) {
    	def deviceList = includeGroup1.split(',')
    	deviceList.each {switchDev ->
        	def nodeId = Integer.parseInt(switchDev,16)
    		//log.debug "Group 1 included node: ${nodeId}"
            cmds << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[nodeId]).format()
        }
    }
    cmds << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: []).format()
    if (includeGroup2) {
    	def deviceList = includeGroup2.split(',')
    	deviceList.each {switchDev ->
        	def nodeId = Integer.parseInt(switchDev,16)
    		//log.debug "Group 2 included node: ${nodeId}"
            cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[nodeId]).format()
        }
    } 
    cmds << zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: []).format()
	if (includeGroup3) {
    	def deviceList = includeGroup3.split(',')
    	deviceList.each {switchDev ->
        	def nodeId = Integer.parseInt(switchDev,16)
    		//log.debug "Group 3 included node: ${nodeId}"
            cmds << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[nodeId]).format()
        }
    }
    cmds << zwave.associationV2.associationRemove(groupingIdentifier: 4, nodeId: []).format()
	if (includeGroup4) {
    	def deviceList = includeGroup4.split(',')
    	deviceList.each {switchDev ->
        	def nodeId = Integer.parseInt(switchDev,16)
    		//log.debug "Group 4 included node: ${nodeId}"
            cmds << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[nodeId]).format()
        }
    }
	delayBetween(cmds,1500) 
}
private secure(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}


def parse(String description) {
    def result = null
    def cmd = zwave.parse(description)
    if (cmd) {
        result = zwaveEvent(cmd)
        //log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        //log.debug "Non-parsed event: ${description}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, type: "physical")]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%", type: "physical")
	}
	return result
}
