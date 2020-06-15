metadata {
 definition (
  name: "Personal Logger Child Device",
  namespace: "neiljackson1984",
  author: "Neil Jackson",
        description: "a virtual dimmer to serve as an input for the user to create a log entry."

 ) {
        capability "Actuator"



        capability "Sensor"



  capability "Switch"



  capability "SwitchLevel"







    }

 preferences {

 }
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
    message += "this is a DeviceExecutor: " + (this.class.name == "com.hubitat.hub.executor.DeviceExecutor").toString() + "\n";


    message += "\n\n";
   return message;
}
def installed() {
 sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
 sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
 sendEvent(name: "supportedThermostatFanModes", value: ["auto"], displayed: false)
 sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
 sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
 sendEvent(name: "thermostatFanMode", value: "auto");
}
def updated() {
 log.debug("updated");
 installed()
 def returnValue = [];
 if(settings.physicalKeypadLock == "Yes" || settings.physicalKeypadLock == "No"){
  returnValue += zigbee.writeAttribute(
   zigbee.THERMOSTAT_USER_INTERFACE_CONFIGURATION_CLUSTER,
   0x01,
   DataType.ENUM8,
   ["Yes":1, "No":0][settings.physicalKeypadLock]
  ) + poll();
 }
 log.debug("returnValue: " + groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson( returnValue )));
 return returnValue;
}
def parse(description) {return null;}
def off() {log.debug "off"; sendEvent(name:"switch", value:"off"); return null;}
def on(){log.debug "on"; sendEvent(name:"switch", value:"on"); return null;}
def setLevel(level, duration=null){sendEvent(name: "level", value: level); return null;}
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
