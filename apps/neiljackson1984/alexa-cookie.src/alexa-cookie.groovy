/**
    This app is intended to emulate the functionality of gabriele-v's 
    AlexaCookieNodeJs script (https://github.com/gabriele-v/hubitat/tree/master/AlexaCookieNodeJs/AlexaCookieNodeJs).
    
    This app will present an http-based interface to the outside world, that will behave in
    a functionally equivalent way to gabriele-v's AlexaCookieNodeJs script.
    
    An instance of this app can replace an instance of gabriele-v's AlexaCookieNodeJs script
    running on a raspberry pi, in order to eleiminate the need for the raspberry pi for
    autoamted Alexa cookie refreshing.
    
    If this works, then conceivably the code herein could be merged with ogiewon's alexatts app
    so that automated cooikie maintenance could be performed entirely by the alexatts app, without
    requring an external device, or even an external app.
 
    -Neil Jackson
 */
definition(
    name: "alexa-cookie",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: "a port of gabriele-v's alexa-cookie nodejs script for Hubitat Groovy.",
    importUrl: "https://raw.githubusercontent.com/neiljackson1984/SmartThingsNeil/master/apps/neiljackson1984/alexa-cookie.src/alexa-cookie.groovy",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

mappings {
     path("/runTheTestCode") { action: [GET:"runTheTestCode"] }
 }


preferences {
    page(name: "mainPage");
}




def runTheTestCode(){
    try{
        return mainTestCode();
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
	// log.debug(message);
	// sendEvent( name: 'testEndpointResponse', value: message )
	// return message;
	return  render( contentType: "text/html", data: message, status: 200);
}


def mainTestCode(){
    startCollectionOfDebugMessage();

	// def debugMessage = ""

	debugMessage += "\n\n";
   
    // debugMessage += "this: " + this.dump() + "\n";
    // debugMessage += "this.class: " + this.class + "\n";

    // debugMessage += "\n\n";
    
    // debugMessage += "this.class.getDeclaredFields(): " + "\n";
    // this.class.getDeclaredFields().each{message += it.toString() + "\n";	}
    
    // debugMessage += "\n\n";
    // debugMessage += "this.class.getMethods(): " + "\n";
    // this.class.getMethods().each{	debugMessage += it.toString() + "\n";}

    // debugMessage += "\n\n";

    // def testCookieString = (
    //     "x-amzn-dat-gui-client-v=1.24.206%2540711.0;            session-id=141-7015063-0680035; session-id-time=2082787201l; ubid-main=133-5535871-8067322; lc-main=en_US; " 
    //     + "x-wl-uid=1V2/WWvmX439HF+XdCkhL9PHawSb5RBbYlQ8lB8Dt2tQWX5lDMtafDiieJtaPA0d03NLcY/5jAFALWHTZMyiCBcSNiDqP0I7OKeZ3ZkfzWWCPjgRCB309I6HSH7+dj52A0Ir2wvU" 
    //     + "qCdk=; s_vnum=1968898322054%26vn%3D1; csrf=-138084346; sst-main=Sst1|PQFOqw7K6XmItpEueZO6sVl4C_mRc2RePxIq-_CHSuTc8I369GLCi-LC9k_tQx3kYEI8zh2revC7" 
    //     + "-J6Dn-Im2YNkthxY-mNwtilRMsw6QzmTe2YkEGIzJ_oeVsEfwNvy6P4QPVbiMNcsMTNX49g2TJsGjtjnI-KZyd4iCr5rkELkGDGqeE2kM987mXo5fQol8IfsnrBFY1k-91U8siWmbzxPXKRcX" 
    //     + "p2rIqQFRGWVJPPYr0ImtY86mGlelDAfiBSMJGQpZsm-nzgIrtqUsOpfg_lyLxJn-TGoCOIB5TKwrbqqOjzLfkzBazwy5EsawkRhyLp4RBJ6rWlRzcCoK-RFl59HvQ; aws-priv=eyJ2IjoxLC" 
    //     + "JldSI6MCwic3QiOjB9; aws-target-static-id=1551327718663-960278; aws-target-data=%7B%22support%22%3A%221%22%7D; s_fid=332C7FF525BA8B6A-3AF67B250F0842" 
    //     + "CE; aws-ubid-main=562-6103331-6308672; aws-business-metrics-last-visit=1551328092970; regStatus=registered; i18n-prefs=USD; aws-userInfo=%7B%22arn%2"
    //     + "2%3A%22arn%3Aaws%3Aiam%3A%3A735865980878%3Aroot%22%2C%22alias%22%3A%22%22%2C%22username%22%3A%22neil%2540rattnow.com%22%2C%22keybase%22%3A%229NjBbLTpc"
    //     + "K%2FWQvADd%2FsYZ71sUfEFXh2i7xEop7LMtcg%5Cu003d%22%2C%22issuer%22%3A%22http%3A%2F%2Fsignin.aws.amazon.com%2Fsignin%22%7D; aws-target-visitor-id=15513"
    //     + "27718674-48428.28_79; aws-session-id=601-5345548-3841895; aws-session-id-time=1555913203l; _mkto_trk=id:112-TZM-766&token:_mch-aws.amazon.com-155132"
    //     + "7719865-50118; s_vn=1582863719597%26vn%3D4; s_dslv=1555915585187; x-main=jrRhPl7HyG7zR80MIT1nWYHjiYIKqpVp; at-main=Atza|IwEBIKDyzu0depUUhPH6Zr0sxOuZ"
    //     + "e7xQ213FeKYMZkyQNZtCVBbJKbmoZIuOdlLbRcCJS-ExIMGFaXEGQek4WVrC2aPuVAsG98_DAZeGzDQxhIhhT5oUlE1132sSZYXLJKaC9Joarjle8daUxIEo0IwjYHLZtvZM-n_nS0n-iGc6mLF"
    //     +" 8LesXc2iySSf7c78f3o-67pZhjiJJtZDq6ftZcjL-lDY86U0tOndV6jr8N2X72wq3A2RrlqJa_hwQMXsF0uAgMExG2S_-FfMKJgnNO6L3jJ_nahI5WcG0EgkSjZJbQKR7peVp3z072aFPAZOPd" 
    //     + "0Eu0pVXPvACvyc1BtlEjGcexruv3D76EhZT2dvpQPa92iW5P62EX7rmpGTBrsByw0ERdJGeb61b9QWNbBFy0sTzxWyi; sess-at-main=\"Gn10SqZw92pMVXL1AOtqyFZVRr+CwKmetH/hb/15a"
    //     + "As=\"; session-token=\"761ffpp1sTgqTMrEE4hRL70s+W83MDyUnvCK1WoQqVp3lGclCOIFk5ey+xfOUN0KUGjUEdcnq2B5uqo+h5GY8q3iV+WpfR+DBH/mOC1JGcO6Yhw62L4DO1fnCKVZvG" 
    //     + "mtAbKMDxrCA4evTmFZWL28pQGFNZevkam9JgyOeDEX7CZhQarUO9iIwstipNBavNYQF020lkRDo3vG9/AevsIbkw==\";"
    // );

    // message += (
    //     "cookie_parse(testCookieString): " + "\n" 
    //     + cookie_parse(testCookieString).collect{key, value -> 
    //         "\t"*1 + key + ": " + "---" + value + "---"
    //     }.join("\n") + "\n"
    // );


    // def testEncodedString = "Mtcg%5Cu003d%22%2C%22iss";
    // message += "URLDecoder.decode(testEncodedString): " + URLDecoder.decode("hello%20there") + "\n";
    // message += "AlexaCookie(): " + AlexaCookie().dump() + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", 25) + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", zigbee) + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", ['set-cookie':1]) + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", 'set-cookie') + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", 'set-cookie asdfsdf') + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", '${["set-cookie":77]}') + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies("balsadfasdfasdf", 'asdfsadfset-cookieasdfsdf') + "\n";
    // message += "AlexaCookie().addCookies(11,...): " + AlexaCookie().addCookies(11, 'asdfsadfset-cookieasdfsdf') + "\n";
    
    httpGet(
        [
            'uri': "https://postman-echo.com/cookies/set?foo1=bar1&foo2=bar2",
            'query': [
                'foo1':'bar1',
                'foo2':'bar2'
            ]
        ],

        {response ->
            appendDebugMessage("response.headers: " + response.headers.dump() + "\n");
            // appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.dump()}.join("\n") + "\n");
            appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n");
            appendDebugMessage("response.getAllHeaders(): " + "\n" + response.getAllHeaders().collect{
                    "\t"*1 + it.getName() + ": " + "\n" +
                    "\t"*2 + it.getValue() + "\n" + 
                    "\t"*2 + "elements" + "\n" + 
                        it.getElements().collect{"\t"*3 +  it.name + ": " + it.value }.join("\n")
                }.join("\n") + "\n");
           //debugMessage += "\n"*2;
            appendDebugMessage("AlexaCookie().addCookies('', response.headers): " + AlexaCookie().addCookies('', response.headers) + "\n");
            appendDebugMessage("response.data: " + response.data.toString() + "\n");
        }
    );
    stopCollectionOfDebugMessage();
   return respondFromTestCode(debugMessage);
}

def startCollectionOfDebugMessage(){
    state['enableCollectionOfDebugMessage'] = 1;
    state['debugMessage'] = "";
}

def stopCollectionOfDebugMessage(){
    state['enableCollectionOfDebugMessage'] = 0;
}


def getDebugMessage(){
    return state['debugMessage'];
}

def setDebugMessage(x){
    if (state['enableCollectionOfDebugMessage']){
        state.debugMessage = x;
    }
}

def appendDebugMessage(x)
{
    debugMessage += x;
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
        uninstall: true //getChildDevices( /*includeVirtualDevices: */ true ).isEmpty() 

    ) {
    	section(/*"label"*/) {
            //label( title: "Assign a name", required: false, defaultValue: (new Date()).getTime());
            label( 
            	title: "label:", 
                description: "Assign a label for this instance of the alexa-cookie SmartApp", 
                required: false, 
                defaultValue: "alexa-cookie--" + myDateFormat.format(myDate)
            );
        }
    	section() {
            input(
                name: "amazon_username", 
                title: "Amazon username" ,
                type: "text", 
                description: "",            
                required:false,
                submitOnChange:false 
            )
            input(
                name: "amazon_password", 
                title: "Amazon password" ,
                type: "text", 
                description: "",            
                required:false,
                submitOnChange:false 
            )  
        }
    }
}



def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def uninstalled() {
}

def updated() {
	log.debug "Updated with settings: ${settings}"
  	//log.debug "myLabel: ${myLabel}"
	unsubscribe()
	initialize()
}

def initialize() {
	
}


/**
* returns a fully-formed AlexaCookie object analagous to the
AlexaCookie object created by the code in alexa-cookie.js
*/
def AlexaCookie() {

    def _ = [:]; //there's nothing special about the identifier "_", we are just using it because it's short and doesn't impair the readability of the code too much.  We are using it as the identifier for the object that we are construction and will return.

    def proxyServer;
    def _options = [:];
    // def Cookie='';

    /**
     *  applies any cookies that may be present in 
     *  a set of http headers (an iterable of org.apache.http.Header)  to an existing Cookie string (adding any cookies that
     *  that do not already exist, and updating any that do.)
     *  Returns the updated version of the Cookie string.
     */
    _.addCookies = {String Cookie, headers ->
        // if (!headers || !('set-cookie' in headers)){
        if (!headers || !headers.any{it.name =="set-cookie"} ){
            appendDebugMessage("could not find a 'set-cookie' header in headers." + "\n");
            return Cookie; 
        }   

        // original javascript:   
        //      const cookies = cookieTools.parse(Cookie);
        def cookies = cookie_parse(Cookie); 

        // original javascript:   
        //    for (let cookie of headers['set-cookie']) {
        // according to https://nodejs.org/api/http.html#http_message_headers ,
        // headers['set-cookie'] will always be an array. (This especially makes sense when the response contains multiple 'set-cookie' 
        // headers (which I guess is allowed (i.e. it seems that the collection of headers is not strictly an associative map, because 
        // you can have multiple entries having the same 'key'.  I guess the collection of headers is more like a list 
        // of (name, value) pairs.)

        for (def headerValue in headers.findAll{it.name == "set-cookie"}.collect{it.value}){
            // original javascript: cookie = cookie.match(/^([^=]+)=([^;]+);.*/);
            // we expect headerValue to be a string that looks like "foo=blabbedy blabbedy blabbedy ;"
            appendDebugMessage("headerValue: " + headerValue + "\n");
            cookieMatch = (~/^([^=]+)=([^;]+);.*/).matcher(headerValue)[0];

            //original javascript:  if (cookie && cookie.length === 3) {
            if (cookieMatch && cookieMatch.size() == 3) {
                appendDebugMessage("cookieMatch: " + cookieMatch[1] + "--" + cookieMatch[2] + "--" + cookieMatch[3] + "\n");
                // original javascript:  if (cookie[1] === 'ap-fid' && cookie[2] === '""') continue;
                if (cookieMatch[1] == 'ap-fid' && cookieMatch[2] == '""'){ continue;}
                
                //original javascript: if (cookies[cookie[1]] && cookies[cookie[1]] !== cookie[2]) {
                if( (cookieMatch[1] in cookies) && (cookies[cookieMatch[1]] != cookieMatch[2]) ){
                    //original javascript: _options.logger && _options.logger('Alexa-Cookie: Update Cookie ' + cookie[1] + ' = ' + cookie[2]);
                    _options['logger'] && _options['logger']('Alexa-Cookie: Update Cookie ' + cookieMatch[1] + ' = ' + cookieMatch[2]);
                } else if (!(cookieMatch[1] in cookies) ) {
                    _options.logger && _options.logger('Alexa-Cookie: Add Cookie ' + cookieMatch[1] + ' = ' + cookieMatch[2]);
                } else {
                    //in this case, (cookieMatch[1] in cookies) && (cookies[cookieMatch[1]] == cookieMatch[2])
                    //in other words, a cookie of the same name and value already exists in cookies.
                } 

                //original javascript: cookies[cookie[1]] = cookie[2];
                cookies[cookieMatch[1]] = cookieMatch[2];
            }
        }

        //rebuild the cookie string from the newly-updated cookies map.

        //>    Cookie = '';
        //>    for (let name in cookies) {
        //>        if (!cookies.hasOwnProperty(name)) continue;
        //>        Cookie += name + '=' + cookies[name] + '; ';
        //>    }
        //>    Cookie = Cookie.replace(/[; ]*$/, '');
        Cookie = '';
        for (name in cookies.keySet()){
            Cookie += name + '=' + cookies[name] + '; ';
        }

        return Cookie;  //>    return Cookie;
    };



    _.addCookies.delegate = _AlexaCookie;

    return _;
}



/**
 * Parse a cookie header.
 *
 * Parse the given cookie header string into an object
 * The object has the various cookies as keys(names) => values
 *
 * @param {string} str
 * @param {object} [options]
 * @return {object}
 * @public
 */
def cookie_parse(str, options=[:]) {
    if (! str instanceof String ) {
        throw new Exception("argument str must be a string");
    }

    //these were originally global variables:
    def pairSplitRegExp = "; *";
    // def decode = decodeURIComponent;
    // def encode = encodeURIComponent;

    def obj = [:];
    def opt = options + [:];
    def pairs = str.split(pairSplitRegExp);

    for (def i = 0; i < pairs.size(); i++) {
        def pair = pairs[i];
        def eq_idx = pair.indexOf('=');

        // skip things that don't look like key=value
        if (eq_idx < 0) {
            continue;
        }

        def key = pair.substring(0, eq_idx).trim();
        def val = pair.substring(++eq_idx, pair.length()).trim();

        // quoted values
        if ('"' == val[0]) {
            val = val[1..-2]; //remove the first and last characters from val.
        }

        // only assign once
        if (! obj.containsKey(key)) {
            obj[key] = URLDecoder.decode(val);
        }
    }

    return obj;
}