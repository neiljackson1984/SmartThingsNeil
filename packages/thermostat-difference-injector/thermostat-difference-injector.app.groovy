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

    // message += "ahoxxxy\n";
    // message += myDateFormat.format(myDate) + "\n";
    // message += java.util.UUID.randomUUID().toString() + "\n";
    // message += app.getName() + "\n";
    // message += "explicitlySpecifiedMasterThermostat?.class?.name: " + explicitlySpecifiedMasterThermostat?.class?.name + "\n";
    // // >>>  com.hubitat.app.DeviceWrapper
    
    // message += "childDevice.name: " + childDevice.name + "\n";
    // message += "childDevice.class.name: " + childDevice.class.name + "\n";
    // message += "childDevice.class.fields: " + childDevice.class.fields + "\n";
    

    // message += "childDevices[0].class.name: " + childDevices[0].class.name + "\n";   
    // message += "explicitlySpecifiedMasterThermostat.class.name: " + explicitlySpecifiedMasterThermostat.class.name + "\n";
    // message += "childDevices[0].class.superclass.name: " + childDevices[0].class.superclass.name + "\n";
    // message += "explicitlySpecifiedMasterThermostat.class.superclass.name: " + explicitlySpecifiedMasterThermostat.class.superclass.name + "\n";
    // // the above statements produced the below output:
    // //  childDevices[0].class.name: com.hubitat.app.ChildDeviceWrapper
    // //  explicitlySpecifiedMasterThermostat.class.name: com.hubitat.app.DeviceWrapper
    // //  childDevices[0].class.superclass.name: com.hubitat.app.DeviceWrapper
    // //  explicitlySpecifiedMasterThermostat.class.superclass.name: groovy.lang.GroovyObjectSupport
    // //
    // // Thus, we see that com.hubitat.app.ChildDeviceWrapper is a subclass of com.hubitat.app.DeviceWrapper


    // def childDevice = childDevices[0]
    // // message += "childDevice.class.class.dump(): " + childDevice.class.class.dump() + "\n";
    // message += "childDevice?.dump(): " + childDevice?.dump() + "\n";
    // // message += "childDevice.class.dump(): " + childDevice.class.dump() + "\n";
    // message += "explicitlySpecifiedMasterThermostat?.dump(): " + explicitlySpecifiedMasterThermostat?.dump() + "\n";
    // // message += "explicitlySpecifiedMasterThermostat.class.dump(): " + explicitlySpecifiedMasterThermostat.class.dump() + "\n";
    
    // // message += "childDevice.class.fields: " + childDevice.class.fields + "\n";
    // // message += "explicitlySpecifiedMasterThermostat.class.fields: " + explicitlySpecifiedMasterThermostat.class.fields + "\n";


    // message += "childDevice.class.getInterfaces(): " + childDevice.class.getInterfaces() + "\n";
    // message += "explicitlySpecifiedMasterThermostat?.class?.getInterfaces(): " + explicitlySpecifiedMasterThermostat?.class?.getInterfaces() + "\n";


    // // message += "getAllChildDevices().size(): " + getAllChildDevices().size() + "\n";
    // // message += "getChildDevices().size(): " + getChildDevices().size() + "\n";
    // // message += "childDevices.size(): " + childDevices.size() + "\n";
    
    // // x = explicitlySpecifiedMasterThermostat.currentValue('coolingSetpoint')
    // // y = explicitlySpecifiedMasterThermostat.currentValue('heatingSetpoint')
    // // z = explicitlySpecifiedMasterThermostat.currentValue('temperature')
    // // message += "x: ${x}" + "\n"
    // // message += "y: ${y}" + "\n"
    // // message += "z: ${z}" + "\n"
    // // message += "x.class.name: ${x.class.name}" + "\n"
 
    // message += "childDevice.getSupportedAttributes(): " + childDevice.getSupportedAttributes() + "\n"
    // // message += "capability.thermostatMode: " + capability.thermostatMode
    // message += "childDevice.getCapabilities(): " + childDevice.getCapabilities() + "\n"
    
    // com.hubitat.app.DeviceWrapper d = getMasterThermostat()
    // // com.hubitat.hub.domain.Capability c = castToCapability(d.getCapabilities().find{it.name == "ThermostatMode"} )
    // // produces "Expression [DeclarationExpression] is not allowed:..."
    // // def c   = castToCapability(d.getCapabilities().find{it.name == "ThermostatMode"} )
    
    // def a  = (com.hubitat.hub.domain.Capability) d.getCapabilities().find{it.name == "ThermostatMode"} 
    // def b  = (com.hubitat.hub.domain.Capability) d.getCapabilities().find{it.name == "Thermostat"} 
    // log.debug(a.attributes.toString())
    // log.debug(b.attributes.toString())

    // log.debug(thermostatModeCapability.attributes.toString())

    // com.hubitat.hub.domain.Capability thermostatModeCapability = childDevice.getCapabilities().find{it.name == "ThermostatMode"} 

    // message += "thermostatModeCapability: " + thermostatModeCapability + "\n"
    // message += "thermostatModeCapability.class.name: " + thermostatModeCapability.class.name + "\n"
    // message += "thermostatModeCapability.dump(): " + thermostatModeCapability.dump() + "\n"
    // def x = [5,6,7]
    // def y = [5,null]
    

    // // message += "x.average(): " + x.average() + "\n"
    // // message += "y.average(): " + y.average() + "\n"
    // message += "x.findAll({it != null}).with{sum()/size()}: " + (x.findAll({it != null}).with{sum()/size()}) + "\n"
    // message += "y.findAll({it != null}).with{sum()/size()}: " + (y.findAll({it != null}).with{sum()/size()}) + "\n"

    // def c = masterThermostat.currentValue('coolingSetpoint')    
    // def h = masterThermostat.currentValue('heatingSetpoint')    
    // def s = masterThermostat.currentValue('thermostatSetpoint')

    // message += "c?.dump(): " + c?.dump() + "\n"
    // message += "h?.dump(): " + h?.dump() + "\n"
    // message += "s?.dump(): " + s?.dump() + "\n"
    // message += "childDevices[15]: " + childDevices[15] + "\n"

    // def b = settings.masterThermometer.currentState("temperature")

    // def a = masterThermostat.currentState("temperature")
    // def b = masterThermostat.events()[0]
    // message += "a.dump(): " + a.dump() + "\n"
    // message += "b.dump(): " + b.dump() + "\n"
    // message += "a.class.name: " + a.class.name + "\n"
    // message += "b.class.name: " + b.class.name + "\n"
    // message += "a.class.superclass.name: " + a.class.superclass.name + "\n"
    // message += "b.class.superclass.name: " + b.class.superclass.name + "\n"
    // message += "a.value.class.name: " + b.value.class.name + "\n"
    // message += "b.value.class.name: " + b.value.class.name + "\n"
    // // the above statements produced the following output:
    // // a.dump(): <com.hubitat.hub.domain.State@4d70f0 date=2021-12-05 19:12:00.022 id=null name=temperature unit=F value=76 dataType=NUMBER deviceId=null>
    // // b.dump(): <com.hubitat.hub.domain.Event@1b5b92e id=146342 archivable=true data=null date=2021-12-05 19:12:00.022 descriptionText=null displayed=true source=DEVICE isStateChange=true displayName=null name=temperature 
    // // value=76 unit=F description=null translatable=false type=null deviceId=167 locationId=null hubId=null installedAppId=null device=null location=null dataString=null>
    // // a.class.name: com.hubitat.hub.domain.State
    // // b.class.name: com.hubitat.hub.domain.Event
    // // a.class.superclass.name: java.lang.Object
    // // b.class.superclass.name: java.lang.Object
    // // a.value.class.name: java.lang.String
    // // b.value.class.name: java.lang.String
    // // masterThermometer.dump(): <com.hubitat.app.DeviceWrapper@10eeab6 device=null deviceId=129>
    // // settings.masterThermometer.dump(): <com.hubitat.app.DeviceWrapper@10eeab6 device=null deviceId=129>


    // message += "masterThermometer.dump(): " + masterThermometer.dump() + "\n"
    // message += "settings.masterThermometer.dump(): " + settings.masterThermometer.dump() + "\n"
    
    
    message += "masterThermostat.currentState('thermostatFanMode').getValue(): " + masterThermostat.currentState('thermostatFanMode').getValue() + "\n"


    // message += "z: " + z + "\n"

   return message;
}

com.hubitat.hub.domain.Capability castToCapability(x){
    return x;
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
            // input(
            //     name: "explicitlySpecifiedMasterThermostat", 
            //     title: "explicitly-specified master thermostat" ,
            //     type: "capability.thermostat", 
            //     // description: (getAllChildDevices().isEmpty() ? "NEW CHILD DEVICE" : "CHILD DEVICE: \n" + getAllChildDevices().get(0).toString() ),            
            //     description: (
            //         ""
            //         + "By default, this app will create a virtual thermostat as a "
            //         + "child and will use that virtual thermostat as the master "
            //         + "thermostat.  However, if you specify a thermostat here, "
            //         + "then we will use your specified thermostat as the master "
            //         + "thermostat."
            //     ),            
            //     required:false,
                
            //     // submitOnChange:true 
            //     // we want to reload the page whenever this preference changes,
            //     // because we need to give mainPage a chance to either show or
            //     // not show the deviceName input according to whether we will be
            //     // creating a child device (which depends on whether the user
            //     // has selected a device)
            // )
           
            
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
                name: "masterThermometer", 
                title: "thermometer for virtual thermostat" ,
                type: "capability.temperatureMeasurement",            
                description: (
                    ""
                    + "The virtual thermostat created by this app shall use this temperature measurement device as its thermometer."
                ),            
                required:True,
            )
            // TODO: prevent the user from choosing the virtual thermostat as the thermometer.
            // possibly: warn the user about choosing the slave thermostat as the thermometer.
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

        section() {
            input(
                name: "minimumAllowedDelayBetweenSetpointSettingCommands", 
                title: " we will wait at least this long (milliseconds) between subsequent sendings of setpoint-setting commands to the slave thermostat. ", 
                type:"number",
                defaultValue: 30000,
                required: true
            )

        }

        section() {
            input(
                name: "minimumAllowedDelayBetweenModeSettingCommands", 
                title: " we will wait at least this long (milliseconds) between subsequent sendings of mode-setting commands to the slave thermostat. ", 
                type:"number",
                defaultValue: 30000,
                required: true
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


    if(masterThermostat == null){ 
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
    } 
    
    if(masterThermostat.label != settings.preferredLabelForChildDevice){
        masterThermostat.setLabel(settings.preferredLabelForChildDevice);
    }


    subscribe(masterThermostat, "temperature",               synchronizationPotentiallyNeededHandler           )
    subscribe(masterThermostat, "coolingSetpoint",           synchronizationPotentiallyNeededHandler           )
    subscribe(masterThermostat, "heatingSetpoint",           synchronizationPotentiallyNeededHandler           )
    // subscribe(masterThermostat, "thermostatSetpoint",        xxxxxxxx  )
    subscribe(masterThermostat, "thermostatFanMode",         synchronizationPotentiallyNeededHandler           )
    subscribe(masterThermostat, "thermostatMode",            synchronizationPotentiallyNeededHandler             )
    // subscribe(masterThermostat, "thermostatOperatingState",  yyyyyy   )


    subscribe(slaveThermostat, "temperature",               synchronizationPotentiallyNeededHandler            )
    subscribe(slaveThermostat, "coolingSetpoint",           synchronizationPotentiallyNeededHandler            )
    subscribe(slaveThermostat, "heatingSetpoint",           synchronizationPotentiallyNeededHandler            )
    // subscribe(slaveThermostat, "thermostatSetpoint",        xxx  )
    subscribe(slaveThermostat, "thermostatFanMode",         synchronizationPotentiallyNeededHandler          )
    subscribe(slaveThermostat, "thermostatMode",            synchronizationPotentiallyNeededHandler             )

    

    // respond to the latest events (actually states) so that we are up to date with the devices that we are watching.
    // the below subscriptions are to keep the (virtual, child) masterThermostat's virtual sensor value updated:
    masterThermometerTemperatureHandler(masterThermometer.currentState("temperature"))
    subscribe( masterThermometer, "temperature", masterThermometerTemperatureHandler )
    
    slaveThermostatThermostatOperatingStateHandler(slaveThermostat.currentState("thermostatOperatingState"))
    subscribe( slaveThermostat, "thermostatOperatingState", slaveThermostatThermostatOperatingStateHandler )

    synchronizeMasterToSlave()
}


// SUBSCRIBED EVENT HANDLERS:


// yes, it is slightly ridiculous to have a separate version of the function for
// state and event, because for our purposes a State and Event are being used in
// exactly the same way, but I need the "state" handler to initialize the
// virtual thermostat's temperature property in the initialize() method.


def masterThermometerTemperatureHandler(com.hubitat.hub.domain.Event event) {
    log.debug("masterThermometerTemperatureHandler (the event version) was called with event: ${event} of class ${event.class.name}: ${event.dump()} ${event.properties}");
    masterThermometerTemperatureHandler(event.value, event.unit)
    return;
}
def masterThermometerTemperatureHandler(com.hubitat.hub.domain.State state) {
    log.debug("masterThermometerTemperatureHandler (the state version) was called with state: ${state} of class ${state.class.name}: ${state.dump()} ${state.properties}");
    masterThermometerTemperatureHandler(state.value, state.unit)
    return;
}
def masterThermometerTemperatureHandler(String value, String unit){
    log.debug("masterThermometerTemperatureHandler (the value version) was called with value: ${value} and unit: ${unit}.")
    sendEvent(masterThermostat, 
        [
            name: "temperature",
            value: value,
            unit: unit
        ]
    )
}


def slaveThermostatThermostatOperatingStateHandler(com.hubitat.hub.domain.Event event) {
    log.debug("slaveThermostatThermostatOperatingStateHandler (the event version) was called with event: ${event} of class ${event.class.name}: ${event.dump()} ${event.properties}");
    slaveThermostatThermostatOperatingStateHandler(event.value)
    return;
}
def slaveThermostatThermostatOperatingStateHandler(com.hubitat.hub.domain.State state) {
    log.debug("slaveThermostatThermostatOperatingStateHandler (the state version) was called with state: ${state} of class ${state.class.name}: ${state.dump()} ${state.properties}");
    slaveThermostatThermostatOperatingStateHandler(state.value)
    return;
}
def slaveThermostatThermostatOperatingStateHandler(String value){
    log.debug("slaveThermostatThermostatOperatingStateHandler (the value version) was called with value: ${value}.")
    sendEvent(masterThermostat, 
        [
            name: "thermostatOperatingState",
            value: value
        ]
    )
}

////  the above handlers dealing with pushing the masterThermometer reading to the (virtual) master thermostat
// the remaining handlers are agnostic as to whether masterThermostat is a childs virtual device that we have created or is a real external thermostat.

def synchronizationPotentiallyNeededHandler(com.hubitat.hub.domain.Event event){
    log.debug("synchronizationPotentiallyNeededHandler() was called with event: ${event.name} = ${event.value} from ${event.device}.");
    synchronizeMasterToSlave();
    return;
}

def synchronizeMasterToSlave(){
    // TODO: deal with units, probably by converting all temperatures to SI internally.
    long _now = now()
    
    Number setpointEqualityTolerance = 0.01

    Number masterCoolingSetpoint    = masterThermostat .currentState( 'coolingSetpoint'    ).getNumberValue()
    Number masterHeatingSetpoint    = masterThermostat .currentState( 'heatingSetpoint'    ).getNumberValue()
    Number masterTemperature        = masterThermostat .currentState( 'temperature'        ).getNumberValue()
    String masterThermostatMode     = masterThermostat .currentState( 'thermostatMode'     ).getValue()
    String masterThermostatFanMode  = masterThermostat .currentState( 'thermostatFanMode'  ).getValue()


    Number slaveCoolingSetpoint     = slaveThermostat  .currentState( 'coolingSetpoint'    )?.getNumberValue()
    Number slaveHeatingSetpoint     = slaveThermostat  .currentState( 'heatingSetpoint'    )?.getNumberValue()
    Number slaveTemperature         = slaveThermostat  .currentState( 'temperature'        ).getNumberValue()
    String slaveThermostatMode      = slaveThermostat  .currentState( 'thermostatMode'     ).getValue()
    String slaveThermostatFanMode   = slaveThermostat  .currentState( 'thermostatFanMode'  ).getValue()

    
    Number masterControlErrorCooling = masterTemperature - masterCoolingSetpoint
    Number masterControlErrorHeating = masterTemperature - masterHeatingSetpoint

    Number desiredSlaveControlErrorCooling = masterControlErrorCooling
    Number desiredSlaveControlErrorHeating = masterControlErrorHeating

    // compute the states that we desire to have on the slave thermostat.
    // compute the desiredSlave setpoint that will cause the slaveControlError to be as desired
    // reminder:   ERROR = ACTUAL - DESIRED
    Number desiredSlaveCoolingSetpoint = slaveTemperature - desiredSlaveControlErrorCooling
    Number desiredSlaveHeatingSetpoint = slaveTemperature - desiredSlaveControlErrorHeating
    String desiredSlaveThermostatMode    = masterThermostatMode
    String desiredSlaveThermostatFanMode = masterThermostatFanMode

    Boolean setpointSettingCommandAllowedByRateLimiter = (
        ( state.timeOfLastSetpointSettingCommand == null ) 
        || 
        (_now - state.timeOfLastSetpointSettingCommand >= minimumAllowedDelayBetweenSetpointSettingCommands)
    )

    Boolean slaveSetpointsMatchMasterSetpoints = ( // setpoints are in sync
        ( ( slaveCoolingSetpoint == null ) ||  tolerantEquals(slaveCoolingSetpoint, desiredSlaveCoolingSetpoint, setpointEqualityTolerance) )
        &&
        ( ( slaveHeatingSetpoint == null ) ||  tolerantEquals(slaveHeatingSetpoint, desiredSlaveHeatingSetpoint, setpointEqualityTolerance) )
    )

    Boolean setpointSettingCommandDesired = (
        (desiredSlaveThermostatMode != "off")
        && !slaveSetpointsMatchMasterSetpoints
    )
    
    long delayPadding = 1000 //we will actually aim to delay by the user-specified delay time plus this amount, in order to ensure that when the delay expires, even with jitter, we are still likely to be past the specified delay interval.

    if (setpointSettingCommandDesired) {
        if (setpointSettingCommandAllowedByRateLimiter)  {
            log.debug("setting slave setpoints to $desiredSlaveCoolingSetpoint, $desiredSlaveHeatingSetpoint")
            slaveThermostat.setCoolingSetpoint(desiredSlaveCoolingSetpoint)
            slaveThermostat.setHeatingSetpoint(desiredSlaveHeatingSetpoint)
            state.timeOfLastSetpointSettingCommand = _now
        } else {
            //ensure that a future check is scheduled
            log.debug("rate limiting prevented setting slave setpoints to $desiredSlaveCoolingSetpoint, $desiredSlaveHeatingSetpoint")
            if (!state.setpointSettingRateLimitHoldoffExpirationHandlerIsScheduled){
                runInMillis(
                    minimumAllowedDelayBetweenSetpointSettingCommands + delayPadding,
                    setpointSettingRateLimitHoldoffExpirationHandler
                )
                state.setpointSettingRateLimitHoldoffExpirationHandlerIsScheduled = true
            }
        }
    }

    Boolean modeSettingCommandAllowedByRateLimiter = (
        ( state.timeOfLastModeSettingCommand == null ) 
        || 
        (_now - state.timeOfLastModeSettingCommand >= minimumAllowedDelayBetweenModeSettingCommands)
    )
    Boolean modeSettingCommandDesired = !(
        (desiredSlaveThermostatMode == slaveThermostatMode)
        &&
        (desiredSlaveThermostatFanMode == slaveThermostatFanMode)
    )
    
    if (modeSettingCommandDesired) {
        if (modeSettingCommandAllowedByRateLimiter)  {
            log.debug("setting slave modes to $desiredSlaveThermostatMode, $desiredSlaveThermostatFanMode")
            slaveThermostat.setThermostatMode(desiredSlaveThermostatMode)
            slaveThermostat.setThermostatFanMode(desiredSlaveThermostatFanMode)
            state.timeOfLastModeSettingCommand = _now
        } else {
            //ensure that a future check is scheduled
            log.debug("rate limiting prevented setting slave modes to $desiredSlaveThermostatMode, $desiredSlaveThermostatFanMode")
            if (!state.modeSettingRateLimitHoldoffExpirationHandlerIsScheduled){
                runInMillis(
                    minimumAllowedDelayBetweenModeSettingCommands + delayPadding,
                    modeSettingRateLimitHoldoffExpirationHandler
                )
                state.modeSettingRateLimitHoldoffExpirationHandlerIsScheduled = true
            }
        }
    }

    //drive the SLAVE thermostatMode and slave fan modes
    // Is there anything to be gained by only sending the command if the modes do not match?


    // possible TODO: handle or at least warn about a disobedient slave
    // thermostat (parituclarly if the slave thermostat's disobedience is
    // causing rapid cycling (might we want to only respond to events that have isStateChange == true?.
    // posible TODO: detect when masterThermometer is
    // offline/not providing fresh data, and then relinquish control, or turn
    // off the slave, or something reasonable. possible TODO: prevent the user
    // from trying to control the same slave thermostat with two instances of
    // this app, or somehow invent a way to stake a claim to the slave
    // thermostat so that other apps (and maybe even in some way the user) will
    // not be able to control it directly (because it is now strictly under the
    // contol of this app. (can we "adopt" the slave thermostat as a child
    // device and then hide it from direct control??) (intercept and thwart
    // commands sent to the slave thermostat from others???) general todo: log
    // and graph the time series data of setpoint, actual, and throttle (just to
    // verify that this control hijacking that I am doing with this app is
    // behaving as expected.).  
    // I am slightly worried that my slave thermostat (or, in general, any
    // thermostat that an eventual user might want to enslave) might be
    // resetting its accumulated integral error register whenever a new setpoint
    // is commanded. to do: (specific to the stelpro thermostat) tweak the
    // update rate/criteria settings to be sure we are getting all the
    // temporal/value resolution that the thermostat can deliver) (or at least
    // that we are getting a sufficiently good resolution for our
    // control-hijacking to perform well.

    return;
}


def modeSettingRateLimitHoldoffExpirationHandler(){
    log.debug("modeSettingRateLimitHoldoffExpirationHandler()")
    state.modeSettingRateLimitHoldoffExpirationHandlerIsScheduled = false
    synchronizeMasterToSlave()
    return;
}

def setpointSettingRateLimitHoldoffExpirationHandler(){
    log.debug("setpointSettingRateLimitHoldoffExpirationHandler()")
    state.setpointSettingRateLimitHoldoffExpirationHandlerIsScheduled = false
    synchronizeMasterToSlave()
    return;
}

Boolean tolerantEquals(Number a, Number b, Number tolerance){
    return java.lang.Math.abs(a-b)<=java.lang.Math.abs(tolerance);
}


def inputHandler(event) {
    log.debug "inputHandler was called with ${event.name} ${event.value} ${event}"

}

//////////////////////////////////////////////////

// The stelpro thermostat which is being used as slave thermostat while testing
// and writing this app has the (annoying) behavior of changing mode from off to
// heat when a set-setpoint command is sent.  This behavior occurs in the
// thermostat's firmware, not in the hubitat device driver.  I am not sure how
// common this behvaior is among commerically-available thermostats.
//
// How aggresively should we control the slave thermostat?  What should we do
// when and if the slavce thermostat spontaneously changes modes?  Should we
// force the slave thermostat to adhere to the mode of the master thermostat?  I
// think yes.
//
// When the master thermostat is in the Off mode, we should refrain from setting
// the setpoint of the slave thermostat (even though we might still be keeping
// track of what we would like that setpoint to be.) in order not to
// inadvertently cause the slave thermostat to turn on (as might happen if the
// slave thermostat has the same
// turn-on-automatically-with-setpoint-setting-command behavior as the Stelpro.)

// This app will aggressively control the slave thermostat.  If we notice that
// the slave thermostat's mode has changed to something other than the master
// thermostat, we will attempt to force the slave thermostat's mode to match the
// master mode.  In addition to reacting to spontaneous uncommanded changes of
// the slave thermostat's mode, we must also detect and respond to the case
// where the slave thermostat's mode fails to change when we want it to. (i.e.
// we have to not only respond to events from the slave thermostat telling us
// that the slave mode has changed, we also need to respond to an absence of
// events, probably with a periodic poll that we will schedule as long as slave
// mode is out of sync with master.

// The virtual thermostat that we create as a child device to serve as master
// thermostat should "inherit" (to mix metaphors) some of its
// behavior/attributes from the slave thermostat.  Specifically, it should
// inherit the set of allowable modes, and perhaps the setpoint range.
//

com.hubitat.app.DeviceWrapper getMasterThermostat(){
    // for the first iteration of this app, I am not going to support the
    // scenario where master thermostat is a pre-existing thermostat; for now, I
    // am going to assume that masterThermostat is our child device.

    return childDevices[0];
}

def copyErrorFromMasterToSlave() {
    // set the heating setpoint and cooling setpoint of the slave thermostat
    // so that the error matches that of the master thermostat.

    // it is a little bit irritating that the thermostat capability has the two
    // separate setpoints -- heating and cooling.  Of course, I understand that
    // the capbility is just trying to correspond to the actual behavior of
    // comnmerically-available thermostats, so this is really a complaint about
    // the user interface of commercially available thermostats.  There is
    // really but one setpoint (possibly with some deadzone around it, but the
    // size of the deadzone should be a separate parameter.) Thus, I have to
    // deal with two separate errors and setpoints -- for heating and cooling,
    // respectively.

    // masterCoolingError = masterThermostat.

    masterError = [


    ]
}




//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"
#include "utility.lib.groovy"