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
    page(name: "mainPage");
    page(name: "removeWithChildDevicesPage1", install: false); 
    page(name: "removeWithChildDevicesPage2", install: false);
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
	
    // settings.foo = "ahoy there";
    // log.debug("settings: " + settings);


    dynamicPage(
    	name: "mainPage", 
        title: "", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?") : yes
        install: true, 
        
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: false //getChildDevices( /*includeVirtualDevices: */ true ).isEmpty() 
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
            

            //  input(
            //  	"theHub", 
            //      "hub", 
            //      title: "Select the hub (required for local execution) (Optional)", 
            //      multiple: false, 
            //      required: false
            //  )
        }

        section(/*"output"*/) {
            input(
            	title: "switches that this smartApp will control:",
                name:"switches", 
                type:"capability.Switch", 
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
        
        if(getAllChildDevices().isEmpty()) //if there are no child devices... 
        {
        	remove("Remove this SmartApp");  //show the remove button)
        } else
        {
        	section() {
                href(
                    name: "hrefToDeviceDeletionPage",
                    title: "REMOVE",
                    description: "Remove this SmartApp",
                    required: false,
                    page: "removeWithChildDevicesPage1"
                );
            }
        }
    }
    //settings.foo = "ahoy there"
}

def removeWithChildDevicesPage1()
{
    dynamicPage(
    	name: "removeWithChildDevicesPage1", 
         //title: "this is the title of removeWithChildDevicesPage1", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?")
        install: false, 
        
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: false,
        //we might override the "uninstall:false" by invoking remove() below to display the remove button.
        
        //nextPage: "mainPage"
    ) {
    	section(){
    		paragraph (
            	"In order to remove this SmartApp, we must first remove all of this SmartApp's child devices, namely: \n" + 
                "${getAllChildDevices().inject("") {result, i -> result + (i.toString() + "\n")} ?: ""}"
            );
            paragraph "Do you want to remove all the child devices?"
            href(
                title: "Yes, delete all child devices",
                description: "",
                required: false,
                page: "removeWithChildDevicesPage2"
            );
            // href(
            //     title: "No, go back to the main page",
            //     description: "",
            //     required: false,
            //     page: "mainPage"
            // );
        }
    }
}


def removeWithChildDevicesPage2()
{
    // unsubscribe()
    String report = "";
    
    
    try {
    	getAllChildDevices().each {
        	unsubscribe(it);
            deleteChildDevice(it.deviceNetworkId);
    	};
        report += "Succesfully deleted all child devices without having to resort to force delete.\n"
        
    } catch(e) {
    	report += "Encountered the error \"${e}\" while attempting to gently delete the child devices, so we are proceeding with forceful deletion\n"
        try{
        	getAllChildDevices().each {
            	unsubscribe(it);
                deleteChildDevice(it.deviceNetworkId, true)
            };
            report += "Succesfully deleted all child devices by resorting to forceful deletion.\n"
        } catch (ee)
        {
        	report += "Forceful deletion of all child devices failed, with error message ${ee}.\n"
        }
        
    }
    dynamicPage(
    	name: "removeWithChildDevicesPage2", 
        //title: "this is the title of removeWithChildDevicesPage2", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?")
        install: false,         
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: false,
        // we will probably be overridding the "uninstall:false" by invoking remove() below.
        //nextPage: "mainPage"
    ) {
    	section(){
    		paragraph report
        }
        remove("Remove")
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
	log.trace "uninstalling"
    //getAllChildDevices().each {
    //    //deleteChildDevice(it.deviceNetworkId, true)
    //    deleteChildDevice(it.deviceNetworkId)
    //}
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
    //try {log.debug "this: ${this}"} catch(e) {}
    //try {log.debug "this.class: " + this.class} catch(e) {}
    //try {log.debug "this: " + groovy.json.JsonOutput.toJson(this)} catch(e) {}
    //try {log.debug "this.name: " + this.name} catch(e) {}
    //try {log.debug "this.dump: " + this.dump()} catch(e) {}
    //try {log.debug "this.propertyNames(): " + this.propertyNames()} catch(e) {}
    //try {log.debug "this.getclass().getName()): " + this.getclass().getName()} catch(e) {}

	
	def deviceNetworkId="virtualDimmerForAggregate" + "-" + getUniqueIdRelatedToThisInstalledSmartApp();
	log.debug("deviceNetworkId: " + deviceNetworkId);
    
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
        
    } else 
    {
        if(getAllChildDevices().isEmpty())
        {
        	dimmerToWatch = 
                addChildDevice(
                    /*namespace: */           "smartthings",
                    /*typeName: */            "Virtual Dimmer Switch",     // How is the SmartThings platform going to decide which device handler to use in the case that I have a custom device handler with the same namespace and name?  Is there any way to specify the device handler's guid here to force the system to use a particular device handler.
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
   // // nevertheless, there might prove to be some secondary or diagnostic reason to listen to the switches 
   // // (make sure they turned on, logging, etc.), so I will subscribe to events from the switches here:
   // subscribe(
   //     switches,
   //     "switch",
   //     catchAllEventHandler
   // )


}


def catchAllEventHandler(event) {
    //log.debug "catchAllEventHandler was called with ${event.name} ${event.value}"
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