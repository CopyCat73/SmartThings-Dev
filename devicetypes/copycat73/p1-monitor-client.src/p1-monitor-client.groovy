/**
 *  P1 Monitor Client
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
	definition (name: "P1 Monitor Client", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Energy Meter"
		capability "Power Meter"
        capability "Refresh"

		attribute "return", "number"
        attribute "returnEnergy", "number"
        attribute "displayCombined", "string"
        
		attribute "lastupdate", "string"

	}
    
    simulator {
    	//TBD
	}
    
    preferences {
	    section ("Settings") {
			input name:"deviceIP", type:"text", title:"P1 Monitor IP address", required: true
         }
	}

	tiles(scale: 2) {
    	// You might want to change the values and colors in the main tile to your own wattage and liking. This can't be set by preferences.
        
        multiAttributeTile(name:"power", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "level", label:'${currentValue} W', defaultState: true, backgroundColors:[
                    [value: 0, color: "#44b621"],
                    [value: 1000, color: "#f1d801"],
                    [value: 2000, color: "#bc2323"]
                ]
            }
            tileAttribute ("device.displayCombined", key: "SECONDARY_CONTROL") {
                attributeState "level", label:'${currentValue}', icon: "st.Appliances.appliances17"
            }
        }
        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}  
  		valueTile("lastupdate", "lastupdate", width: 4, height: 2, inactiveLabel: false) { 			
          state "default", label:"Last updated: " + '${currentValue}' 		
		}                  
        main (["power"])
 		details(["power","energy", "lastupdate", "refresh"])
	}

}

private getPort() {
    return 80
}

private getApiPath() { 
	"/json/apiV3basic.php"  
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
    log.debug ("hubaction" + hubAction)
    //hubAction this does not work with runEveryXMinutes
    sendHubCommand(hubAction)
}

def parse(physicalgraph.device.HubResponse hubResponse) {
    //log.debug "in parse: $hubResponse"
    //log.debug "received data: $hubResponse.body"
    def data = hubResponse.body - "[" -"]" 
    def slurper = new JsonSlurper()
    def json = slurper.parseText(data)

    def energyKwhValue = json.VERBR_KWH_SUM
    def returnEnergyKwhValue = json.GELVR_KWH_SUM
    def powerWattValue = json.ACT_W_VERBR
    def returnWattValue = json.ACT_W_GELVR
    def displayCombined = "Return: ${returnWattValue} W Energy: ${energyKwhValue} kWh\nReturned energy: ${returnEnergyKwhValue} kWh"
    log.debug "received values $powerWattValue $displayCombined"
    
    sendEvent([name: "energy", value: energyKwhValue, unit: "kWh"])
    sendEvent([name: "returnEnergy", value: returnEnergyKwhValue, unit: "kWh"])
    sendEvent([name: "power", value: powerWattValue, unit: "W"])
    sendEvent([name: "return", value: returnWattValue, unit: "W"])
    sendEvent([name: "displayCombined", value: displayCombined, unit: ""])
    sendEvent(name: 'lastupdate', value: lastUpdated(now()), unit: "")
    
    /*
    {
    "TIMESTAMP": "2018-01-10 23:17:23",
    "P1DB_PROCESSED": 0,
    "VERBR_KWH_D": 6692,
    "VERBR_KWH_P": 3491,
    "GELVR_KWH_D": 2279,
    "GELVR_KWH_P": 5726,
    "TARIEFCODE": "D",
    "ACT_W_VERBR": 420,
    "ACT_W_GELVR": 0,
    "VERBR_KWH_SUM": 10183,
    "GELVR_KWH_SUM": 8005,
    "VERBR_GAS": 0,
    "DATA_IS_OK": 1,
    "STATUS": "production"
    }
    */


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