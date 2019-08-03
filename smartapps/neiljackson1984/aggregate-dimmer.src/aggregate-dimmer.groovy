/**
 *  aggregate-dimmer
 *  *  MAGIC COMMENTS USED BY MY MAKEFILE FOR UPLOADING AND TESTING THE CODE:
 *  //////hubitatId=97
 *  //////hubitatIdOfTestInstance=326
 *  //////testEndpoint=runTheTestCode
 *  //////typeOfCode=app
 *  //////urlOfHubitat=https://toreutic-abyssinian-6502.dataplicity.io
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

mappings {
     path("/runTheTestCode") { action: [GET:"runTheTestCode"] }
 }
def runTheTestCode(){
   //do some test stuff here.
   return  render( contentType: "text/html", data: "\n\nthis is the message that will be returned from the curl call.\n", status: 200);
}



preferences {
    page(name: "mainPage");
}

def mainPage() {
	def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);
	// def temporaryChildDimmer
    // if(!settings.dimmer)
    // {
    // 	//if the user has not selected a dimmer (which should only happen when this mainPage is displayed for the first time,when the user is first installing an instance of this SmartApp (because we are designating the dimmer input as required)) (although I suppose it might also happen if the dimmer that had been previouisly selected had been deleted.)
    //     //create a child device that is a virtual dimmer switch.
    //     //This child device will serve as the default value for the dimmer input.  If the user selects some other device, we will turn around and delete the child device.
    //     temporaryChildDimmer =   
    //         addChildDevice(
    //             /*namespace: */           "smartthings",
    //             /*typeName: */            "Virtual Dimmer Switch",     // How is the SmartThings platform going to decide which device handler to use in the case that I have a custom device handler with the same namespace and name?  Is there any way to specify the device handler's guid here to force the system to use a particular device handler.
    //             /*deviceNetworkId: */     "virtualDimmerForAggregate" + "-" + getUniqueIdRelatedToThisInstalledSmartApp()  , //how can we be sure that our deviceNetworkId is unique?  //should I be generating a guid or similar here.
    //             /*hubId: */               theHub?.id,
    //             /*properties: */          [completedSetup: true, label: "DUMMY--" + myDateFormat.format(myDate)]
    //         );
    //    log.debug("just created a child device: " + temporaryChildDimmer);
    //    //doesn't work: (although does not throw error: ")settings.dimmer = temporaryChildDimmer?.deviceNetworkId
    //    //dimmer = temporaryChildDimmer?.deviceNetworkId
    // }
    
    // I want to create the illusion of populating the dimmer preference automatically with a child device in the case where the user has 
    // not selected a dimmer explicitly.  I would naively expect to be able to do this with a "defaultValue" argument to the input() function.
    // Unfortunateley, the input() function iognores the defaultValue argument when the type is a capability (i.e. a device), and it does not seem that
    // there is any way to programmatically set the dimmer preference (the user has to select a device in order for dimmer not to be null).
    // To create the illusion of a default value for the dimmer preference, I will rely on the fact that the "Description" argument to the input function is 
    // displayed where the value would be if the user had selected a value.  The Description argument acts a bit like hint text in a text field on an html form 
    // (a light gray text that gets automatically overwritten when the user begines typing in the field).
    // In the case where the user has not selected dimmer (which, for the purposes of this smart app, means that we will use a child device instead of a manually selected dimmer).

    dynamicPage(
    	name: "mainPage", 
        title: "", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?") : yes
        install: true, 
        
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: true //getChildDevices( /*includeVirtualDevices: */ true ).isEmpty() 
        /*|++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
         *|   we will, in some cases, override the "uninstall: false" by invoking remove() below to 
         *|   display the remove button. if there are no child devices, we will 
         *|   display the remove button. if there are child devices, we will not 
         *|   display the remove button; rather we will display an href to another 
         *|   preferences page whose dynamic function will run DeleteChildDevice(..., 
         *|   true) . (The second argument to DeleteChildDevice is not documented, but 
         *|   it appears to be some sort of directive to forcefully delete the device 
         *|   even if the device is used by other smartapps.) All this special 
         *|   treatment of child devices is necessary during the app removal process 
         *|   because, the automatic removal of child devices that SmartThings 
         *|   performs during the standard SmartApp removal procedure will fail (and 
         *|   hence the removal of the SmartApp will fail) if the child devices happen 
         *|   to be in use by other SmartApps. Perhaps we should try to make the 
         *|   special preferences page sequence for child device deletion behave like 
         *|   the default Removal button, in that it will give the user an opportunity 
         *|   to back out (an "are you sure?" message). Also, perhaps it would be a 
         *|   good idea to first attempt the child device removal without the second 
         *|   argument to the deleteChildDevice function, and then if that fails 
         *|   (which we will detect in a try...catch structure) (there seems to be no 
         *|   reliable way to predict whether the DeleteChildDevice() method without the second argument will fail 
         *|  , so we can't warn the user ahead of time in 
         *|   a single warning message that would serve as both an "Are you sure you 
         *|   want to remove this smartapp?" warning and the "some child devices are 
         *|   in use by other smartapps, so are you really sure?" warning message, which would be ideal. 
         */


    ) {
    	section(/*"label"*/) {
            //label( title: "Assign a name", required: false, defaultValue: (new Date()).getTime());
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of the aggregate-dimmer SmartApp", 
                required: false, 
                defaultValue: "aggregate-dimmer--" + myDateFormat.format(myDate)
            );
            //input("foo", type:"text", title:"foo", required:false)
        }
    	section(/*"input"*/) {
            input(
                name: "dimmer", 
                title: "dimmer that this SmartApp will watch:" ,
                type: "capability.switchLevel", 
                description: (getAllChildDevices().isEmpty() ? "NEW CHILD DEVICE" : "CHILD DEVICE: \n" + getAllChildDevices().get(0).toString() ),            
                required:false,
                submitOnChange:true //we want to reload the page whenever this prerference changes, because we need to give mainPage a chance to either show or not show the deviceName input according to whether we will be creating a child device (which depends on whether the user has selected a device)
                //defaultValue: temporaryChildDimmer //(temporaryChildDimmer ? temporaryChildDimmer.deviceNetworkId : settings.dimmer.deviceNetworkId)
            )
            if(!settings.dimmer && getAllChildDevices().isEmpty()) //if there is no selected dimmer input and there are no child devices (which, as it happens, are precisely the conditions under which we will create a new child device)
            {
            	input(
                	name: "labelForNewChildDevice", 
                    title: "Specify a label to be given to the new child device", 
                    type:"text",
                    // defaultValue: myDate.getHours().toString() + myDate.getMinutes().toString() + "-" + myDate.getSeconds().toString() , //getUniqueIdRelatedToThisInstalledSmartApp() + "-1", 
                    defaultValue: "virtualDimmer--" + myDateFormat.format(myDate),
                    required: false
                )
            } 
            
            if(settings.dimmer)
            {
            	paragraph ( "To create a new virtual dimmer as a child device, and use it as the dimmer that this SmartApp will watch, set the above input to be empty.");
            }
            
        }

        section(/*"output"*/) {
            input(
            	title: "switches that this smartApp will control:",
                name:"switches", 
                type:"capability.switch", 
                description: "select several switches (2 or more for good effect)",
                multiple:true,
                required:false
            )
        }
        //I would like to disallow, or at least warn about, selecting as an output the very dimmer that we are using as an input.
        
       // section("Devices Created") {
       //     paragraph "${getAllChildDevices().inject("") {result, i -> result + (i.toString() + "\n")} ?: ""}"            
       // }
       section("") {
        	mode( title: "Set for specific mode(s)", required: false);
        }
    }
}

String getUniqueIdRelatedToThisInstalledSmartApp()
{
    // java.util.regex.Pattern x = new  java.util.regex.Pattern();
    // java.util.regex.Pattern myPattern = java.util.regex.Pattern.compile("(?<=_)([0123456789abcdef]+)(?=@)");
    // def myMatcher= myPattern.matcher((String) this);
    def myMatcher= ((String) this) =~ "(?<=_)([0123456789abcdef]+)(?=@)";
    //myMatcher.find();
    //return myMatcher.group();
    return myMatcher[0][1];

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def uninstalled() {
	log.trace "uninstalling and deleting child devices"
    getAllChildDevices().each {
        log.trace "deleting child device " + it.toString();
       deleteChildDevice(it.deviceNetworkId)
    }
}

def updated() {
	log.debug "Updated with settings: ${settings}"
  	//log.debug "myLabel: ${myLabel}"
	unsubscribe()
	initialize()
}

def initialize() {
	//if dimmer is null (i.e. no existing dimmer switch was selected by the user),
    // then ensure that a child device dimmer exists (create it if needed), and subscribe to its events.
	def deviceNetworkId="virtualDimmerForAggregate" + "-" + getUniqueIdRelatedToThisInstalledSmartApp();
	log.debug("deviceNetworkId: " + deviceNetworkId);
    log.debug "getFullApiServerUrl(): " + getFullApiServerUrl();
    if(! state.accessToken){ 
        // state.accessToken = createAccessToken(); 
    }
    log.debug "state.accessToken: " + state.accessToken;
    log.debug "cloud-based url to run the test code: " + getFullApiServerUrl() + "/" + "runTheTestCode" + "?access_token=" + state.accessToken;
    log.debug "local url to run the test code: " + getFullLocalApiServerUrl() + "/" + "runTheTestCode" + "?access_token=" + state.accessToken;
    def dimmerToWatch
    if(settings.dimmer)
    {
    	dimmerToWatch = dimmer;
        
        //delete all child devices that might happen to exist
        getAllChildDevices().each {
            //unsubscribe(it);
            if(it.deviceNetworkId != dimmerToWatch.deviceNetworkId) //this guards against the edge case wherein the user has selected the child device of this SmartApp.
           	{
            	deleteChildDevice(it.deviceNetworkId, true);
            }
        };
        
    } else {
        if(getAllChildDevices().isEmpty())
        {
        	dimmerToWatch = 
                addChildDevice(
                    /*namespace: */           "hubitat",//"smartthings",
                    /*typeName: */            "Virtual Dimmer",     // How is the SmartThings platform going to decide which device handler to use in the case that I have a custom device handler with the same namespace and name?  Is there any way to specify the device handler's guid here to force the system to use a particular device handler.
                    /*deviceNetworkId: */     deviceNetworkId  , //how can we be sure that our deviceNetworkId is unique?  //should I be generating a guid or similar here.
                    /*hubId: */               settings.theHub?.id,
                    /*properties: */          [completedSetup: true, label: settings.labelForNewChildDevice]
                );
            log.debug("just created a child device: " + dimmerToWatch);
        } else 
        {
        	dimmerToWatch = childDevices.get(0);
                //To do: update the properties of dimmerToWatch, if needed, to ensure that the deviceName matches the user's preference
                // (because the user might have changed the value of the device name field.
                //oops -- there is no way to change the name of an existing child device here programmatically.
                // --allof the properties of the child device are read-only.
        }
    }
    
   subscribe(
       dimmerToWatch,
       "level",
       inputHandler
   ) 
   
   if(dimmerToWatch.hasCapability("Switch"))
   {
   	//we want to be able to intelligently deal with both the case where dimmer has and the case where dimmer does not have the Switch capability.
       	subscribe(
               dimmerToWatch,
               "switch",
               inputHandler
           ) 
   }
   // 
   // 
   // 
   // // for the essential function of this SmartApp, it really is not necessary to listen for events
   // // from the switches.  Our only interaction with the switches will be to send them on and off commands.
}

def inputHandler(event) {
	log.debug "inputHandler was called with ${event.name} ${event.value} ${event}"
    log.debug event.getDevice().toString() + ".currentValue(\"switch\"): " + event.getDevice().currentValue("switch") 
    log.debug event.getDevice().toString() + ".currentValue(\"level\"): " + event.getDevice().currentValue("level") 
    
    writeLevel(
    	(int) (
        	event.getDevice().currentValue("level") 
            * (event.getDevice().hasCapability("Switch") && (event.getDevice().currentValue("switch") == "off") ? 0 : 1)
        )
    ); 

    
}

/* writes the specified level ( an integer in the range 0, ..., 100 ) to the array of switches */
def writeLevel(int level)
{
	int numberOfSwitchesThatShouldBeOn = level/100 * switches.size()
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