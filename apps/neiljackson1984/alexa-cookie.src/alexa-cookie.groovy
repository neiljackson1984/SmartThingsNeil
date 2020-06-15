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
 // */
definition(
    name: "alexa-cookie",
    namespace: "neiljackson1984",
    author: "Neil Jackson",
    description: (
        "a port of gabriele-v's alexa-cookie nodejs script for Hubitat Groovy.  This is not meant to be a useful app on its own.  Rather, it "
        + "is a test harness for the alexaCookieUtility object defined in alexa_cookie_utility.groovy"
    ),
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


def prettyPrint(x){
    return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(x));
}


def getDebugMessage(){
    return state.debugMessage;
}

def setDebugMessage(x){
    // log.debug('setting debug message to ' + x);
    if (state.enableCollectionOfDebugMessage){
        state.debugMessage = x;
    }
    return x;
}

def startCollectionOfDebugMessage(){
    state.enableCollectionOfDebugMessage = 1;
    // debugMessage = ""; //the automatic mapping of the assignment operator to a coresponding setter does not seem to be working in hubitat as in smartthings.
    setDebugMessage("");
}

def stopCollectionOfDebugMessage(){
    state['enableCollectionOfDebugMessage'] = 0;
}

def appendDebugMessage(x){
    // log.debug("appending to debugMessage: " + "\n" + "<code>" + groovy.xml.XmlUtil.escapeXml(org.apache.commons.lang3.StringUtils.abbreviate(x, 300))  + "</code>");
    Closure abbreviateString = {y ->
        final Integer maximumAllowedUnabbreviatedLength = 60;
        String idOfTruncatedSpan = UUID.randomUUID();
        String idOfUnabridgedSpan = UUID.randomUUID();
        Boolean weNeedToAbbreviate = y.length() > maximumAllowedUnabbreviatedLength;
        return (
                "<span id='${weNeedToAbbreviate ? idOfTruncatedSpan : idOfUnabridgedSpan}' style='display:initial;'>" + "<code>" + groovy.xml.XmlUtil.escapeXml(y.take(maximumAllowedUnabbreviatedLength).replace('\n','')) + "</code>" + "</span>"
                + ( 
                    weNeedToAbbreviate 
                    ?
                    "<button onClick=\"document.getElementById('${idOfUnabridgedSpan}').style.display = (document.getElementById('${idOfUnabridgedSpan}').style.display === 'none' ? 'initial' : 'none');\">&hellip;</button>"
                    + "<span id='${idOfUnabridgedSpan}' style='display:none;'>" + "<code>" + "\n" + groovy.xml.XmlUtil.escapeXml(y) + "</code>" + "</span>"
                :
                ""
            )
        );
    }
    
    // log.debug("appending to debugMessage: " + abbreviateString(x));
    log.debug(abbreviateString(x));
    setDebugMessage(getDebugMessage() + x);
}

def runTheTestCode(){
    startCollectionOfDebugMessage();
    try{
        mainTestCode();
    } catch (e)
    {
        String internalDebugMessage = '';
        
        // def debugMessage = ""
        internalDebugMessage += (
            "\n\n" + "================================================" + "\n" +
            (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n" +
            "encountered an exception: \n${e}\n"
        );
        
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

            for(item in stackTrace){stackTraceItems << item; }


            def filteredStackTrace = stackTraceItems.findAll{ it['fileName']?.startsWith("user_") };
			
			//the last element in filteredStackTrace will always be a reference to the line within the runTheTestCode() function body, which
			// isn't too interesting, so we get rid of the last element.
			if(!filteredStackTrace.isEmpty()){
				filteredStackTrace = filteredStackTrace.init();  //The init() method returns all but the last element. (but throws an exception when the iterable is empty.)
			}
            
            // filteredStackTrace.each{appendDebugMessage(it['fileName'] + " @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n");   }
            filteredStackTrace.each{internalDebugMessage += (" @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n");   }
                 
        } catch(ee){ 
            internalDebugMessage += ("encountered an exception while trying to investigate the stack trace: \n${ee}\n");
            // appendDebugMessage("ee.getProperties(): " + ee.getProperties() + "\n");
            // appendDebugMessage("ee.getProperties()['stackTrace']: " + ee.getProperties()['stackTrace'] + "\n");
            internalDebugMessage += ("ee.getStackTrace(): " + ee.getStackTrace() + "\n");
            
            
            // // java.lang.Throwable x;
            // // x = (java.lang.Throwable) ee;
            
            // //appendDebugMessage("x: \n${prettyPrint(x.getProperties())}\n");
            // appendDebugMessage("ee: \n" + ee.getProperties() + "\n");
            // // appendDebugMessage("ee: \n" + prettyPrint(["a","b","c"]) + "\n");
            // //appendDebugMessage("ee: \n${prettyPrint(ee.getProperties())}\n");
        }
        
        // internalDebugMessage += "filtered stack trace: \n" + 
            // groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(filteredStackTrace)) + "\n";
    
        internalDebugMessage += "\n";
        appendDebugMessage(internalDebugMessage);
    }
    stopCollectionOfDebugMessage();
    return respondFromTestCode(debugMessage);
}

def respondFromTestCode(message){
	// log.debug(message);
	// sendEvent( name: 'testEndpointResponse', value: message )
	// return message;


    // log.debug(renderedMessage);
    // new MarkupBuilder().root {
    //     mkp.yield('blabbedy')
    // }

    log.debug("debugMessage: " + "\n" + "<code>" + groovy.xml.XmlUtil.escapeXml(message) + "</code>");
	return  render(contentType: "text/html", data: message, status: 200);
}

Integer setFoo(Integer x){
    state.foo = x;
    appendDebugMessage("setFoo was called, with argument: " + x + "\n");
    return x;
}

def getFoo(){
    def returnValue = state.foo;
    appendDebugMessage("getFoo is returning " + returnValue + "." + "\n");
    return returnValue;
}

private void mainTestCode(){
    
    setFoo(1);
    this.setFoo(2);


    appendDebugMessage("\n");
    appendDebugMessage("getFoo(): " + getFoo() + "\n");
    appendDebugMessage("this.getFoo(): " + this.getFoo() + "\n");
    appendDebugMessage("foo: " + foo + "\n");
    appendDebugMessage("this.foo: " + this.foo + "\n");
    appendDebugMessage("\n");

    this.foo = 3;
    appendDebugMessage("\n");
    appendDebugMessage("getFoo(): " + getFoo() + "\n");
    appendDebugMessage("this.getFoo(): " + this.getFoo() + "\n");
    appendDebugMessage("foo: " + foo + "\n");
    appendDebugMessage("this.foo: " + this.foo + "\n");
    appendDebugMessage("\n");

    foo = 4;
    appendDebugMessage("\n");
    appendDebugMessage("getFoo(): " + getFoo() + "\n");
    appendDebugMessage("this.getFoo(): " + this.getFoo() + "\n");
    appendDebugMessage("foo: " + foo + "\n");
    appendDebugMessage("this.foo: " + this.foo + "\n");
    appendDebugMessage("\n");
}

private void mainTestCode6(){
    // appendDebugMessage( "settings.manuallyEnteredAlexaRefreshOptions.class: " + settings.manuallyEnteredAlexaRefreshOptions.class);
    // sendEvent(name:"foo", value: now());
    // return;
    
    alexaCookie.refreshAlexaCookie(
        options: [
            logger: {appendDebugMessage(it + "\n");},
            formerRegistrationData: (new groovy.json.JsonSlurper()).parseText(settings.manuallyEnteredAlexaRefreshOptions)
        ],
        callback: {String error, Map result ->
            if(error){
                appendDebugMessage("error string that resulted from attempting to refresh the alexa cookie: " + error + "\n");
                
            } else if(!result){
                appendDebugMessage("alexaCookie.refreshAlexaCookie did not return an explicit error, but returned a null result." + "\n");
            } else {
                appendDebugMessage("alexaCookie.refreshAlexaCookie returned the following succesfull result: " + "\n" + prettyPrint(result) + "\n");
                handleNewAlexaRefreshOptions(new groovy.json.JsonBuilder(result).toString());
				// sendEvent(name:"GetCookie", descriptionText: "New cookie downloaded succesfully");
            }
        }
    );
}

/**
*  This function takes care of processing newly-fetched alexa refresh options.
*  We need to set state.alexaRefreshOptions, and we need to update state.alexaCookie with the cookie
*  that is contained in the newAlexaRefreshOptions.
*  Also, optionally, for debugging, I want to record the existing value of state.alexaRefreshOptions along witha timestamp before writing the new value.
*  the whole business with updating state.alexaCookie essentially amounts to nothing more than memoization of the output of the getCookieFromOptions() function.
*  Perhaps it would be cleaner to do something that is more obviously memoization.
*/
def handleNewAlexaRefreshOptions(String newAlexaRefreshOptions){
    //sending an event is only for debugging:
    // record the existing alexaRefreshOptions //only for debugging
    sendEvent(
        name: "alexaRefreshOptions", 
        value: newAlexaRefreshOptions, 
        data: [alexaRefreshOptions: new groovy.json.JsonSlurper().parseText(newAlexaRefreshOptions)]
    ); // for debugging only
    state.alexaRefreshOptions = newAlexaRefreshOptions;
    state.alexaCookie = getCookieFromOptions(state.alexaRefreshOptions);
}

def mainTestCode5(){

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

    // appendDebugMessage(
    //     "alexaCookie.cookie_parse(testCookieString): " + "\n" 
    //     + alexaCookie.cookie_parse(testCookieString).collect{key, value -> 
    //         "\t"*1 + key + ": " + "---" + value + "---"
    //     }.join("\n") + "\n"
    // );


    alexaCookie.generateAlexaCookie(
        'email': settings.amazon_username,
        'password': settings.amazon_password,
        'options':  [
            'logger': {appendDebugMessage(it + "\n");},
            'amazonPage': "amazon.com",
            'acceptLanguage':'en-US',
            'userAgent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36'
        ],
        'callback': {String error, Map result ->
            if(error){
                appendDebugMessage("error string that resulted from attempting to generate alexa cookie: " + error + "\n");
            }
            if(result){
                appendDebugMessage("result of attempting to generate alexa cookie: " + prettyPrint(result) + "\n");
            }
        }
    );



   

}

//{ DEPRECATED test code

def mainTestCode4(){

    httpGet(
        [
            'uri': "https://postman-echo.com/cookies",
            'headers':[
                'Cookie': "foo1=bar1; foo2=bar2;"
            ],
            'query': [
                'a':'1',
                'b':'2'
            ]
        ],
        { response ->
            // appendDebugMessage("response.entity.content: " + response.entity.content.getText() + "\n"*2); //throws a "Stream closed" exception
            appendDebugMessage("response.headers: " + response.headers.dump() + "\n");
            appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n");
            appendDebugMessage("alexaCookie.addCookies('', response.headers): " + alexaCookie.addCookies('', response.headers) + "\n");


            appendDebugMessage("\n");
            appendDebugMessage("response.getContext().delegate.map: " + "\n" + response.getContext().delegate.map.collect{"\t"*1 + it.key + ": " + it.value.dump()}.join("\n") + "\n"*2);
            appendDebugMessage("response.getContext().delegate.map.keySet(): " + "\n" + response.getContext().delegate.map.keySet().collect{"\t"*1 + it}.join("\n") + "\n"*2);
            appendDebugMessage("\n");
            // appendDebugMessage("response.getContext()['http.request'].getURI(): " + response.getContext()['http.request'].getURI().dump() + "\n");
            // appendDebugMessage("response.getContext()['http.request'].getTarget(): " + response.getContext()['http.request'].getTarget().dump() + "\n");
            // appendDebugMessage("response.getContext()['http.request'].getRequestLine(): " + response.getContext()['http.request'].getRequestLine().dump() + "\n");
            // appendDebugMessage("response.getContext()['http.request'].getRequestLine().getUri(): " + response.getContext()['http.request'].getRequestLine().getUri() + "\n");


            // appendDebugMessage("response.getContext()['http.request'].getOriginal().getRequestLine(): " + response.getContext()['http.request'].getOriginal().getRequestLine().dump() + "\n");
            // appendDebugMessage("response.getContext()['http.request'].getOriginal().getRequestLine().getUri(): " + response.getContext()['http.request'].getOriginal().getRequestLine().getUri() + "\n");

            // appendDebugMessage("response.getContext()['http.target_host'].hostname: " + response.getContext()['http.target_host'].hostname + "\n");

            // appendDebugMessage("response.data: " + response.data.dump() + "\n");
            // appendDebugMessage("response.entity: " + response.entity.dump() + "\n"*2);
            // appendDebugMessage("response.getContext()['http.response']: " + response.getContext()['http.response'].dump() + "\n"*2);
            // appendDebugMessage("response.getContext()['http.response'].original: " + response.getContext()['http.response'].original.dump() + "\n"*2);
            // appendDebugMessage("response.getContext()['http.response'].original: " + response.getContext()['http.response'].original.toString() + "\n"*2);
            // appendDebugMessage("response.entity.content: " + response.entity.content.dump() + "\n"*2);
            // // appendDebugMessage("response.entity.content: " + response.entity.content.getText() + "\n"*2);
            // // appendDebugMessage("response.getContext()['http.response'].entity.content: " + response.getContext()['http.response'].entity.content.getText() + "\n"*2);
            // // appendDebugMessage("response.getContext()['http.response'].original.entity.content: " + response.getContext()['http.response'].original.entity.content.getText() + "\n"*2);
            // appendDebugMessage("response.getContext()['http.response'].params: " + response.getContext()['http.response'].params.dump() + "\n"*2);
        }
    );

    // a = new hubitat.helper.InterfaceUtils();

    // appendDebugMessage("hubitat.helper.InterfaceUtils: " + hubitat.helper.InterfaceUtils.dump() + "\n");

    // appendDebugMessage("hubitat.helper.InterfaceUtils.getMethods(): " + "\n" + hubitat.helper.InterfaceUtils.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n");

    // appendDebugMessage("hubitat.helper.InterfaceUtils: " + hubitat.helper.InterfaceUtils.dump() + "\n");
    // appendDebugMessage("hubitat.helper.InterfaceUtils.getMethods(): " + "\n" + hubitat.helper.InterfaceUtils.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n");

    // // appendDebugMessage("hubitat: " + hubitat.dump() + "\n");
    // // appendDebugMessage("hubitat.getMethods(): " + "\n" + hubitat.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n");

    // appendDebugMessage("location.hub: " + location.hub.dump() + "\n");
    // appendDebugMessage("location.hub.class.getMethods(): " + "\n" + location.hub.class.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n");

    // appendDebugMessage('HTTPBuilder: ' + (new HTTPBuilder()).dump());

    alexaCookie.generateAlexaCookie(
        'email': settings.amazon_username,
        'password': settings.amazon_password,
        'options':  [
            'logger': {appendDebugMessage(it + "\n");},
            'amazonPage': "amazon.com",
            'acceptLanguage':'en-US',
            'userAgent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36'
        ],
        'callback': {String error, Map result ->
            if(error){
                appendDebugMessage("error string that resulted from attempting to generate alexa cookie: " + error + "\n");
            }
            if(result){
                appendDebugMessage("result of attempting to generate alexa cookie: " + prettyPrint(result) + "\n");
            }
        }
    );

   

}

def mainTestCode3(){
    // String htmlString = "<p style=\"color:green;\">ahoy there</p>"
    // log.debug(htmlString);
    
    
    // // def escapedHtmlString = org.apache.commons.text.StringEscapeUtils.escapeHtml4('ahoy');
    // // def escapedHtmlString = StringEscapeUtils.escapeHtml4('ahoy');
    // def escapedHtmlString = groovy.xml.XmlUtil.escapeXml(htmlString);
    // log.debug(escapedHtmlString);

    // return;

    // def markupBuilder = new groovy.xml.MarkupBuilder();
    
    // markupBuilder.root {
    //     a { mkp.yield('blabbedy') }
    // };

    // return;
    // def a = new org.apache.xerces.dom.DocumentImpl();

    // debugMessage = "ahodfgafgasddy"; return;
    // foo = "wahtever"; return;
	// setDebugMessage("ahoy"); 
	// debugMessage += "there"; return;
}

def mainTestCode2(){
    startCollectionOfDebugMessage();

	// def debugMessage = ""

	appendDebugMessage("\n\n");
   
    // appendDebugMessage("this: " + this.dump() + "\n");
    // appendDebugMessage("this.class: " + this.class + "\n");

    // appendDebugMessage("\n\n");
    
    // appendDebugMessage("this.class.getDeclaredFields(): " + "\n");
    // this.class.getDeclaredFields().each{message += it.toString() + "\n";	}
    
    // appendDebugMessage("\n\n");
    // appendDebugMessage("this.class.getMethods(): " + "\n");
    // this.class.getMethods().each{	debugMessage += it.toString() + "\n";}

    // appendDebugMessage("\n\n");

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
    
    // httpGet(
    //     [
    //         'uri': "https://postman-echo.com/cookies/set?foo1=bar1&foo2=bar2",
    //         'query': [
    //             'foo1':'bar1',
    //             'foo2':'bar2'
    //         ]
    //     ],

    //     {response ->
    //         appendDebugMessage("response.headers: " + response.headers.dump() + "\n");
    //         // appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.dump()}.join("\n") + "\n");
    //         appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n");
    //         appendDebugMessage("response.getAllHeaders(): " + "\n" + response.getAllHeaders().collect{
    //                 "\t"*1 + it.getName() + ": " + "\n" +
    //                 "\t"*2 + it.getValue() + "\n" + 
    //                 "\t"*2 + "elements" + "\n" + 
    //                     it.getElements().collect{"\t"*3 +  it.name + ": " + it.value }.join("\n")
    //             }.join("\n") + "\n");
    //        //appendDebugMessage("\n"*2);
    //         appendDebugMessage("AlexaCookie().addCookies('', response.headers): " + AlexaCookie().addCookies('', response.headers) + "\n");
    //         appendDebugMessage("response.data: " + response.data.toString() + "\n");
    //     }
    // );

    //   List<Integer> a = [11,22,33];
    // final List<Integer> b = [11,22,33];
    //       List<Integer> c = [11,22,33].asImmutable();
    // final List<Integer> d = [11,22,33].asImmutable();

    // try{a << 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to append to a: " + e + "\n");}
    // try{b << 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to append to b: " + e + "\n");}
    // try{c << 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to append to c: " + e + "\n");}
    // try{d << 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to append to d: " + e + "\n");}


    // try{a[0] = 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to modify a: " + e + "\n");}
    // try{b[0] = 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to modify b: " + e + "\n");}
    // try{c[0] = 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to modify c: " + e + "\n");}
    // try{d[0] = 44; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to modify d: " + e + "\n");}

    // try{a[1] = "ahoy"; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to assign a string to an alement of a: " + e + "\n");}
    // try{b[1] = "ahoy"; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to assign a string to an alement of b: " + e + "\n");}
    // try{c[1] = "ahoy"; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to assign a string to an alement of c: " + e + "\n");}
    // try{d[1] = "ahoy"; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to assign a string to an alement of d: " + e + "\n");}

    // try{a = [55,66]; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to overwrite a: " + e + "\n");}
    // try{b = [55,66]; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to overwrite b: " + e + "\n");}
    // try{c = [55,66]; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to overwrite c: " + e + "\n");}
    // try{d = [55,66]; } catch(Exception e) {appendDebugMessage("encountered exception when attempting to overwrite d: " + e + "\n");}



    // appendDebugMessage("a.dump(): " + a.dump() + "\n");
    // appendDebugMessage("a: " + "\n" + a.collect{"\t"*1 + it}.join("\n") + "\n");

    // appendDebugMessage("b.dump(): " + b.dump() + "\n");
    // appendDebugMessage("b: " + "\n" + b.collect{"\t"*1 + it}.join("\n") + "\n");

    // appendDebugMessage("c.dump(): " + c.dump() + "\n");
    // appendDebugMessage("c: " + "\n" + c.collect{"\t"*1 + it}.join("\n") + "\n");

    // appendDebugMessage("d.dump(): " + d.dump() + "\n");
    // appendDebugMessage("d: " + "\n" + d.collect{"\t"*1 + it}.join("\n") + "\n");

    // appendDebugMessage("(new org.apache.http.protocol.HttpCoreContext()).HTTP_REQUEST: " + (new HttpCoreContext()).HTTP_REQUEST + "\n");
    // appendDebugMessage("HTTP_REQUEST: " + HTTP_REQUEST + "\n");

    // java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
    // java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder().uri(URI.create("http://foo.com/")).build();


    httpGet(
        [
            'uri': "https://postman-echo.com/cookies/set?foo1=bar1&foo2=bar2",
            'query': [
                'foo1':'bar1',
                'foo2':'bar2'
            ]
        ],
        { response ->
            appendDebugMessage("response.headers: " + response.headers.dump() + "\n");
            appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n");
            appendDebugMessage("AlexaCookie().addCookies('', response.headers): " + AlexaCookie().addCookies('', response.headers) + "\n");
            appendDebugMessage("response.data: " + response.data.dump() + "\n");
            appendDebugMessage("response.getContext(): " + response.getContext().dump() + "\n");
            appendDebugMessage("\n");
            appendDebugMessage("prettyPrint(response.getContext()): " + prettyPrint(response.getContext()) + "\n");
            appendDebugMessage("\n");
            appendDebugMessage("response.getContext().getClass().getFields(): " + "\n" + response.getContext().delegate.getProperties().collect{"\t"*1 + it.toString()}.join("\n") + "\n");
            // appendDebugMessage("response.getContext(): " + response.getContext().collect{it.toString()}.join("\n") + "\n");
            appendDebugMessage("\n");
            //see the org.apache.http.protocol.HttpContext and org.apache.http.protocol.HttpCoreContext sections of https://hc.apache.org/httpcomponents-core-4.4.x/httpcore/apidocs/constant-values.html
            // to know which keys are available in the context.
            // appendDebugMessage("response.getContext()['http.request']: " + response.getContext()['http.request'].dump() + "\n");
            appendDebugMessage("response.getContext().delegate: " + response.getContext().delegate.dump() + "\n");
            appendDebugMessage("\n");




            candidateKeys = ["http.", "http.connection","http.request_sent", "http.request", "http.response", "http.target_host"];

            appendDebugMessage(
                candidateKeys.collect{
                    candidateKey ->
                    "response.getContext()['${candidateKey}']: " + response.getContext()[candidateKey]?.dump()
                }.join("\n"*2)
            );
 

            appendDebugMessage("response.getContext().delegate.map: " + response.getContext().delegate.map.dump() + "\n"*2);
            appendDebugMessage("response.getContext().delegate.map: " + "\n" + response.getContext().delegate.map.collect{"\t"*1 + it.key + ": " + it.value.dump()}.join("\n") + "\n"*2);

            // // appendDebugMessage("httpGet: " + this.httpGet.dump() + "\n");
            // appendDebugMessage("this: " + this.dump() + "\n");
            // appendDebugMessage("this.delegate: " + this.delegate.dump() + "\n");
            // appendDebugMessage("this.class.getMethods(): " + "\n" +  this.class.getMethods().collect{"\t" + it.toString()}.join("\n") + "\n");

            // def xxx = new HTTPBuilder();


            appendDebugMessage("response.context['http.request']: " + response.context['http.request'].dump() + "\n"*2);
            appendDebugMessage("response.context['http.request'].original: " + response.context['http.request'].original.dump() + "\n"*2);
            appendDebugMessage("response.context['http.request'].original: " + response.context['http.request'].original.toString() + "\n"*2);

            appendDebugMessage("response.context['http.connection']: " + response.context['http.connection'].dump() + "\n"*2);
            appendDebugMessage("response.context['http.connection'].class.getMethods(): " + response.context['http.connection'].class.getMethods().collect{"\t" + it.toString()}.join("\n") + "\n"*2);

            // appendDebugMessage("response.context['http.connection'].getMetrics(): " + response.context['http.connection'].getMetrics().dump() + "\n"*2);
            appendDebugMessage("response.context['http.connection'].isStale(): " + response.context['http.connection'].isStale().toString() + "\n"*2);
            // appendDebugMessage("response.context['http.connection'].newProxy(): " + response.context['http.connection'].getMetrics().newProxy() + "\n"*2);
            

        }
    );

    stopCollectionOfDebugMessage();
   return respondFromTestCode(debugMessage);
}

//}


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
            );
            input(
                name: "amazon_password", 
                title: "Amazon password" ,
                type: "text", 
                description: "",            
                required:false,
                submitOnChange:false 
            ); 
            input(
                name: "manuallyEnteredAlexaRefreshOptions", 
                type: "text",
                title: "Alexa cookie refresh options", 
                required: false, 
                submitOnChange: false
            );
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
    String hashOfManuallyEnteredAlexaRefreshOptions = sha1(settings.manuallyEnteredAlexaRefreshOptions);

	if(state.hashOfLastManuallyEnteredAlexaRefreshOptions != hashOfManuallyEnteredAlexaRefreshOptions){
        state.hashOfLastManuallyEnteredAlexaRefreshOptions = hashOfManuallyEnteredAlexaRefreshOptions;
        // it might make sense to give the user some warning before overwriting the existing alexaRefreshOptions
        // in the case where the existing alexaRefreshOptions had been obtained programmatically by the refreshAlexaCookie()
        // function (i.e. not manually entered).
        handleNewAlexaRefreshOptions(settings.manuallyEnteredAlexaRefreshOptions);
        state.timeOfLastManualEntryOfAlexaRefreshOptions = now();
    }
}


/**
* returns a fully-formed AlexaCookie object analagous to the
AlexaCookie object created by the code in alexa-cookie.js
*/
private Map  getAlexaCookie() {
    #include "alexa_cookie_utility.groovy"
    return alexaCookieUtility;
}


/**
*  returns the sha1 hash of a String.
*/
private String sha1(String x){
    return java.security.MessageDigest.getInstance('SHA-1').digest(x.getBytes()).encodeHex().toString();
}


/**
*  I have taken this function verbatim from alexa-tts-manager:
*/
private String getCookieFromOptions(options) {
    try{
        def cookie = new groovy.json.JsonSlurper().parseText(options);
        if (!cookie || cookie == "") {
            log.error("'getCookieFromOptions()': wrong options format!");
            //notifyIfEnabled("Alexa TTS: Error parsing cookie, see logs for more information!");
            return "";
        }
        cookie = cookie.localCookie.replace('"',"");
        if(cookie.endsWith(",")) {
            cookie = cookie.reverse().drop(1).reverse();
        }
        cookie += ";";
        log.info("Alexa TTS: new cookie parsed succesfully");
        return cookie;
    } catch(e){
        log.error("'getCookieFromOptions()': error = ${e}");
        //notifyIfEnabled("Alexa TTS: Error parsing cookie, see logs for more information!");
        return "";
    }
}