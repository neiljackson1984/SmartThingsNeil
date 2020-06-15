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
    message += "this: " + this.dump() + "\n";
    message += "this.class: " + this.class + "\n";
    message += "\n\n";
    message += "this.class.getDeclaredFields(): " + "\n";
    this.class.getDeclaredFields().each{message += it.toString() + "\n"; }
    message += "\n\n";
    message += "this.class.getMethods(): " + "\n";
    this.class.getMethods().each{ message += it.toString() + "\n";}
 message += "\n\n";
    message += "this.class: " + this.class + "\n";
    message += "this.class.name: " + this.class.name + "\n";
    message += "this is an AppExecutor: " + (this.class.name == "com.hubitat.hub.executor.AppExecutor").toString() + "\n";
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
        install: true,
        uninstall: true
    ) {
     section( ) {
            label(
             title: "label:",
                description: "Assign a label for this instance of the personal-logger app",
                required: false,
                defaultValue: "personal-logger --" + myDateFormat.format(myDate)
            );
        }
     section( ) {
            input(
                name: "dimmer",
                title: "dimmer that this SmartApp will watch:" ,
                type: "capability.switchLevel",
                description: (getAllChildDevices().isEmpty() ? "NEW CHILD DEVICE" : "CHILD DEVICE: \n" + getAllChildDevices().get(0).toString() ),
                required:false,
                submitOnChange:true
            )
            if(!settings.dimmer && getAllChildDevices().isEmpty())
            {
             input(
                 name: "labelForNewChildDevice",
                    title: "Specify a label to be given to the new child device",
                    type:"text",
                    defaultValue: "virtualDimmer--" + myDateFormat.format(myDate),
                    required: false
                )
            }
            if(settings.dimmer)
            {
             paragraph ( "To create a new virtual dimmer as a child device, and use it as the dimmer that this SmartApp will watch, set the above input to be empty.");
            }
        }
        section( ) {
            input(
             title: "speech synthesis devices for notification:",
                name:"speechSynthesizers",
                type:"capability.speechSynthesis",
                description: "select any number of speech synthesis devices to be used for notifications and prompts.",
                multiple:true,
                required:false
            )
        }
    }
}
String getUniqueIdRelatedToThisInstalledSmartApp(){
    def myMatcher= ((String) this) =~ "(?<=_)([0123456789abcdef]+)(?=@)";
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
 unsubscribe()
 initialize()
}
def initialize() {
 def deviceNetworkId="virtualDimmerForLogger" + "-" + getUniqueIdRelatedToThisInstalledSmartApp();
 log.debug("deviceNetworkId: " + deviceNetworkId);
    def dimmerToWatch
    if(settings.dimmer)
    {
     dimmerToWatch = dimmer;
        getAllChildDevices().each {
            if(it.deviceNetworkId != dimmerToWatch.deviceNetworkId)
            {
             deleteChildDevice(it.deviceNetworkId, true);
            }
        };
    } else {
        if(getAllChildDevices().isEmpty())
        {
         dimmerToWatch =
                addChildDevice(
                                              "hubitat",
                                              "Virtual Dimmer",
                                              deviceNetworkId ,
                                              settings.theHub?.id,
                                              [completedSetup: true, label: settings.labelForNewChildDevice]
                );
            log.debug("just created a child device: " + dimmerToWatch);
        } else
        {
         dimmerToWatch = childDevices.get(0);
        }
    }
   subscribe(
       dimmerToWatch,
       "level",
       inputHandler
   )
   if(dimmerToWatch.hasCapability("Switch"))
   {
        subscribe(
               dimmerToWatch,
               "switch",
               inputHandler
           )
   }
   speak("welcome");
}
def inputHandler(event) {
 log.debug "inputHandler was called with ${event.name} ${event.value} ${event}"
    log.debug event.getDevice().toString() + ".currentValue(\"switch\"): " + event.getDevice().currentValue("switch")
    log.debug event.getDevice().toString() + ".currentValue(\"level\"): " + event.getDevice().currentValue("level")
    value = (int) (
         event.getDevice().currentValue("level")
            * (event.getDevice().hasCapability("Switch") && (event.getDevice().currentValue("switch") == "off") ? 0 : 1)
        );
    speak(value);
}
def speak(String message){
    for (speechSynthesizer in speechSynthesizers){
        speechSynthesizer.speak(message);
    }
}
def runTheTestCode(){
    try{
        return respondFromTestCode(mainTestCode());
    } catch (e)
    {
        def debugMessage = ""
        debugMessage += "\n\n" + "================================================" + "\n";
        debugMessage += (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n";
        debugMessage += "encountered an exception: \n${e}\n"
        try{
            def stackTraceItems = [];
            def stackTrace;
            try{ stackTrace = e.getStackTrace();} catch(java.lang.SecurityException e1) {
                stackTrace = e1.getStackTrace();
            }
            for(item in stackTrace)
            {
                stackTraceItems << item;
            }
            def filteredStackTrace = stackTraceItems.findAll{ it['fileName']?.startsWith("user_") };
   if(!filteredStackTrace.isEmpty()){
    filteredStackTrace = filteredStackTrace.init();
   }
            filteredStackTrace.each{debugMessage += " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n"; }
        } catch(ee){
            debugMessage += "encountered an exception while trying to investigate the stack trace: \n${ee}\n";
            debugMessage += "ee.getStackTrace(): " + ee.getStackTrace() + "\n";
        }
        debugMessage += "\n"
        return respondFromTestCode(debugMessage);
    }
}
def respondFromTestCode(message){
    switch(this.class.name){
        case "com.hubitat.hub.executor.AppExecutor":
            return render( contentType: "text/html", data: message, status: 200);
            break;
        case "com.hubitat.hub.executor.DeviceExecutor":
            sendEvent( name: 'testEndpointResponse', value: message )
            return null;
            break;
        default: break;
    }
}
