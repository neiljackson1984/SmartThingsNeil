/**
 *  aggregateDimmer
 *
 *  Copyright 2018 Neil Jackson
 *
 */
definition(
    name: "aggregateDimmer",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: "Drives a set of switches based on the value of a dimmer switch (which, optionally, can be a virtual dimmer switch) in order to create the effect of a dimmable light by using a set of several non-dimmable lights.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Title") {
		// TODO: put inputs here
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers