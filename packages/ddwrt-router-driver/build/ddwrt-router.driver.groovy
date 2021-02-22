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
    
    return defaultSettings;
}





//LIFECYCLE FUNCTION
void installed() {
	log.debug("installed");
    
}

//LIFECYCLE FUNCTION
List<String>  updated() {
	log.debug("updated");

    return [];
}

//LIFECYCLE FUNCTION
List<Map> parse(description) {
    log.debug("parse was called with description ${description}.  This should not have happened, because I am a virtual device.");
    return [];
}


//custom command 
List<String> reboot(){
    log.debug("reboot");

    String authorizationHeaderValue = 'Basic ' + (getSetting('usernameOfRouter') + ":" + getSetting('passwordOfRouter')).bytes.encodeBase64().toString()

    log.debug("authorizationHeaderValue: " + authorizationHeaderValue)

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




    return [];
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
