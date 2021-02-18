metadata {
	definition (
		name: "Personal Logger Child Device", 
		namespace: "neiljackson1984", 
		author: "Neil Jackson", 
        description: "a virtual dimmer to serve as an input for the user to create a log entry."
	) {
        capability "Actuator"  //The "Actuator" capability is simply a marker to inform the platform that this device has commands     
        //attributes: (none)
        //commands:  (none)
        
        capability "Sensor"   //The "Sensor" capability is simply a marker to inform the platform  that this device has attributes     
        //attributes: (none)
        //commands:  (none)

		// capability "Switch"
        // //attributes: enum switch ("on", "off")
        // //commands: on(), off()

		capability "Switch"
		// attributes: switch
        // commands: 'on', 'off'


		capability "Configuration"
        
        command "getAssociationReport"
    	command "getVersionReport"
	    command "getCommandClassReport"
	    command "getParameterReport", [[name:"parameterNumber",type:"NUMBER", description:"Parameter Number (omit for a complete listing of parameters that have been set)", constraints:["NUMBER"]]]
	    command "setParameter",[[name:"parameterNumber",type:"NUMBER", description:"Parameter Number", constraints:["NUMBER"]],[name:"size",type:"NUMBER", description:"Parameter Size", constraints:["NUMBER"]],[name:"value",type:"NUMBER", description:"Parameter Value", constraints:["NUMBER"]]]
	

    }

	preferences {
		
	}  
}

def mainTestCode(){
	def message = ""

	message += "\n\n";

    message += "this.class: " + this.class + "\n";
    
    
	x = createEvent(name: "switch", value:  "on");
	message += "x.class: " + x.class + "\n";
	message += "x.class: " + x.getProperties()['class'] + "\n";
	message += "x: " + x.toString() + "\n";
	



	z0 = new hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport(value:0)
	message += "z0: " + z0.toString() + "\n";

	z1 = new hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport(value:1)
	message += "z1: " + z1.toString() + "\n";
	message += "z1.format(): " + z1.format() + "\n";

	y = response(z1)
	message += "y: " + y.toString() + "\n";
	message += "y.class: " + y.getProperties()['class'] + "\n";
	message += "y.getProperties(): " + y.getProperties() + "\n";
	message += "y.action.class: " + y.action.getProperties()['class'] + "\n";

	message += "zwave.parse(y.action): " + zwave.parse(y.action) + "\n";

	message += "\n\n";

   return message;
}


//LIFECYCLE FUNCTION
void installed() {
	// off();
	log.debug("installed");
}

//LIFECYCLE FUNCTION
List<String>  updated() {
	log.debug("updated");
}

//LIFECYCLE FUNCTION
List<Map> parse(description) {return null;}



/* off() is a command belonging to the capability "Switch"  */
List<String> off() {log.debug "off"; sendEvent(name:"switch", value:"off"); return null;}

/* on() is a command belonging to the capability "Switch".  */
List<String> on(){log.debug "on";  sendEvent(name:"switch", value:"on"); return null;}

/* configure() is a command belonging to the capability "Confioguration"  */
List<String> configure(){
	log.debug "configure";

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
