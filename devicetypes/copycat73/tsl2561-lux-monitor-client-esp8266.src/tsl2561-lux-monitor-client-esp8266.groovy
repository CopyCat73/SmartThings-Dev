/**
 *  TSL2561 Lux Monitor Client (ESP8266)
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
import groovy.json.JsonSlurper

metadata {
	definition (name: "TSL2561 Lux Monitor Client (ESP8266)", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Illuminance Measurement"
		capability "Refresh"
      	attribute "lastupdate", "string"

	}
    
    simulator {
    	//TBD
	}
    
    preferences {
	    section ("Settings") {
			input name:"deviceIP", type:"text", title:"Lux Monitor IP address", required: true
         }
	}
    
	tiles(scale: 2) {
        
        multiAttributeTile(name:"illuminance", type:"generic", width:6, height:4) {
            tileAttribute("device.illuminance", key: "PRIMARY_CONTROL") {
                attributeState "luminosity", label:'${currentValue} lux', defaultState: true, backgroundColors:[
                    [value: 0, color: "#000000"],
                    [value: 250, color: "#ffcc00"],
                    [value: 1000, color: "#153591"]
                ]
            }
		}
        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}  
  		valueTile("lastupdate", "lastupdate", width: 4, height: 2, inactiveLabel: false) { 			
          state "default", label:"Last updated: " + '${currentValue}' 		
		}                  
        main (["illuminance"])
 		details(["illuminance","lastupdate", "refresh"])
	}

}

private getPort() {
    return 80
}

private getApiPath() { 
	"/"  
}

def installed() {
	log.debug "Installed with settings: ${settings}"
}

def uninstalled() {
	log.debug "uninstalled()"
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	initialize()
}

def initialize() {
	
    // Do the initial poll
	poll()
	// Schedule it to run every minute
	runEvery1Minute("poll")

}
def refresh() {
	poll()
}

def poll() {

	if (deviceIP == null) {
    	log.debug "IP address missing in preferences"
        return
    }
    def hosthex = convertIPtoHex(deviceIP).toUpperCase()
    def porthex = convertPortToHex(getPort()).toUpperCase()
    def path = getApiPath()
    device.deviceNetworkId = "$hosthex:$porthex" 
  	def hostAddress = "$deviceIP:$port"
    def headers = [:] 
    headers.put("HOST", hostAddress)

    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: path,
        headers: headers,
        null,
        [callback : parse] 
    )
    //log.debug ("hubaction" + hubAction)
    //hubAction this does not work with runEveryXMinutes
    sendHubCommand(hubAction)
}

def parse(physicalgraph.device.HubResponse hubResponse) {
    //log.debug "in parse: $hubResponse"
    //log.debug "received data: $hubResponse.body"
    def data = hubResponse.body - "[" -"]" 
    def slurper = new JsonSlurper()
    def json = slurper.parseText(data)

    def lux = json.lux
    log.debug "received value $lux"
    
    sendEvent([name: "illuminance", value: lux, unit: "lux"])
    sendEvent(name: 'lastupdate', value: lastUpdated(now()), unit: "")

}

def lastUpdated(time) {
	def timeNow = now()
	def lastUpdate = ""
	if(location.timeZone == null) {
    	log.debug "Cannot set update time : location not defined in app"
    }
    else {
   		lastUpdate = new Date(timeNow).format("MMM dd yyyy HH:mm", location.timeZone)
    }
    return lastUpdate
}
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}