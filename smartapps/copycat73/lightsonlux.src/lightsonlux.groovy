/**
 *  LightsOnLux
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
definition(
    name: "LightsOnLux",
    namespace: "CopyCat73",
    author: "Nick Veenstra",
    description: "Switch lights on or off when an illuminance sensor crosses a threshold (and only then).",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Settings") {
		input "lightSensor", "capability.illuminanceMeasurement", title: "Select illuminance sensor"
		input "lights", "capability.switch", multiple: true, title: "Select one or more lights to switch"
		input "upperThreshold", "number", title: "Enter lux amount above which lights turn off"
		input "lowerThreshold", "number", title: "Enter lux amount below which lights turn on"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    
    state.canSwitchOn = true
    state.canSwitchOff = true
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
  	subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
}


def illuminanceHandler(evt) {
	//log.debug "$evt.name: $evt.value, lastStatus: $state.lastLux"
    
    if (evt.integerValue <= lowerThreshold && state.canSwitchOn) {
        state.canSwitchOn = false
    	state.canSwitchOff = true
    	lights.on()
    }
    
    if (evt.integerValue >= upperThreshold && state.canSwitchOff) {
     	state.canSwitchOn = true
    	state.canSwitchOff = false
    	lights.off()
    }
}

