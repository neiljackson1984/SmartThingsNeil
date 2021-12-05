definition(
    name: "Thermostat Difference Injector",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: (
        ""
        + "Watches and controls an existing thermostat x.  Watches an existing " 
        + "thermometer y.  Creates a virtual thermostat z, which uses y as its " 
        + "thermometer.  Drives the setpoint of x in order to cause x's control " 
        + "error (difference between setpoint and actual) to always match z's " 
        + "control error.  This effectively causes the thermostat x to use the " 
        + "arbitrary temperature measurement device y, specified by you, instead " 
        + "of its own internal thermometer.  Conceivably, a thermostat could be " 
        + "designed to support this sort of external-thermometer functionality " 
        + "natively -- such thermostats undoubtedly exist.  However, if you want " 
        + "to achieve such functionality with a thermostat that does not " 
        + "natively support it, then this app will do the trick.  An enabling " 
        + "assumption here is that the transfer function of the thermostat x " 
        + "depends only on the error (and not, for instance, on the absolute " 
        + "value of the setpoint or the absolute value of the temperature " 
        + "measurement).  This assumption is probably not strictly true; for " 
        + "instance, in most cases, the thermostat x probably allows the " 
        + "setpoint to be set only within a limited allowed range.  However, the " 
        + "assumption is hopefully true enough to be useful, within the regime" 
        + "that you are interested in."
    ),
    iconUrl: "",
    iconX2Url: ""
)

def mainTestCode(){
	def message = ""
	message += "\n\n";

    // long currentUnixTime = now();
    // Date currentDate = new Date(currentUnixTime);

    def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);

    message += "ahoxxxy\n";
    message += myDateFormat.format(myDate) + "\n";
    message += java.util.UUID.randomUUID().toString() + "\n";
    message += app.getName() + "\n";
 
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
        title: "Preferences Page One", 
        install: true, 
        // "allow the app to be installed from this page?" (i.e. "show the OK/Install button?") : yes

        uninstall: true 
        // "allow the app to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
    ) {
    	section() {
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of this app", 
                required: false, 
                defaultValue: app.getName() + "--" + myDateFormat.format(myDate)
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
                name: "explicitlySpecifiedMasterThermostat", 
                title: "explicitly-specified master thermostat" ,
                type: "capability.thermostat", 
                // description: (getAllChildDevices().isEmpty() ? "NEW CHILD DEVICE" : "CHILD DEVICE: \n" + getAllChildDevices().get(0).toString() ),            
                description: (
                    ""
                    + "By default, this app will create a virtual thermostat as a "
                    + "child and will use that virtual thermostat as the master "
                    + "thermostat.  However, if you specify a thermostat here, "
                    + "then we will use your specified thermostat as the master "
                    + "thermostat."
                ),            
                required:false,
                
                // submitOnChange:true 
                // we want to reload the page whenever this preference changes,
                // because we need to give mainPage a chance to either show or
                // not show the deviceName input according to whether we will be
                // creating a child device (which depends on whether the user
                // has selected a device)
            )
           
            
            // if(settings.explicitlySpecifiedMasterThermostat)
            // {
            // 	paragraph ( "To create a new virtual dimmer as a child device, and use it as the dimmer that this SmartApp will watch, set the above input to be empty.");
            // }
            
            //specifications for the virtual thermostat:
            // we might consider hiding these settings somehow in the case where the user has explicitly specified a master thermostat, maybe.

            input(
                name: "preferredLabelForChildDevice", // "labelForNewChildDevice", 
                title: "Specify a label for the child device", 
                type:"text",
                defaultValue: app.getName() + " " + "child" + "--" + myDateFormat.format(myDate),
                required: false
            )

            
            input(
                name: "virtualThermostatThermometer", 
                title: "thermometer for virtual thermostat" ,
                type: "capability.temperatureMeasurement",            
                description: (
                    ""
                    + "The virtual thermostat created by this app shall use this temperature measurement device as its thermometer."
                ),            
                required:false,
            )
        }

        section(/*"output"*/) {
            input(
                name: "slaveThermostat", 
                title: "thermostat whose setpoint this app will drive." ,
                type: "capability.thermostat", 
                description: "thermostat whose setpoint this app will drive.",            
                required:true
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
    // figure out which thermostat is our master thermostat.
    // Our master thermostat shall be the thermostat that the user has explicitly 
    // speicified by means of the explicitlySpecifiedMasterThermostat or, if the user
    // has left that setting blank, then we will use a child virtual thermostat (creating it if necessary).

    def masterThermostat

    if (settings.explicitlySpecifiedMasterThermostat){
        masterThermostat = settings.explicitlySpecifiedMasterThermostat;

        //delete all child devices that might happen to exist
        getAllChildDevices().each {
            //unsubscribe(it);
            if(it.deviceNetworkId != masterThermostat.deviceNetworkId) //this guards against the edge case wherein the user has selected the child device of this SmartApp.
           	{
            	deleteChildDevice(it.deviceNetworkId, true);
            }
        }; 
    } else {
        if(getAllChildDevices().isEmpty()){
        	masterThermostat = 
                addChildDevice(
                    /*namespace: */           "neiljackson1984",//"smartthings",
                    /*typeName: */            "Thermostat Difference Injector Virtual Thermostat",     
                    /*deviceNetworkId: */     java.util.UUID.randomUUID().toString(),
                    /*hubId: */               settings.theHub?.id,
                    /*properties: */          [
                                                    isComponent: false,
                                                    // name: "", 
                                                    label: settings.preferredLabelForChildDevice, 
                                                    completedSetup: true
                                              ]
                );
            log.debug("just created a child device: " + masterThermostat);
        } else {
        	masterThermostat = childDevices.get(0);
            // update the properties of masterThermostat, if needed, to ensure
            // that the deviceName matches the user's preference (because the
            // user might have changed the value of the device name field.
            if(masterThermostat.label != settings.preferredLabelForChildDevice){
                masterThermostat.setLabel(settings.preferredLabelForChildDevice);
            }
        }

        //to do: ensure that the connection between the thermometer specified by the user
        // and the virtual thermostat is configured.
        subscribe(
            settings.virtualThermostatThermometer,
            "temperature",
            inputHandler
        )
    }

    subscribe(
        masterThermostat,
        "temperature",
        inputHandler
    )

    subscribe(
        masterThermostat,
        "thermostatSetpoint",
        inputHandler
    )

    subscribe(
        slaveThermostat,
        "temperature",
        inputHandler
    )

    subscribe(
        slaveThermostat,
        "thermostatSetpoint",
        inputHandler
    )
}


def inputHandler(event) {
    log.debug "inputHandler was called with ${event.name} ${event.value} ${event}"

}

//////////////////////////////////////////////////







//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"
#include "utility.lib.groovy"