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
// the value to 55, she will not do anything (or, more likely, Alexa herself
// does something but the Alexa Hubitat app doesn't do anything). This is
// another reason why we need to have a level that we regard as "NULL" (i.e.
// nothing happening).
#define NULL_LEVEL (100)
#define LOG_PURGE_THRESHOLD (20)

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

    message += "initially, state.outbox is : " + state.outbox + "\n";

    // submitLogEntry("foo");

    long currentUnixTime = now();
    Date currentDate = new Date(currentUnixTime);

    // state.remove('outbox');
    // state.remove('log');
    // submitLogEntry([
    //             'date': currentDate,
    //             'timestamp': currentUnixTime,
    //             'value': "bogus"
    //         ]);

    // syncTheLog();

    // message += "finally, state.outbox is : " + state.outbox + "\n";
    // message += "state.foo is : " + state.foo + "\n";

    // message += "ahoy\n";

    // // java.time.Duration periodOffsetDuration = java.time.Duration.ofHours(1) ; // reporting periods turn over at localMidnight + periodOffsetDuration.

    // // reporting periods turn over at localMidnight

    // // java.time.Duration      periodBoundaryOffsetFromLocalMidnight = java.time.Duration.ofHours(1);
    // java.time.LocalTime     periodBoundary = java.time.LocalTime.parse("10:15")

    // java.time.Duration      naggingGraceDuration  = java.time.Duration.ofHours(9) ; // we will commence nagging if no report has been submitted after instantOfEndOfLastReportedPeriod + naggingGraceDuration.
    // // we are trying to cajole the user to achieve at least one report during each period.
    // java.time.Instant       currentInstant        = java.time.Instant.now();
    // java.time.Instant       instantOfLastReport   = java.time.Instant.ofEpochMilli(state.timestampOfLastReport);
    // java.time.ZoneId        localZoneId           = java.time.ZoneId.of(location.timeZone.getID());
    // java.time.ZonedDateTime currentZonedDateTime  = java.time.ZonedDateTime.ofInstant(currentInstant, localZoneId);
    // // java.time.Instant       instantOfLastLocalMidnight = currentZonedDateTime.truncatedTo(java.time.temporal.ChronoUnit("DAYS")).toInstant();
    // // java.time.Instant       instantOfLastLocalMidnight = currentZonedDateTime.truncatedTo(java.time.temporal.ChronoUnit("DAYS")).toInstant();
    // // java.time.Instant       instantOfLastLocalMidnight = currentZonedDateTime.truncatedTo(java.time.temporal.ChronoUnit.DAYS).toInstant(); // causes an exception: Expression [ClassExpression] is not allowed: java.time.temporal.ChronoUnit 
    // // java.time.Instant       instantOfLastLocalMidnight = currentZonedDateTime.truncatedTo(java.time.Duration.ofDays(1)).toInstant(); // does not woirk
    // java.time.Instant       instantOfLastLocalMidnight = currentZonedDateTime.withNano(0).withSecond(0).withMinute(0).withHour(0).toInstant();
    
    
    // java.time.Instant       instantOfLocalMidnightFollowingLastReport = java.time.ZonedDateTime.ofInstant(
    //     instantOfLastReport, 
    //     localZoneId
    // ).withNano(0).withSecond(0).withMinute(0).withHour(0).plusDays(1).toInstant();
    
    // java.time.Instant       instantOfPeriodBoundaryOnDayOfLastReport = java.time.ZonedDateTime.ofInstant(instantOfLastReport, localZoneId).with(periodBoundary).toInstant();
    // java.time.Instant       instantOfPeriodBoundaryOnDayBeforeDayOfLastReport = java.time.ZonedDateTime.ofInstant(instantOfLastReport, localZoneId).with(periodBoundary).minusDays(1).toInstant();
    
    // java.time.Instant       instantOfPeriodBoundaryAtStartOfPeriodContainingLastReport = ( instantOfPeriodBoundaryOnDayOfLastReport <= instantOfLastReport ? instantOfPeriodBoundaryOnDayOfLastReport : instantOfPeriodBoundaryOnDayBeforeDayOfLastReport );
    // java.time.Instant       instantOfPeriodBoundaryAtEndOfPeriodContainingLastReport = java.time.ZonedDateTime.ofInstant(instantOfPeriodBoundaryAtStartOfPeriodContainingLastReport, localZoneId).plusDays(1).toInstant();

    // java.time.Instant       instantOfNagStart = instantOfPeriodBoundaryAtEndOfPeriodContainingLastReport.plus(naggingGraceDuration)


    // java.time.Instant       instantOfEndOfPeriodContainingInstantOfLastReport = ())
    // if (!(instantOfEndOfPeriodContainingInstantOfLastReport > instantOfLastReport)){
    //     instantOfEndOfPeriodContainingInstantOfLastReport
    // }
    
    // // java.time.Instant       instantOfNagStart = instantOfLocalMidnightFollowingLastReport.plus(naggingGraceDuration)
    // java.time.Instant       instantOfNagStart = java.time.Instant.ofEpochMilli(state.instantOfNagStartToEpochMilli)
    // java.time.Instant       b = java.time.Instant.ofEpochMilli(1644166700000)
    


    // message += "instantOfNagStart:  " + instantOfNagStart + "\n";
    // message += "state.instantOfNagStartToEpochMilli:  " + state.instantOfNagStartToEpochMilli + "\n";
    
    // state.remove('instantOfNagStart');
    // state.instantOfNagStartToEpochMilli = instantOfNagStart.toEpochMilli()
    // java.time.Instant a = ( (java.time.Instant) state.instantOfNagStart)
 

    // // def a = java.lang.Enum.valueOf(java.time.temporal.ChronoUnit, "DAYS")
    // // def a = java.time.Duration.ofDays(1)
    // // def b = java.time.temporal.ChronoUnit.valueOf("DAYS")

    // // want to find local midnight that followed instantOfLastReport.
    // // java.time.Instant nagStartTime = ;
    // // java.time.LocalDateTime currentLocalDateTime = ;

    // message += "periodOffsetDuration:                       " + periodOffsetDuration + "\n";
    // message += "naggingGraceDuration:                       " + naggingGraceDuration + "\n";
    // message += "currentInstant:                             " + currentInstant + "\n";
    // message += "instantOfLastReport:                        " + instantOfLastReport + "\n";
    // message += "location.timeZone:                          " + location.timeZone + "\n";
    // message += "localZoneId:                                " + localZoneId + "\n";
    // message += "currentZonedDateTime:                       " + currentZonedDateTime + "\n";
    // message += "instantOfLastLocalMidnight:                 " + instantOfLastLocalMidnight + "\n";
    // message += "instantOfLocalMidnightFollowingLastReport:  " + instantOfLocalMidnightFollowingLastReport + "\n";
    // message += "a:  " + a + "\n";
    // message += "b:  " + b + "\n";
    // message += "state.instantOfNagStartToEpochMilli:  " + state.instantOfNagStartToEpochMilli + "\n";
    // message += "(b < instantOfNagStart):  " + (b < instantOfNagStart) + "\n";
    // message += "(b == instantOfNagStart):  " + (b == instantOfNagStart) + "\n";

    // java.io.ByteArrayOutputStream x = new java.io.ByteArrayOutputStream()
    // // java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(x)
    // def oos = new java.io.ObjectOutputStream(x)
    // oos.writeObject(instantOfLocalMidnightFollowingLastReport)
    // oos.close()
    // message += "x.toByteArray():  " + x.toByteArray() + "\n";

    // TODO: handle the case where timestampOfLastReport is null.
    // Date lastReportTime = new Date(state.timestampOfLastReport);

    // java.time.Duration periodOffset = java.time.Duration.ofHours(9) ; // reporting periods turn over at localMidnight + periodOffset.
    // def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    // myDateFormat.setTimeZone(location.timeZone);

    // message += "ahoxxxy\n";
    // message += myDateFormat.format(myDate) + "\n";
 
    // state.remove('timestampOfLastReport')
    // state.remove('instantOfNAgStart')
    // state.remove('instantOfNagStartToEpochMilli')
    // state.remove('lastReportTime')
    // state.remove('nagStartTime')
    // state.remove('nextNagTime')
    updateNagging();
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
        title: "Personal Logger", 
        // "allow the app to be installed from this page?" (i.e. "show the OK/Install button?") : yes
        install: true, 
        
        // "allow the app to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: true 
    ) {
    	section() {
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of this app", 
                required: false, 
                defaultValue: "personal-logger --" + myDateFormat.format(myDate)
            );
            input(
                name: "foo", 
                type: "text", 
                title: "this is the title", 
                description: "this is the description",
                // defaultValue: "this is the default value",
                required:false
            
            );
        }



    	section(/*"input"*/) {
            input(
                name: "dimmer", 
                title: "dimmer that this SmartApp will watch:" ,
                type: "capability.switchLevel", 
                description: (getAllChildDevices().isEmpty() ? "NEW CHILD DEVICE" : "CHILD DEVICE: \n" + getAllChildDevices().get(0).toString() ),            
                required:false,
                submitOnChange:true 
                
                // we want to reload the page whenever this preference changes,
                // because we need to give mainPage a chance to either show or
                // not show the deviceName input according to whether we will be
                // creating a child device (which depends on whether the user
                // has selected a device)
            )
            if(!settings.dimmer){ 
                //if there is no selected dimmer input (i.e. if we will be creating and a managing a (virtual) dimmer as a child device)
            	input(
                	name: "preferredLabelForChildDevice", // "labelForNewChildDevice", 
                    title: "Specify a label for the child device", 
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
                                                    label: settings.preferredLabelForChildDevice, 
                                                    completedSetup: true
                                              ]
                );
            log.debug("just created a child device: " + dimmerToWatch);
        } else {
        	dimmerToWatch = childDevices.get(0);
            //To do: update the properties of dimmerToWatch, if needed, to ensure that the deviceName matches the user's preference
            // (because the user might have changed the value of the device name field.
            if(dimmerToWatch.label != settings.preferredLabelForChildDevice){
                dimmerToWatch.setLabel(settings.preferredLabelForChildDevice);
            }
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
            submitLogEntry([
                'date': event.getDate(),
                'timestamp': event.getUnixTime(),
                'value': eventValue
            ]);

            java.time.Instant       instantOfLastReport   = java.time.Instant.ofEpochMilli(event.getUnixTime());
            state.instantOfLastReportToEpochMilli = instantOfLastReport.toEpochMilli();
            speak("log" + " " + eventValue);
            updateNagging();
            event.getDevice().setLevel(NULL_LEVEL);
        }
    }
}

def nag() {
    speak("please report.");
}

def updateNagging() {
    java.time.Instant currentInstant        = java.time.Instant.now();
    java.time.Instant instantOfLastReport   = java.time.Instant.ofEpochMilli(state.instantOfLastReportToEpochMilli);

    java.time.LocalTime     periodBoundary        = java.time.LocalTime.parse("00:00") ;
    java.time.Duration      naggingGraceDuration  = java.time.Duration.ofHours(9) ;
    // java.time.Duration      naggingGraceDuration  = java.time.Duration.ofMinutes(2) ;
    java.time.Duration      nagInterval           = java.time.Duration.ofMinutes(10) ;
    // these three parameters ought to be settings rather than hardcoded here.
    
    java.time.ZoneId        localZoneId           = java.time.ZoneId.of(location.timeZone.getID());

    java.time.Instant       instantOfPeriodBoundaryOnDayOfLastReport = java.time.ZonedDateTime.ofInstant(instantOfLastReport, localZoneId).with(periodBoundary).toInstant();
    java.time.Instant       instantOfPeriodBoundaryOnDayBeforeDayOfLastReport = java.time.ZonedDateTime.ofInstant(instantOfLastReport, localZoneId).with(periodBoundary).minusDays(1).toInstant();
    java.time.Instant       instantOfPeriodBoundaryAtStartOfPeriodContainingLastReport = ( instantOfPeriodBoundaryOnDayOfLastReport <= instantOfLastReport ? instantOfPeriodBoundaryOnDayOfLastReport : instantOfPeriodBoundaryOnDayBeforeDayOfLastReport );
    java.time.Instant       instantOfPeriodBoundaryAtEndOfPeriodContainingLastReport = java.time.ZonedDateTime.ofInstant(instantOfPeriodBoundaryAtStartOfPeriodContainingLastReport, localZoneId).plusDays(1).toInstant();
    java.time.Instant       instantOfNagStart = instantOfPeriodBoundaryAtEndOfPeriodContainingLastReport.plus(naggingGraceDuration)
    
    
    //just for diagnostics:
    

    java.time.Instant instantOfNextNag;
    if (currentInstant >= instantOfNagStart){ // we should consider putting a bit of fudge in this comparison.
        nag();
        log.debug("nagged at " + java.time.ZonedDateTime.ofInstant(currentInstant, localZoneId).toString());
        instantOfNextNag = currentInstant + nagInterval
    } else {
        instantOfNextNag = instantOfNagStart
    }


    log.debug(
        ""
        + "lastReportTime: " + java.time.ZonedDateTime.ofInstant(instantOfLastReport, localZoneId).toString() + "\n"
        + "nagStartTime: " + java.time.ZonedDateTime.ofInstant(instantOfNagStart, localZoneId).toString() + "\n"
        + "nextNagTime: " + java.time.ZonedDateTime.ofInstant(instantOfNextNag, localZoneId).toString() + "\n"
    );


    runInMillis( 
        java.time.Duration.between(java.time.Instant.now(), instantOfNextNag).toMillis(),
        'updateNagging'
    );
}

def getNewLogEntryId() {
    def newId = (state.lastAssignedLogEntryId ?: 0) + 1;
    state.lastAssignedLogEntryId = newId;
    //Is this thread safe?  Perhaps we should use guids or some sort of semaphore arrangement.

    return newId;
}

def submitLogEntry(payload){
    // push logEntry into the buffer of logEntries to be stored in the off-site
    // database, then trigger the mechanism that will process the buffer and
    // (attempt) to send the messages. we ought to do everything here
    // atomically, in a 'thread-safe' way, since we are manipulating a single
    // repository of data that is shared by potentially multiple runs of this
    // app running simualtneously.
    
    // payload.date = "" + payload.date;
    logEntry = [id: getNewLogEntryId(), payload: payload, committedToDatabase: false, failedTransmissionCount: 0];

    if(!state.log){state.log = [];}
    state.log << logEntry;

    runIn(
        //Long delayInSeconds
        2, 
        
        //String handlerMethod
        "syncTheLog"
    );
    

}

def syncTheLog(){
    log.debug("syncTheLog")
    
    for (logEntry in state.log.findAll({ ! it.committedToDatabase }) ){
        log.debug("now sending the following logEntry to the database: " + logEntry);
        log.debug("logEntry.payload.date.getProperties()['class']: " + logEntry.payload.date.getProperties()['class']);

        // log.debug("after fixing, logEntry.payload.date.getProperties()['class']: " + logEntry.payload.date.getProperties()['class']);

        if(!settings.logDestinationUrl){continue;}
        Map requestBody=[ 'failedTransmissionCount' : logEntry.failedTransmissionCount ] + logEntry.payload;

        log.debug("requestBody: " + (new groovy.xml.XmlUtil()).escapeXml(prettyPrint(requestBody)));

        Map paramsForPost = [
                'uri': settings.logDestinationUrl,
                'body' :  requestBody,
                //'body' :  logEntry.payload,
                // we insert a little bit of protocol metadata into the data that we are sending to the database, to assist debugging efforts.

                'contentType':groovyx.net.http.ContentType.TEXT,
                'requestContentType': groovyx.net.http.ContentType.URLENC    
            ];
        Map callbackData = [logEntry:logEntry];
        
        // asynchttpPost(

        //     // String callbackMethod = null 
        //     // The name of a callback method to send the response to. Can be null if the response can be ignored.
        //     'httpPostCallback',

        //     // Map params
        //     paramsForPost,

        //     // Map data = null
        //     //optional data to be passed to the callback method.
        //     callbackData

        // );
        //asyncHttpPost seems to be converting each value in the params map to a string, which 
        // means that we lose the automatic processing of non-string bodies that
        // is done by the regular httpPost() function.  This alone is something we could
        // woprk around.  The fatal flaw with asyncHttpPost for the current application is
        // that ayncHttpPost passes to the callback a 'response' of type hubitat.scheduling.AsyncResponse.
        // which seems to be missing much of the functionality of the groovyx.net.http.HttpResponseDecorator
        // that the regular httpPost() function passes to its callback.  One example of the difference between
        // hubitat.scheduling.AsyncResponse and groovyx.net.http.HttpResponseDecorator is that, when the 
        // server responds to the request with a redirect, the AsyncResponse object only tells about the redirect message,
        // whereas the responseDecorator object lets us see the whole history of the http transaction including 
        // what happened after the redirect.  
        // actually, it seems that even the object passed to the callback by the regular httpPost function 
        // does not have the ability to retrieve the details about the http transaction after the redirect.

        try{
            httpPost(paramsForPost,  { httpPostCallback(it, callbackData); } );
        } catch(ee){
            log.debug("encountered an exception while trying to send the data to the database: \n${ee}\n");
            logEntry.failedTransmissionCount++;
        }
        log.debug("httpPost completed");
    }

    //if any logEntries remain uncommitted, schedule another running of syncTheLog to occur in a while
    if( state.log.count( {! it.committedToDatabase} )){
        log.debug("some log entries remain uncommitted so we will try again in a while.");
        runIn(30, "syncTheLog");
    } else {
        log.debug("all entries have been succesfully committed.");
    }


}

// void httpPostCallback(groovyx.net.http.HttpResponseDecorator response, Map data) {
// declaring response to have type groovyx.net.http.HttpResponseDecorator causes the following compile-time 
// (upload-time) error that looks like: Expression [VariableExpression] is not allowed: response at line number 441
// the line number is that of the line where we first use the response variable within the function body.
void httpPostCallback( response, Map data=[:]) {
    // when we are being called as a callback for httpPost(), then response is of type groovyx.net.http.HttpResponseDecorator
    // On the other hand, when we are being called as a callback for asynchttpPost(), then response is of type hubitat.scheduling.AsyncResponse
    
    
    log.debug("httpPostCallback");

    if(response.isSuccess()){
        //  mark the logEntry as haivng been succesfully sent to the database
        state.log.find({it.id == data.logEntry.id}).committedToDatabase = true;
        pruneTheLocalLog()
    } else {
        //increment the log entry's failedTransmissionCount counter
        state.log.find({it.id == data.logEntry.id}).failedTransmissionCount++;
    }


    log.debug("response.getProperties()['class']: " + response.getProperties()['class']);
    
    // if('' + response.getProperties()['class'] == 'class groovyx.net.http.HttpResponseDecorator'){
    //     log.debug("response.getProperties(): " + (new groovy.xml.XmlUtil()).escapeXml("" + response.getProperties()));
    // } else {
    //     // log.debug("response.getProperties(): " + '<code>' + (new groovy.xml.XmlUtil()).escapeXml(prettyPrint(response.getProperties())) + '</code>');
    //     log.debug("response.getProperties(): " + '<code>' + (new groovy.xml.XmlUtil()).escapeXml(groovy.json.JsonOutput.toJson((response.getProperties())) + '</code>');
    // }
    // log.debug("response.getProperties(): " + prettyPrint(response.getProperties()));
    // log.debug("response.getProperties(): " + (new groovy.xml.XmlUtil()).escapeXml(prettyPrint(response.getProperties())));
    // log.debug("response.getClass(): " + response.getClass());

    log.debug(
        "response.getProperties(): " 
        + '<code>' 
        + (new groovy.xml.XmlUtil()).escapeXml(
            // groovy.json.JsonOutput.toJson(response.getProperties())
            "\n\n" + response.getProperties().collect({ key, value -> key + ": " + value}).join("\n\n")
        )
        + '</code>'
    );

    // log.debug(
    //     "response.class.getProperties(): " 
    //     + '<code>' 
    //     + (new groovy.xml.XmlUtil()).escapeXml(
    //         // groovy.json.JsonOutput.toJson(response.getProperties())
    //         "\n\n" + response.getProperties()['class'].getProperties().collect({ key, value -> key + ": " + value}).join("\n\n")
    //     )
    //     + '</code>'
    // );

    // log.debug(
    //     "response.getProperties()['class'].getProperties()['methods']: " 
    //     + '<code>' 
    //     + (new groovy.xml.XmlUtil()).escapeXml(
    //         // groovy.json.JsonOutput.toJson(response.getProperties())
    //         "\n\n" + response.getProperties()['class'].getProperties()['methods'].collect({ value -> "" + value}).join("\n")
    //     )
    //     + '</code>'
    // );

    // log.debug(
    //     "response.getData(): " 
    //     + '<code>' 
    //     + (new groovy.xml.XmlUtil()).escapeXml(
    //         response.getData()
    //     )
    //     + '</code>'
    // );

    String responseText;
    responseText = response.data.getText();
    log.debug("response.contentType: " + response.contentType);
    log.debug("responseText.length(): " + responseText.length());
    log.debug("responseText: " + (new groovy.xml.XmlUtil()).escapeXml(responseText));
}

def speak(message){
    log.debug("speaking " + ((String) message))
    for (speechSynthesizer in speechSynthesizers){
        speechSynthesizer.speak((String) message);
    }
}

def pruneTheLocalLog(){
    // delete any log entries that are committed, except for the most recent LOG_PURGE_THRESHOLD committed entries.
    def countOfCommittedLogEntries = state.log.count( { it.committedToDatabase } )
    def numberOfLogEntriesToDelete = Math.max( 0, countOfCommittedLogEntries - LOG_PURGE_THRESHOLD )

    def indicesOfLogEntriesToBeDeleted = [];

    int i=0;
    int numberOfLogEntriesDeleted = 0;
    while(i < state.log.size && numberOfLogEntriesDeleted < numberOfLogEntriesToDelete){
        if ( state.log[i].committedToDatabase ){
            state.log.remove(i);
            numberOfLogEntriesDeleted++;
        } else {
            i++;
        }
    }
}

//////////////////////////////////////////////////



def getAppsCodeForLoggingToGoogleSheets(){
//This is the google apps code that is to be pasted into the embedded script of a google sheet
// in order to enable logging (in case the publicly-available template is taken down for some reason)
//see instructions at https://wp.josh.com/2014/06/04/using-google-spreadsheets-for-logging-sensor-data/ 
return '''

// Format a string into text for the HTML response

function out( s ) {
  
  return ContentService.createTextOutput(s).setMimeType(ContentService.MimeType.TEXT);
  
}


function doPost(e) { 
  
//  Logger.clear();
      
  var ssID = ScriptProperties.getProperty('targetSpreadsheetID');
  
  if (ssID == null ) {
    
    return( out( "Property targetSpreadsheetID not found. Be sure to run Setup script."));
    
  }
  
//  Logger.log("Spreadsheet ID=%s", ssID );  
  
  var ss = SpreadsheetApp.openById( ssID );
  
  if (ss == null ) { 
    
    return( out( "Could not find spreadsheet ID ["+ssID+"]. Aborting."));
    
  }
  
  
  var sheetName = ScriptProperties.getProperty('targetSheetName');
  
  if (sheetName == null ) {
    
    return( out( "Property targetSheetName not found. Be sure to run Setup script."));
    
  }
  
  
//  Logger.log( "Target Sheet Name=%s" , sheetName );  
  
  var sheet = ss.getSheetByName(sheetName);
  
  // No such thing as Spreadsheet.getSheetById()? Really?
  
  if (sheet == null ) {
    
    
    return( out( "Could not find sheet named  ["+sheetName+"]. Aborting."));
    
  }
  
  
    
  var parameters = e.parameter;   // Grab the  parameters from the request
  
  //begin section added by Neil 2020-12-24
  parameters['recorded_in_log_timestamp']=(new Date()).valueOf();
  //end section added by Neil 2020-12-24
  
  var headers;
    
  if ( sheet.getLastColumn() == 0 ) {   // Special case of empty sheet...
    
    headers = [ ];
    
  } else {
    
    headers = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];  //read headers from top row of spreadsheet    
    
  }
    
  
  var newHeadersFlag = false; 
      
  var newRow = [];     // Hold new row to be Added to bottom
           
  for (var p in parameters) { // loop through the request parameter names (keys) and put them in the right column in spreadsheet
    
//    Logger.log( " parameter key=%s"  ,  p );
                 
    var col = headers.indexOf( p );    // Find column for the param name 
    
    if ( col <0 ) {  // if matching col header not found
      
      // add new column at end           
      
      headers.push(p);
      
      col = headers.indexOf( p );    // Find column for the param name       
      
      newHeadersFlag = true;
                  
//      Logger.log( "New col=%s" ,  col );
      
      // Note that it appears if you send multipule params with the smae name that only the first one shows up
      
    }
        
    newRow[col]  = parameters[ p ];    // Lookup value of the passed param and put it into the new row we are building
        
//    Logger.log( " col=%s value=%s", col  , newRow[col] );
    
  }
  
  if (newRow.length==0) {
    
     return( out( "No parameters found, no data appended."));      // Nessisary becuase appending a blank row causes a Sheet Serivce Error after the script completes
    
  }   
  
  if (newHeadersFlag) {   // We updated some headers, so reflect in the sheet
    
    var headersRange = [ headers ]; // Must be 2 dimensional array to set a range
    
    sheet.getRange( 1, 1, 1 , headers.length ).setValues( headersRange );
    
  }
    
  sheet.appendRow(newRow);   // Append new row to end of spreadsheet  
    
  return( out( "Data appended successfully.") );
  
}

function doGet(e) {
  return(doPost(e));
}



function setupLoggingToCurrentSheet() {
  ScriptProperties.setProperty('targetSpreadsheetID', SpreadsheetApp.getActiveSpreadsheet().getId());
  ScriptProperties.setProperty('targetSheetName', SpreadsheetApp.getActiveSpreadsheet().getActiveSheet().getSheetName());      
}


function onOpen() {
  SpreadsheetApp.getActive()
  .addMenu("Setup Logging",
           [{name: "Setup Script", 
             functionName: "setupLoggingToCurrentSheet"}]);
}


'''
}



//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"