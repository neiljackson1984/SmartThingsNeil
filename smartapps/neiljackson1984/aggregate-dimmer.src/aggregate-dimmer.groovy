/**
 *  aggregate-dimmer
 *
 *  Copyright 2018 Neil Jackson
 *
 */
definition(
    name: "aggregate-dimmer",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: 
		"Drives a set of switches based on the value of a dimmer switch " 
		+ "(which, optionally, can be a virtual dimmer switch) in order to " 
		+ "create the effect of a dimmable light by using a set of several "
		+ "non-dimmable lights.",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
	section("inputs") {
		input(
			name:"dimmer", 
			type:"capability.switchLevel", 
			description:"choose the dimmer switch, which this SmartApp will watch.",
			required:false
		)
	}
	
	section("outputs") {
		input(
			name:"switches", 
			type:"capability.Switch", 
			description:
            	 "select zero or more (2 or more for good effect) switches, which " 
               + "this SmartApp will control",
			multiple:true,
			required:false
		)
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(
        dimmer,
        "level",
        inputHandler
    ) 
    
    
    
    // for the essential function of this SmartApp, it really is not necessary to listen for events
    // from the switches.  Our only interaction with the switches will be to send them on and off commands.
    // nevertheless, there might prove to be some secondary or diagnostic reason to listen to the switches 
    // (make sure they turned on, logging, etc.)
    subscribe(
        switches,
        "switch",
        catchAllEventHandler
    )


}


def catchAllEventHandler(event) {
    log.debug "catchAllEventHandler was called with ${event.name} ${event.value}"
}

def inputHandler(event) {
	log.debug "inputHandler was called with ${event.name} ${event.value}"
    
    int numberOfSwitchesThatShouldBeOn = event.integerValue / 100 * switches.size()
    log.debug "turning on ${numberOfSwitchesThatShouldBeOn} switches."
    
    for (int i = 0; i < switches.size(); i++) {
    	if( (i+1) <= numberOfSwitchesThatShouldBeOn)
        {
        	switches[i].on();
        } 
        else
        {
        	switches[i].off();
        }
	}
}