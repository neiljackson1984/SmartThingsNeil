/**
 * personal-logger.groovy
 *
 * An app for collecting numerical information (events with a single integer
 * paramater) that has to be manually reported by a human being.  This is useful
 * for logging pain, perhaps or the ingestion of certain things (drugs, food,
 * etc), or the completion of certain actions that would be hard to detect
 * automatically.  This app is particularly designed for cases where the human
 * wants to make a periodic report (for instance, once every n hours, report how
 * many times x happened since the last report). The app will prompt the human
 * (via TTS devices or similar) to make the report, and will become more
 * aggressive with the prompts until the report is given.
 *
 * the criteria for when the app will prompt the human for the report should be
 * flexible and adaptive (not just a prompt at 9:00 am every day , for instance)
 * We want the app to detect when the human has first woken up in the morning
 * (by looking at motion sensor events, perhaps), and issue a prompt shortly
 * after wakeup.
 *
 * This app creates a virtual device that has the dimmer interface. The human
 * logs an event by setting the dim level of the device to something other than
 * zero.  The app will log this event by adding a line to a google sheet (or
 * maybe some more sophisticated web-based log). (TO DO: cache the log entries
 * in order to handle cases where the logging service is unavailable -- save up
 * the log entries until the logging service is available, and notify the user
 * if log entries are lost.
 *
 *
 *
 *
 *
 **/

/** 
 * Interestingly, it seems that (this.class.name ==
 * "com.hubitat.hub.executor.AppExecutor") evaluates to true not only during the
 * normal execution of the app, but also during initialization of the app (the
 * thing that the hubitat does when it first receives an app's code).
 * Presumably, a similar condition would apply when initializing a driver's
 * code.  This could provide a way to have a single groovy file that would be,
 * simultaneously, a valid app and a valid driver.  Such a hack might prove
 * useful for bundling an app and an associated driver into a single file.
 * However, that single file would still have to be inserted into two different
 * places in the Hubitat system, so perhaps there is not much to be gained
 * anyway.
 **/
// if(this.class.name == "com.hubitat.hub.executor.AppExecutor"){}


//to acknowledge input, we will set the level of the input device to NULL_LEVEL.
// If Alexa sees that the value of a dimmer is 55 and then you ask her to set 
// the value to 55, she will not do anything (or, more likely, Alexa herself does something
// but the Alexa Hubitat app doesn't do anything).
// This is another reason why we need to have a level that we regard as "NULL" (i.e. nothing happening).
#define NULL_LEVEL (100)
 

definition(
    name: "Personal Logger",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: "Logs personal events",
    iconUrl: "",
    iconX2Url: "")

mappings {
     path("/runTheTestCode") { action: [GET:"runTheTestCode"] }
}

def mainTestCode(){
	def message = ""

	message += "\n\n";
 
   return message;
}


preferences {
    page(name: "pageOne")
}

def pageOne(){
    def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);
    
    dynamicPage(
    	name: "pageOne", 
        title: "", 
        // "allow the app to be installed from this page?" (i.e. "show the OK/Install button?") : yes
        install: true, 
        
        // "allow the app to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: true 
    ) {
    	section(/*"label"*/) {
            //label( title: "Assign a name", required: false, defaultValue: (new Date()).getTime());
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of the personal-logger app", 
                required: false, 
                defaultValue: "personal-logger --" + myDateFormat.format(myDate)
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
                    defaultValue: "personal logger " + myDateFormat.format(myDate),
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
            	title: "speech synthesis devices for notification:",
                name:"speechSynthesizers", 
                type:"capability.speechSynthesis", 
                description: "select any number of speech synthesis devices to be used for notifications and prompts.",
                multiple:true,
                required:false
            )
            input(
            	title: "logging destination url:",
                name:"logDestinationUrl", 
                type:"text", 
                description: "Insert the URL that you generated by following the instructions at https://wp.josh.com/2014/06/04/using-google-spreadsheets-for-logging-sensor-data/",
                required:false
            )

        }
    }
}

String getUniqueIdRelatedToThisInstalledSmartApp(){
    // java.util.regex.Pattern x = new  java.util.regex.Pattern();
    // java.util.regex.Pattern myPattern = java.util.regex.Pattern.compile("(?<=_)([0123456789abcdef]+)(?=@)");
    // def myMatcher= myPattern.matcher((String) this);
    def myMatcher= ((String) this) =~ "(?<=_)([0123456789abcdef]+)(?=@)";
    //myMatcher.find();
    //return myMatcher.group();
    return myMatcher[0][1];
}

//LIFECYCLE FUNCTION
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

//LIFECYCLE FUNCTION
def uninstalled() {
	log.trace "uninstalling and deleting child devices"
    getAllChildDevices().each {
        log.trace "deleting child device " + it.toString();
       deleteChildDevice(it.deviceNetworkId)
    }
}

//LIFECYCLE FUNCTION
def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	//if dimmer is null (i.e. no existing dimmer switch was selected by the user),
    // then ensure that a child device dimmer exists (create it if needed), and subscribe to its events.
	def deviceNetworkId="virtualDimmerForLogger" + "-" + getUniqueIdRelatedToThisInstalledSmartApp();
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
        
    } else {
        if(getAllChildDevices().isEmpty()){
        	dimmerToWatch = 
                addChildDevice(
                    /*namespace: */           "neiljackson1984",//"smartthings",
                    /*typeName: */            "Personal Logger Child Device",     // How is the SmartThings platform going to decide which device handler to use in the case that I have a custom device handler with the same namespace and name?  Is there any way to specify the device handler's guid here to force the system to use a particular device handler.
                    /*deviceNetworkId: */     deviceNetworkId  , //how can we be sure that our deviceNetworkId is unique?  //should I be generating a guid or similar here.
                    /*hubId: */               settings.theHub?.id,
                    /*properties: */          [
                                                    isComponent: false,
                                                    // name: "", 
                                                    label: settings.labelForNewChildDevice, 
                                                    completedSetup: true
                                              ]
                );
            log.debug("just created a child device: " + dimmerToWatch);
        } else {
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
        //we want to be able to intelligently deal with both the case where dimmer
        //has and the case where dimmer does not have the Switch capability.
       	subscribe(
               dimmerToWatch,
               "switch",
               inputHandler
           ) 
   }

   speak("welcome");
}

def prettyPrint(x){
    return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(x));
}

def inputHandler(event) {
    // log.debug "inputHandler was called with ${event.name} ${event.value} ${event}"
	// speak("inputHandler was called with ${event.name} ${event.value}")

    log.debug event.getDevice().toString() + ".currentValue(\"level\"): " + event.getDevice().currentValue("level") 
    if(event.name  == "level"){
        int eventValue = event.value as Integer
        if (eventValue != NULL_LEVEL){
            // log.debug("eventValue.class: " + eventValue.class )
            // log.debug("event fields: " +  event.class.getDeclaredFields().collect{it.toString()}.join("\n"))
            // mappifiedEvent = event.class.getDeclaredFields().collectEntries{ [(it.toString()): event.getProperty(it.toString())] }
            
            
            // mappifiedEvent = event.class.getDeclaredFields()
            //     .findAll { !it.synthetic }
            //     .collectEntries { field ->
            //         [field.name, 
            //             "b" //event."$field.name"
            //         ]
            //     };
            // fieldNames = event.class.getDeclaredFields().collectEntries{ [(it.name): it.fieldAccessor]}
            // firstField = event.class.getDeclaredFields().first()
            // log.debug("firstField fields: " + firstField.class.getDeclaredFields().collect{it.toString()}.join("\n")  )
            // // log.debug("fieldNames: " + prettyPrint(fieldNames))
            // log.debug("fieldNames: " + fieldNames)
            // mappifiedEvent = [];

            // log.debug("serialized event: " +  prettyPrint(mappifiedEvent))
            // mappifiedEvent = [
            //         "SOURCE_LOCATION",
            //         "SOURCE_DEVICE",
            //         "SOURCE_APP",
            //         "SOURCE_HUB",
            //         "id",
            //         "archivable",
            //         "data",
            //         "date",
            //         "descriptionText",
            //         "displayed",
            //         "source",
            //         "isStateChange",
            //         "displayName",
            //         "name",
            //         "value",
            //         "unit",
            //         "description",
            //         "translatable",
            //         "type",
            //         "deviceId",
            //         "locationId",
            //         "hubId",
            //         "installedAppId",
            //         "device",
            //         //"location",
            //         "dataString"
            //     ]
            //     .collectEntries { fieldName ->
            //         [fieldName, 
            //             event."$fieldName"
            //         ]
            //     };
            // log.debug("serialized event: " +  prettyPrint(mappifiedEvent))
            // log.debug("serialized event: " +  prettyPrint(['a':1,'b':2]))
            // log.debug("prettyPrint event: " +  prettyPrint(event))
            
            if(settings.logDestinationUrl){
                httpPost(
                    [
                        'uri': settings.logDestinationUrl,
                        'body' : [
                            'date': event.getDate(),
                            'timestamp': event.getUnixTime(),
                            'value': eventValue
                        ],
                        'contentType':groovyx.net.http.ContentType.TEXT,
                        'requestContentType': groovyx.net.http.ContentType.URLENC    
                    ],
                    {response1 ->
                        String response1Text = response1.data.getText();
                        log.debug("response1.contentType: " + response1.contentType);
                        log.debug("response1Text.length(): " + response1Text.length());
                        log.debug("response1Text: " + response1Text);
                    }
                );
            }
            sleep(1000);
            // speak(event.getDevice().toString() + " " + eventValue);
            speak("log" + " " + eventValue);
            event.getDevice().setLevel(NULL_LEVEL);
        }
    }
}

def speak(message){
    log.debug("speaking " + ((String) message))
    for (speechSynthesizer in speechSynthesizers){
        speechSynthesizer.speak((String) message);
    }
}

//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"