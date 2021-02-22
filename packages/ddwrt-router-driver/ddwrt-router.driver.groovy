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
#include "debugging.lib.groovy"