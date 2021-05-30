metadata {
	definition (
		name: "ddwrt-router", 
		namespace: "neiljackson1984", 
		author: "Neil Jackson", 
        description: "A driver for your dd-wrt router, to perform reboot."
	) {
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform the platform that this device has commands     
        //attributes: (none)
        //commands:  (none)
        
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform the platform  that this device has attributes     
        //attributes: (none)
        //commands:  (none)

        command "reboot"
    	
        //attribute("testEndpointResponse", "string"); //this is for debugging.
    }

	preferences {
        input( 
            name: "urlOfRouter",
            title: "URL of router", 
            type: "text",
            description: "Enter the URL of your dd-wrt router", 
            defaultValue: getSetting('urlOfRouter')
        );

        input( 
            name: "usernameOfRouter",
            title: "Username to authenticate into router", 
            type: "text",
            description: "Enter your username.", 
            defaultValue: getSetting('usernameOfRouter')
        );

        input( 
            name: "passwordOfRouter",
            title: "Password to authenticate into router", 
            type: "text",
            description: "Enter your password.", 
            defaultValue: getSetting('passwordOfRouter')
        );

        input( 
            name: "holdOffDuration",
            title: "Duration (in seconds) to wait before sending the reboot command to the router.  (A short delay is useful if the router that we are rebooting is the same router carrying the internet traffic that is allowing you to communicate with the Hubitat hub.", 
            type: "decimal",
            description: "Enter the duration", 
            defaultValue: getSetting('holdOffDuration')
        );
	}  
}

def mainTestCode(){
	def message = ""

	message += "\n\n";

    message += "settings.urlOfRouter: " + settings.urlOfRouter + "\n";
    message += "settings.usernameOfRouter: " + settings.usernameOfRouter + "\n";
    message += "settings.passwordOfRouter: " + settings.passwordOfRouter + "\n";

    message += "getSetting('urlOfRouter'): " + getSetting('urlOfRouter') + "\n";
    message += "getSetting('usernameOfRouter'): " + getSetting('usernameOfRouter') + "\n";
    message += "getSetting('passwordOfRouter'): " + getSetting('passwordOfRouter') + "\n";
    state.keySet().each{ state.remove(it) } 
    // ensureChildDevicesExistAndAreCorrectlyLabeled();

   return message;
}


    
private getSetting(nameOfSetting){
    return settings?.containsKey(nameOfSetting) ? settings[nameOfSetting] : getDefaultSettings()[nameOfSetting];
}

private Map getDefaultSettings(){
    def defaultSettings =  [:];
    
    //fill in all the default settings that are declared in the configurationModel.
    //configurationModel.each{defaultSettings[it.key] = it.value.defaultValue};
    
    //explicitly declare any other default settings
    // it is expected that the default settings that you explicitly declare here are not directly 
    // related to the device's internal non-volatile settings.
    defaultSettings += ['urlOfRouter'          :    'http://192.168.1.1'];
    defaultSettings += ['usernameOfRouter'     :    'root'];
    defaultSettings += ['passwordOfRouter'     :    ''];
    defaultSettings += ['holdOffDuration'      :    0.0];
    
    return defaultSettings;
}

private String convertCommandNameToDeviceNetworkIdOfChildDevice(String commandName){
    return "${device.deviceNetworkId}-${commandName}"
}

private String convertDeviceNetworkIdOfChildDeviceToCommandName(String deviceNetworkIdOfChildDevice){
    return deviceNetworkIdOfChildDevice.split("-")[-1] 
}

private String convertCommandNameToDesiredLabelForChildDevice(String commandName){
    return "$device.displayName $commandName"
}

// encoding the command name in the network id of the child device is probably pretty crude, 
// but it serves the immediate purpose of being able to identify which child device's 'push' 
// command we are processing.

// We would like to run ensureChildDevicesExistAndAreCorrectlyLabeled() whenever the 
// device label might have changed, which happens (if changed manually by the user)
// when the user clicks the "save device" button in the ui.  Unfortunately,
// there does not seem to be any way to hook into the "save device" event.  See:
// https://community.hubitat.com/t/driver-save-device-hook/36835/7
// as a crappy hack, I am calling ensureChildDevicesExistAndAreCorrectlyLabeled() (via initialize())
// when updated() is fired (which only happens when the user updates the preferences -- does it also happen on 
// programmatic changing of the preferences?)
// I guess that trying to keep the labels of the child devices slaved to the
// label of the parent device is somehow bad.  

private void ensureChildDevicesExistAndAreCorrectlyLabeled() {
    state.oldLabel = device.label
    commandNamesForWhichWeWantChildDevices = ["reboot"]
    List unassociatedChildDevices = childDevices.collect() 
    // the .collect() makes a clone of the list
    // initiall, unassociatedChildDevices is all of the child devices.
    // we will go through the list of commands, and attempt to 
    // identify a matching child device for each command.
    // when we identify a matching child device, we will remove it from the list of 
    // unassociatedChildDevices.  When we have gone through all commands,
    // unassociatedChildDevices will contain precisely the child devices that we want to delete.


    for (commandName in commandNamesForWhichWeWantChildDevices) {
        def childDevice = unassociatedChildDevices.find{ convertDeviceNetworkIdOfChildDeviceToCommandName(it.deviceNetworkId) == commandName }
        if (childDevice) { unassociatedChildDevices.removeElement(childDevice) }

        if (!childDevice) {
            childDevice = addChildDevice(
                /* namespace:        */   "neiljackson1984"                       ,  
                /* typeName:         */   "ddwrt-child-virtual-button"                    , 
                /* deviceNetworkId:  */   convertCommandNameToDeviceNetworkIdOfChildDevice(commandName)      , 
                ///* hubId:            */   null                                    ,         
                /* properties:       */  
                   [ //  I suspect that none of these properties have any special meaning for hubitat (they did for 
                       
                        //completedSetup: true, 
                        //label: convertCommandNameToDesiredLabelForChildDevice(commandName),
                        isComponent: true, 
                        //componentName: commandName,  
                        //componentLabel: commandName
                   ]
            );
            if(childDevice){log.debug("created a new child device for the command " + commandName + ".")}
            else{ log.debug("failed to create a new child device for the command " + commandName + ".") }
        }
        if(childDevice){
            childDevice.setLabel(convertCommandNameToDesiredLabelForChildDevice(commandName))
            childDevice.initialize()
        };
    }

    log.debug("now attemping to delete ${unassociatedChildDevices.size()} child devices that do not seem to be associated with any existing commands.")
    unassociatedChildDevices.each{ deleteChildDevice(it.deviceNetworkId) }

}





//LIFECYCLE FUNCTION
void installed() {
	log.debug("installed");
    initialize();
}

//LIFECYCLE FUNCTION
List<String>  updated() {
	log.debug("updated");
    initialize();
    return [];
}

private void initialize(){
    ensureChildDevicesExistAndAreCorrectlyLabeled();
}

//LIFECYCLE FUNCTION
List<Map> parse(description) {
    log.debug("parse was called with description ${description}.  This should not have happened, because I am a virtual device.");
    return [];
}

//old version of reboot command:
List<String> reboot_v1(){
    log.debug("reboot");

    String authorizationHeaderValue = 'Basic ' + (getSetting('usernameOfRouter') + ":" + getSetting('passwordOfRouter')).bytes.encodeBase64().toString()

    String commandToRunInRouterShell = 

    //log.debug("authorizationHeaderValue: " + authorizationHeaderValue)

    Map requestParams = [
        uri: getSetting('urlOfRouter') + "/apply.cgi",
        headers: [
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': authorizationHeaderValue
        ],
        contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
        requestContentType: groovyx.net.http.ContentType.URLENC, //this influences how the system treats the body of the request.   
        body: [
            "action": "Reboot"
        ]
    ];

    httpPost(requestParams,
        {response ->
            log.debug("response received from request to reboot: ${response.status} ${response.data}" )
        }
    );

    // to-do clean up handling of the response. 
    // we wouold like to porovide ther user some indication of whether the reboot attempt was succesful.
    // (of course, this is largely moot, because the usual use-case will be rebooting a router that itself is providing the
    // user's connection to the hubitat -- in which case rebooting the router will cause a radio silence from the hub that
    // wouild prevent the user from receiving the information, even if we carefully prepared it here.
    // One thing we should detect and inform the user about is a failure of authentication.




    return [];
}

//custom command 
List<String> reboot(){
    log.debug("reboot");

    String commandToRunInRouterShell = "logger \"hubitat is initiating reboot of the router\"; sleep 5; reboot"
    runCommandInRouterShell(commandToRunInRouterShell)

    return [];
}

List<String> runCommandInRouterShell(commandToRunInRouterShell){
    log.debug("running the following command in the router's shell: " + commandToRunInRouterShell);

    String authorizationHeaderValue = 'Basic ' + (getSetting('usernameOfRouter') + ":" + getSetting('passwordOfRouter')).bytes.encodeBase64().toString()

    //log.debug("authorizationHeaderValue: " + authorizationHeaderValue)

    Map requestParams = [
        uri: getSetting('urlOfRouter') + "/apply.cgi",
        headers: [
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': authorizationHeaderValue
        ],
        contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
        requestContentType: groovyx.net.http.ContentType.URLENC, //this influences how the system treats the body of the request.   
        body: [
            'submit_button'   : 'Ping',
            'action'          : 'ApplyTake',
            'submit_type'     : 'start',
            'change_action'   : 'gozila_cgi',
            'next_page'       : 'Diagnostics.asp',
            'ping_ip'         : commandToRunInRouterShell
        ]
    ];

    httpPost(requestParams,
        {response ->
            log.debug("response received from request to reboot: ${response.status} ${response.data}" )
        }
    );

    // to-do clean up handling of the response. 
    // we wouold like to porovide ther user some indication of whether the reboot attempt was succesful.
    // (of course, this is largely moot, because the usual use-case will be rebooting a router that itself is providing the
    // user's connection to the hubitat -- in which case rebooting the router will cause a radio silence from the hub that
    // wouild prevent the user from receiving the information, even if we carefully prepared it here.
    // One thing we should detect and inform the user about is a failure of authentication.




    return [];
}

void childPush(String deviceNetworkIdOfChildDevice){
    log.debug "childPush($deviceNetworkIdOfChildDevice)"
    device."${convertDeviceNetworkIdOfChildDeviceToCommandName(deviceNetworkIdOfChildDevice)}"()

}




//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).

/**

 * If you are including this in an app, you must have the following mapping

 * declared: 

 * mappings {

 *      path("/runTheTestCode") { action: [GET:"runTheTestCode"] }

 * }

 * 

 * 

 * If you are including this in a driver, you must declare the following command:

 * command "runTheTestCode"

 * //Actually, it appears not to be necessary to declare "runTheTestCode" as a command,

 * // but still not a bad idea.

**/


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
            
            // in the case where e is a groovy.lang.GroovyRuntimeException, invoking e.getStackTrace() causes a java.lang.SecurityException 
            // (let's call it e1) to be 
            // thrown, saying that 
            // we are not allowed to invoke methods on class groovy.lang.GroovyRuntimeException.
            // The good news is that we can succesfully call e1.getStackTrace(), and the 
            // returned value will contain all the information that we had been hoping to extract from e.getStackTrace().
            // oops -- I made a bad assumption.  It turns out that e1.getStackTrace() does NOT contain the information that we are after.
            // e1.getStackTrace() has the file name and number of the place where e.getStackTrace(), but not of anything before that.
            //So, it looks like we are still out of luck in our attempt to get the stack trace of a groovy.lang.GroovyRuntimeException.

            def stackTrace;
            try{ stackTrace = e.getStackTrace();} catch(java.lang.SecurityException e1) {
                stackTrace = e1.getStackTrace();
            }

            for(item in stackTrace)
            {
                stackTraceItems << item;
            }


            def filteredStackTrace = stackTraceItems.findAll{ it['fileName']?.startsWith("user_") };
			
			//the last element in filteredStackTrace will always be a reference to the line within the runTheTestCode() function body, which
			// isn't too interesting, so we get rid of the last element.
			if(!filteredStackTrace.isEmpty()){
				filteredStackTrace = filteredStackTrace.init();  //The init() method returns all but the last element. (but throws an exception when the iterable is empty.)
			}
            
            // filteredStackTrace.each{debugMessage += it['fileName'] + " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n";   }
            filteredStackTrace.each{debugMessage += " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n";   }
                 
        } catch(ee){ 
            debugMessage += "encountered an exception while trying to investigate the stack trace: \n${ee}\n";
            // debugMessage += "ee.getProperties(): " + ee.getProperties() + "\n";
            // debugMessage += "ee.getProperties()['stackTrace']: " + ee.getProperties()['stackTrace'] + "\n";
            debugMessage += "ee.getStackTrace(): " + ee.getStackTrace() + "\n";
            
            
            // // java.lang.Throwable x;
            // // x = (java.lang.Throwable) ee;
            
            // //debugMessage += "x: \n${prettyPrint(x.getProperties())}\n";
            // debugMessage += "ee: \n" + ee.getProperties() + "\n";
            // // debugMessage += "ee: \n" + prettyPrint(["a","b","c"]) + "\n";
            // //debugMessage += "ee: \n${prettyPrint(ee.getProperties())}\n";
        }
        
        // debugMessage += "filtered stack trace: \n" + 
            // groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(filteredStackTrace)) + "\n";
    
        debugMessage += "\n"
        return respondFromTestCode(debugMessage);
    }
}

def respondFromTestCode(message){
    switch(this.class.name){
        case "com.hubitat.hub.executor.AppExecutor":
            return  render( contentType: "text/html", data: message, status: 200);
            break;
        case "com.hubitat.hub.executor.DeviceExecutor": 
            sendEvent( name: 'testEndpointResponse', value: message )
            return null;
            break;
        default: break;
    }
}
