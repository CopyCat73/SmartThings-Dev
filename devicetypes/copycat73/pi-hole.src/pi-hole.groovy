/**
 *  Pi-hole
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
	definition (name: "Pi-hole", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Switch"
		capability "Refresh"

		attribute "lastupdate", "string"
	}
    
    simulator {
		//TBD
	}
    
    preferences {
	    section ("Settings") {
            input name: "deviceIP", type:"text", title:"Pi-home IP address", required: true
            input name: "apiToken", type: "text", title: "API token", required: true      
         }
	}

	tiles(scale: 2) {
    	
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4) {
    		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Office.office10", backgroundColor:"#44b621", nextState:"off"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Office.office10", backgroundColor:"#bc2323", nextState:"on"
            }
          	tileAttribute ("device.combined", key: "SECONDARY_CONTROL") {
        		attributeState "combined", label:'${currentValue}', icon: "st.Electronics.electronics18"
    		}
		}
        standardTile("refresh", "device.refresh", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}  
  		valueTile("lastupdate", "lastupdate", width: 5, height: 1, inactiveLabel: false) { 			
          state "default", label:"Last updated: " + '${currentValue}' 		
		}                  
        main (["switch"])
 		details(["switch", "lastupdate", "refresh"])
	}

}

private getPort() {
    return 80
}

private getApiPath() { 
	"/admin/api.php" 
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
    sendHubCommand(hubAction)
}

def parse(response) {
	
    //log.debug "Parsing '${response}'"
    def json = response.json
	//log.debug "Received '${json}'"
    if (json.FTLnotrunning) {
    	return
    }
    if (json.status == "enabled") {
    	sendEvent(name: "switch", value: "on")
    }
	if (json.status == "disabled") {
    	sendEvent(name: "switch", value: "off")
    }
    if (json.dns_queries_today.toInteger() >= 0) {
    	def combinedValue = "Queries today: " +json.dns_queries_today + " Blocked: " + json.ads_blocked_today + "\nClients: " + json.unique_clients
		sendEvent(name: "combined", value: combinedValue, unit: "")
    } 	
    
    sendEvent(name: 'lastupdate', value: lastUpdated(now()), unit: "")
   
}

def on() {
	doSwitch("enable")
}

def off() {
	doSwitch("disable")
}

def doSwitch(toggle) {
	
    if (deviceIP == null) {
    	log.debug "IP address missing in preferences"
        return
    }
    def hosthex = convertIPtoHex(deviceIP).toUpperCase()
    def porthex = convertPortToHex(getPort()).toUpperCase()
    def path = getApiPath() + "?" + toggle + "&auth=" + apiToken
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
    sendHubCommand(hubAction)
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