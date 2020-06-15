metadata {
 definition (
  name: "Personal Logger Child Device",
  namespace: "neiljackson1984",
  author: "Neil Jackson",
        description: "a virtual dimmer to serve as an input for the user to create a log entry."
 ) {
        capability "Actuator"



        capability "Sensor"







  capability "SwitchLevel"







    }

 preferences {

 }
}

def mainTestCode(){
 def message = ""

 message += "\n\n";

    message += "this.class: " + this.class + "\n";
    message += "\n\n";



   return message;
}



def installed() {

 setLevel(0);
}


def updated() {
 log.debug("updated");
 return null;
}



def parse(description) {return null;}
def setLevel(level, duration=null){
 log.debug("setLevel was called with " + ((String) level));
 sendEvent(name: "level", value: level); return null;
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
