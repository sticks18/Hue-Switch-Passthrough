definition(
    name: "Hue Dimmer Switch Control",
    namespace: "sticks18",
    author: "sgibson18@gmail.com",
    description: "Direct Zigbee Light Control via Hue Dimmer Switch",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Select your Hue Dimmer Switch"){
		input "remote", "capability.button", multiple: false, required: true
	}
    section("Control these Zigbee bulbs..."){
		input "switches", "capability.switch", multiple: true, required: false
        // input "zigId", "text", title: "Zigbee ID"
        // input "endpoint", "text", title: "Zigbee Endpoint"
	}
}

def installed()
{
    initialize()
}

def updated()
{
	unsubscribe()
    initialize()
}


def zigbeeHandler(evt) {
	log.info evt.value
    switches.each {
    	def zigId = it.deviceNetworkId
        def endpoint = it.endpointId
        remote.zigbeeCommand(evt.value, zigId, endpoint)
    }
    
}

def initialize() {
	subscribe(remote, "zMessage", zigbeeHandler)
}
