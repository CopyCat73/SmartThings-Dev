/*
 *  Netatmo Person
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
	definition (name: "Netatmo Person", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Presence Sensor"
		capability "Sensor"
        
        command "seen"
		command "away"
	}

	simulator {
		// TBD
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.present", backgroundColor:"#00A0DC")
			state("not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff")
		}
		main "presence"
		details "presence"
	}
}

def installed() {
    log.debug "Netatmo person installed"
}

def updated() {
    log.debug "Netatmo person updated"    
}

// handle commands
def seen() {
    sendEvent(name: "presence", value: "present")
}


def away() {
	 sendEvent(name: "presence", value: "not present")
}

