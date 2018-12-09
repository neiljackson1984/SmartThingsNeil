/**
 *  web exposer
 *
 *  Copyright 2018 Neil Jackson
 *
 *  This smartapp subscribes to a device, and exposes some REST endpoints that call commands of that device.  You must use the 
 *  smartthings web interface (https://graph-na04-useast2.api.smartthings.com/ide/app/edit/<<id of this smartapp>>) to enable oauth.
 */
definition(
    name: "web exposer",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: "This smartapp subscribes to a device, and exposes some REST endpoints that call functions (or commands) of that device.",
    category: "Testing",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

mappings {
  path("/runTheTestCode") {
    action: [
      GET: "runTheTestCode"
    ]
  }
}

def runTheTestCode()
{
    return preferences.exposedDevice?.runTheTestCode();
    //curiously, when we designate 'runTheTestCode' as a coommand, the above call to device.runTheTestCode(). does not return the value that we return within the device's runTheTestCode() using the return statement.
    //however, if the child thermostat's runTheTestCode() is not designated as a command, then the return value propagates as expected.  This quirk is probably related to the pattern of having command methods return 
    //a list of events, that the platform then processes.
}

preferences {
	page(name: "mainPage");
}

def mainPage() {
	def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);
    dynamicPage(
    	name: "mainPage", 
        title: "", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?") : yes
        install: true, 
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: true //getChildDevices( /*includeVirtualDevices: */ true ).isEmpty() 
       
    ) {
    	section(/*"label"*/) {
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of the Web Exposer SmartApp", 
                required: false, 
                defaultValue: "web-exposer--" + myDateFormat.format(myDate)
            );
        }
    	section(/*"exposed device"*/) {
            input(
                name: "exposedDevice", 
                title: "device to expose as a web endpoint" ,
                type: "capability.actuator", 
                description: "select one device",            
                multiple:false,
                required:false,
                submitOnChange:false 
            )
        }

       section("") {
        	mode( title: "Set for specific mode(s)", required: false);
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    
    // we do not want to call initialize here, because app.getLabel() evaluates to the default lable, which is the same as the name of the Smart App.
    // immediately after installing and calling this installed() function, the platform will then call the updated() function.
    // we want to call initialize from the updated() function, because by that time, app.getLabel() evaluates to the real label of this
    // installedSmartApp.
	//initialize()
}

def uninstalled() {
	log.trace "uninstalling"
    //getAllChildDevices().each {
    //    //deleteChildDevice(it.deviceNetworkId, true)
    //    deleteChildDevice(it.deviceNetworkId)
    //}
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe();
    initialize();
}

def initialize() {
	log.debug "app.getLabel(): ${app.getLabel()}"
    log.debug ("label: " + label)
}

def doNothing(event){
	log.debug "doNothing(${event}) was called."
}

