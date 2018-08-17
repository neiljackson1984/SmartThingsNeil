/**
 *  Virtual Thermostat
 *
 *  Copyright 2018 Neil Jackson
 *
 */
definition(
    name: "Virtual Thermostat Manager",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: "This SmartApp creates and manages a single Virtual Thermostat child device.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "mainPage");
    page(name: "removeWithChildDevicesPage1", install: false); 
    page(name: "removeWithChildDevicesPage2", install: false);
}

def mainPage() {
	def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);
    dynamicPage(
    	name: "mainPage", 
        title: "", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?") : yes
        install: true, 
        
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: false //getChildDevices( /*includeVirtualDevices: */ true ).isEmpty() 
        /*|++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
         *|   we will, in some cases, override the "uninstall: false" by invoking remove() below to 
         *|   display the remove button. if there are no child devices, we will 
         *|   display the remove button. if there are child devices, we will not 
         *|   display the remove button; rather we will display an href to another 
         *|   preferences page whose dynamic function will run DeleteChildDevice(..., 
         *|   true) . (The second argument to DeleteChildDevice is not documented, but 
         *|   it appears to be some sort of directive to forcefully delete the device 
         *|   even if the device is used by other smartapps.) All this special 
         *|   treatment of child devices is necessary during the app removal process 
         *|   because, the automatic removal of child devices that SmartThings 
         *|   performs during the standard SmartApp removal procedure will fail (and 
         *|   hence the removal of the SmartApp will fail) if the child devices happen 
         *|   to be in use by other SmartApps. Perhaps we should try to make the 
         *|   special preferences page sequence for child device deletion behave like 
         *|   the default Removal button, in that it will give the user an opportunity 
         *|   to back out (an "are you sure?" message). Also, perhaps it would be a 
         *|   good idea to first attempt the child device removal without the second 
         *|   argument to the deleteChildDevice function, and then if that fails 
         *|   (which we will detect in a try...catch structure) (there seems to be no 
         *|   reliable way to predict whether the DeleteChildDevice() method without the second argument will fail 
         *|  , so we can't warn the user ahead of time in 
         *|   a single warning message that would serve as both an "Are you sure you 
         *|   want to remove this smartapp?" warning and the "some child devices are 
         *|   in use by other smartapps, so are you really sure?" warning message, which would be ideal. 
         */
    ) {
    	section(/*"label"*/) {
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of the Virtual Thermostat SmartApp", 
                required: false, 
                defaultValue: "virtual-thermostat-manager--" + myDateFormat.format(myDate)
            );
            if(!getChildThermostat() )
            {
                input( 
                	name: "labelForNewChildThermostat",
                    title: "label for new child thermostat:", 
                    type: "text",
                    description: "assign a label for the new child thermostat that is to be created.", 
                    required: false, 
                    defaultValue: "virtual-thermostat--" + myDateFormat.format(myDate)
                );
            }
        }
    	section(/*"afferents"*/) {
            input(
                name: "thermometer", 
                title: "temperature sensor for the virtual thermostat" ,
                type: "capability.temperatureMeasurement", 
                description: "select a temperature sensor",            
                required:false,
                submitOnChange:false 
            )
        }

        section(/*"efferents"*/) {
            input(
            	title: "heater output:",
                name:"heaters", 
                type:"capability.Switch", 
                description: "select zero or more heaters",
                multiple:true,
                required:false
            )
            
            input(
            	title: "cooler output:",
                name:"coolers", 
                type:"capability.Switch", 
                description: "select zero or more coolers",
                multiple:true,
                required:false
            )
        }
       section("") {
        	mode( title: "Set for specific mode(s)", required: false);
        }
        
        if(getAllChildDevices().isEmpty()) //if there are no child devices... 
        {
        	remove("Remove this SmartApp");  //show the remove button)
        } else
        {
        	section() {
                href(
                    name: "hrefToDeviceDeletionPage",
                    title: "REMOVE",
                    description: "Remove this SmartApp",
                    required: false,
                    page: "removeWithChildDevicesPage1"
                );
            }
        }
    }
}


def removeWithChildDevicesPage1()
{
    dynamicPage(
    	name: "removeWithChildDevicesPage1", 
         //title: "this is the title of removeWithChildDevicesPage1", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?")
        install: false, 
        
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: false,
        //we might override the "uninstall:false" by invoking remove() below to display the remove button.
        
        //nextPage: "mainPage"
    ) {
    	section(){
    		paragraph (
            	"In order to remove this SmartApp, we must first remove all of this SmartApp's child devices, namely: \n" + 
                "${getAllChildDevices().inject("") {result, i -> result + (i.toString() + "\n")} ?: ""}"
            );
            paragraph "Do you want to remove all the child devices?"
            href(
                title: "Yes, delete all child devices",
                description: "",
                required: false,
                page: "removeWithChildDevicesPage2"
            );
            // href(
            //     title: "No, go back to the main page",
            //     description: "",
            //     required: false,
            //     page: "mainPage"
            // );
        }
    }
}


def removeWithChildDevicesPage2()
{
    // unsubscribe()
    String report = "";
    
    
    try {
    	getAllChildDevices().each {
        	unsubscribe(it);
            deleteChildDevice(it.deviceNetworkId);
    	};
        report += "Succesfully deleted all child devices without having to resort to force delete.\n"
        
    } catch(e) {
    	report += "Encountered the error \"${e}\" while attempting to gently delete the child devices, so we are proceeding with forceful deletion\n"
        try{
        	getAllChildDevices().each {
            	unsubscribe(it);
                deleteChildDevice(it.deviceNetworkId, true)
            };
            report += "Succesfully deleted all child devices by resorting to forceful deletion.\n"
        } catch (ee)
        {
        	report += "Forceful deletion of all child devices failed, with error message ${ee}.\n"
        }
        
    }
    dynamicPage(
    	name: "removeWithChildDevicesPage2", 
        //title: "this is the title of removeWithChildDevicesPage2", 
        // "allow the SmartApp to be installed from this page?" (i.e. "show the OK/Install button?")
        install: false,         
        // "allow the SmartApp to be uninstalled from this page?" (i.e. "show the "Remove" button?") 
        uninstall: false,
        // we will probably be overridding the "uninstall:false" by invoking remove() below.
        //nextPage: "mainPage"
    ) {
    	section(){
    		paragraph report
        }
        remove("Remove")
    }
}


String getUniqueIdRelatedToThisInstalledSmartApp()
{
    //   // java.util.regex.Pattern x = new  java.util.regex.Pattern();
    //   // java.util.regex.Pattern myPattern = java.util.regex.Pattern.compile("(?<=_)([0123456789abcdef]+)(?=@)");
    //   // def myMatcher= myPattern.matcher((String) this);
    //   def myMatcher= ((String) this) =~ "(?<=_)([0123456789abcdef]+)(?=@)";
    //   //myMatcher.find();
    //   //return myMatcher.group();
    //   return myMatcher[0][1];
    
    return app.getId();  
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    
    // we do not want to call initialize here, because app.getLabel() evaluates to the default lable, which is the same as the name of the Smart App.
    // immediately after installing and calling this installed() function, the platform will then call the updated() function.
    // we want to call initialize from the updated() function, because by that time, app.getLabel() evaluates to the real label of this
    // installedSmartApp.
	//initialize()
}

def uninstalled() {
	log.trace "uninstalling"
    //getAllChildDevices().each {
    //    //deleteChildDevice(it.deviceNetworkId, true)
    //    deleteChildDevice(it.deviceNetworkId)
    //}
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "app.getLabel(): ${app.getLabel()}"
    log.debug ("label: " + label)
    log.debug ("labelForNewChildThermostat: " + labelForNewChildThermostat)
	def deviceNetworkId="virtualThermostat" + "-" + getUniqueIdRelatedToThisInstalledSmartApp();
	log.debug("deviceNetworkId: " + deviceNetworkId);
	
    if( !getChildThermostat())
    {
        addChildDevice(
            /*namespace: */           "neiljackson1984",
            /*typeName: */            "Virtual Thermostat",     // How is the SmartThings platform going to decide which device handler to use in the case that I have a custom device handler with the same namespace and name?  Is there any way to specify the device handler's guid here to force the system to use a particular device handler.
            /*deviceNetworkId: */     deviceNetworkId  , //how can we be sure that our deviceNetworkId is unique?  //should I be generating a guid or similar here.
            /*hubId: */               settings.theHub?.id,
            /*properties: */          [completedSetup: true, label: labelForNewChildThermostat]
        );
        log.debug("just created a child device: " + getChildThermostat());
    } 
    
    getChildThermostat().updateController();
    
    log.debug("parent of the child: " + getChildThermostat().getParent());
    
    //we subscrbe to something from the child thermostat for the sole reason of causing this smartapp to appear in the smartapps section of the child thermostat settings page.
    subscribe(
        getChildThermostat(),
        "temperature",
        doNothing
    );
    
    if(thermometer)
    {
        subscribe(
            thermometer,
            "temperature",
            inputHandler
        );
      
        
        if(thermometer.hasCapability("Health Check"))
        {

            //the "Health Check" capability has
            //  Attributes: checkInterval, DeviceWatch-DeviceStatus, healthStatus
            //  Commands: ping
            subscribe(
                new physicalgraph.app.DeviceWrapper(thermometer),
                "healthStatus",
                inputHandler
            )
        }
    }
    
    //log.debug("thermometer.dump(): " + thermometer.dump());
    //log.debug("thermometer.inspect(): " + thermometer.inspect());
    //log.debug("thermometer.class: " + thermometer.class    );
    //
    // TO DO: implement a safety feature whereby we respond to the case of losing communication with the thermometer by, for instance, turning off all heaters and coolers. 
    
}

def doNothing(){}

def getChildThermostat() {
	def childThermostat;
	
    if(!getAllChildDevices().isEmpty())
    {
        childThermostat = childDevices.get(0);
    }
    
    return childThermostat;
}

def inputHandler(event) {
	log.debug "inputHandler was called with ${event.name} ${event.value} ${event}"
    log.debug event.getDevice().toString() + ".currentValue(\"temperature\"): " + event.getDevice().currentValue("temperature") 
    log.debug event.getDevice().toString() + ".currentValue(\"healthStatus\"): " + event.getDevice().currentValue("healthStatus") 
    getChildThermostat().setTemperature(event.getDevice().currentState("temperature"));
    //  writeLevel(
    //  	(int) (
    //      	event.getDevice().currentValue("level") 
    //          * (event.getDevice().hasCapability("Switch") && (event.getDevice().currentValue("switch") == "off") ? 0 : 1)
    //      )
    //  ); 

    
}