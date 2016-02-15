/**
 *  
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
/* Philips Hue Wireless Dimmer

Capabilities:
  Actuator
  Configuration
  Polling
  Refresh
  Switch
  Switch Level
  
    
*/

metadata {
	definition (name: "Hue Dimmer Remote", namespace: "Sticks18", author: "Scott G") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Sensor"
        
		command "zigbeeCommand"
        
        attribute "zMessage", "String"
        
	//	fingerprint profileId: "C05E", deviceId: "0820", inClusters: "0000", outClusters: "0000,0003,0004,0006,0008", manufacturer: "Philips", model: "RWL020"
	}

	// simulator metadata
	simulator {
		// status messages

	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
		main "button"
		details(["button"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
    //	log.trace description
    sendEvent(name: "zMessage", value: description, displayed: false, isStateChange: true)
    def msg = zigbee.parse(description)
    
    if (msg.clusterId == 6) {
    	
    	def button = (msg.command == 1 ? 1 : 4)
        def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug "Parse returned ${result?.descriptionText}"
        return result 
    }
    else if (msg.clusterId == 8) {
    
    	switch (msg.command) 
        {
        
        	case 2:
            	
                def y = description[-6..-5]
                if (y == "1E") {
            		               	
                    def button = (description[-8..-7] == "00" ? 2 : 3)
                	def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
                    log.debug "Parse returned ${result?.descriptionText}"
                    return result
					break
                }
                log.debug "Received held message"
                break
         	
            case 3:
            	
            	log.debug "Received stop message"
                break
                
        }
     }
        
}

def zigbeeCommand(description, zigId, endP) {
    def msg = zigbee.parse(description)
    log.debug msg
    
	if (msg.clusterId == 6) {
    	
    	def c = (msg.command == 1 ? 1 : 0)
        
        def results = "st cmd 0x${zigId} 0x${endP} 6 ${c} {}"
        log.debug results
        return results
    }
    else if (msg.clusterId == 8) {
    	
        def c = msg.command
        def p = (c==2 ? description[-8..-1] : "")      	
           
           // def m = description[-8..-7]
           // def s = description[-6..-5]
           // def t = description[-4..-1]
        
        def results = "st cmd 0x${zigId} 0x${endP} 8 ${c} {${p}}"
        log.debug results
        return results
     }
}


def configure() {

	log.debug "configure"
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting and Bindings."
	def configCmds = [	
		
        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 1 1 8 {${device.zigbeeId}} {}", "delay 500",
        
	]
    return configCmds  
}


private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}
