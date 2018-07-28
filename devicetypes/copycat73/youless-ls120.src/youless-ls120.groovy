/**
 *  Youless LS120
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
	definition (name: "Youless LS120", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"

		attribute "lastupdate", "string"
		attribute "p1", "number"
		attribute "s0", "number"
	}
    
    simulator {
		//TBD
	}
    
    preferences {
	    section ("Settings") {
            input name: "deviceIP", type:"text", title:"Youless IP address", required: true
            input name: "mainInfo", type: "enum", title: "Value to show on the main tile", options: ["0": "P1 data", "1": "S0 data", "2": "P1+S0 (actual usage)"], description: "Select option", required: true      
            input name: "nameP1", type:"text", title:"P1 wording", required: true
            input name: "nameS0", type:"text", title:"S0 wording", required: true
         }
	}

	tiles(scale: 2) {
    	// You might want to change the values and colors in the main tile to your own wattage and liking. This can't be set by preferences.
        
        multiAttributeTile(name:"power", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "level", label:'${currentValue}W', defaultState: true, backgroundColors:[
                    [value: 0, color: "#44b621"],
                    [value: 1000, color: "#f1d801"],
                    [value: 2000, color: "#bc2323"]
                ]
            }
            tileAttribute ("device.combined", key: "SECONDARY_CONTROL") {
        		attributeState "combined", label:'${currentValue}', icon: "st.Appliances.appliances17"
    		}
		}
        standardTile("refresh", "device.refresh", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}  
  		valueTile("lastupdate", "lastupdate", width: 5, height: 1, inactiveLabel: false) { 			
          state "default", label:"Last updated: " + '${currentValue}' 		
		}                  
        main (["power"])
 		details(["power","p1", "s0", "lastupdate", "refresh"])
	}

}

private getPort() {
    return 80
}

private getApiPath() { 
	"/a?f=j" 
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

def parse(response) {
	
    //log.debug "Parsing '${response}'"
    def json = response.json
	//log.debug "Received '${json}'"
    def kwhValue = json.cnt
    def p1Value = json.pwr.toInteger()
    def s0Value = json.ps0.toInteger()
    def netValue = p1Value + s0Value
    def combinedValue = nameP1 + ": " + p1Value + " W " + nameS0 + ": " + s0Value + " W\nTotal: " + kwhValue + " kWh"
    
    def powerValue
    switch (mainInfo) {
        case "0":
            powerValue = p1Value
            break
        case "1":
            powerValue = s0Value
            break
        case "2":
            powerValue = netValue
            break
	}   
    sendEvent([name: "power", value: powerValue, unit: "W"])
    sendEvent([name: "combined", value: combinedValue, unit: "W"])
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