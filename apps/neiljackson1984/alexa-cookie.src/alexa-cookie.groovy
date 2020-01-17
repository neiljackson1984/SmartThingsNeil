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

private void mainTestCode(){
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

    // Map _ = [:]; //there's nothing special about the identifier "_", we are just using it because it's short and doesn't impair the readability of the code too much.  We are using it as the identifier for the object that we are construction and will return.

    def proxyServer;
    Map _options = [:];
    String Cookie='';
    final String defaultAmazonPage = 'amazon.de';
    final String defaultUserAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:99.0) Gecko/20100101 Firefox/99.0';
    final String defaultUserAgentLinux = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36';
    final String defaultAcceptLanguage = 'de-DE';

    final List<String> csrfPathCandidates = [
        '/api/language',
        '/spa/index.html',
        '/api/devices-v2/device?cached=false',
        '/templates/oobe/d-device-pick.handlebars',
        '/api/strings'
    ].asImmutable();
    //Groovy does not respect my final, nor my "<String>" type specification, but they are my intent nonetheless.

    /**
    *   prettyPrint() serves only to make the debugging messages look nicer.
    */
    Closure prettyPrint = {
            return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(it));
    };

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
    // Closure cookie_parse = {String str, Map options=[:] ->
    Closure cookie_parse = {String str, Map options=[:] ->
        if (! str instanceof String ) {
            throw new Exception("argument str must be a string");
        }
        if(!str){str='';}

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
    };

    /**
     *  applies any cookies that may be present in 
     *  a set of http headers (an iterable of org.apache.http.Header)  to an existing Cookie string (adding any cookies that
     *  that do not already exist, and updating any that do.)
     *  Returns the updated version of the cookie string.
     */
    Closure addCookies = {String cookie, headers ->
        String internalDebugMessage = "";
        internalDebugMessage += "addCookies run summary:" + "\n";
        internalDebugMessage += "starting with: " + cookie + "\n";
        String returnValue;
        // if (!headers || !('set-cookie' in headers)){
        if (!headers || !headers.any{it.name.toLowerCase() == "set-cookie"} ){
            internalDebugMessage += ("could not find a 'set-cookie' header in headers." + "\n");
            returnValue =  cookie; 
        } else {
            if(!cookie){
                cookie='';
            }   

            // original javascript:   
            //      const cookies = cookieTools.parse(Cookie);
            def cookies = cookie_parse(cookie); 

            

            // original javascript:   
            //    for (let cookie of headers['set-cookie']) {
            // according to https://nodejs.org/api/http.html#http_message_headers ,
            // headers['set-cookie'] will always be an array. (This especially makes sense when the response contains multiple 'set-cookie' 
            // headers (which I guess is allowed (i.e. it seems that the collection of headers is not strictly an associative map, because 
            // you can have multiple entries having the same 'key'.  I guess the collection of headers is more like a list 
            // of (name, value) pairs.)

            for (def headerValue in headers.findAll{it.name.toLowerCase() == "set-cookie"}.collect{it.value}){
                // original javascript: cookie = cookie.match(/^([^=]+)=([^;]+);.*/);
                // we expect headerValue to be a string that looks like "foo=blabbedy blabbedy blabbedy ;"
                
                cookieMatch = (~/^([^=]+)=([^;]+);.*/).matcher(headerValue)[0];

                //original javascript:  if (cookie && cookie.length === 3) {
                if (cookieMatch && cookieMatch.size() == 3) {
                    
                    // original javascript:  if (cookie[1] === 'ap-fid' && cookie[2] === '""') continue;
                    if (cookieMatch[1] == 'ap-fid' && cookieMatch[2] == '""'){ continue;}
                    
                    //original javascript: if (cookies[cookie[1]] && cookies[cookie[1]] !== cookie[2]) {
                    if( (cookieMatch[1] in cookies) && (cookies[cookieMatch[1]] != cookieMatch[2]) ){
                        //original javascript: _options.logger && _options.logger('Alexa-Cookie: Update Cookie ' + cookie[1] + ' = ' + cookie[2]);
                        internalDebugMessage += ('Alexa-Cookie: Update Cookie ' + cookieMatch[1] + ' = ' + cookieMatch[2]) + "\n";
                    } else if (!(cookieMatch[1] in cookies) ) {
                        internalDebugMessage += ('Alexa-Cookie: Add Cookie ' + cookieMatch[1] + ' = ' + cookieMatch[2]) + "\n";
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
            // cookie = '';
            // for (name in cookies.keySet()){
            //     cookie += name + '=' + cookies[name] + '; ';
            // }

            // return cookie;  //>    return Cookie;
            returnValue = cookies.collect{it.key + "=" + it.value}.join("; ");
        }
        internalDebugMessage += "addCookies is returning: " + returnValue + "\n";
        _options.logger && _options.logger(internalDebugMessage);
        return returnValue;
    };

    Closure getFields = {String body ->
        Map returnValue = [:];
        //replace carriage returns and newlines with spaces
        body = body.replace("\r", ' ').replace("\n", ' ');
        fieldBlockMatcher = (~/^.*?("hidden"\s*name=".*$)/).matcher(body);
        if (fieldBlockMatcher.find()) {
            fieldMatcher = (~/.*?name="([^"]+)"[\s^\s]*value="([^"]+).*?"/).matcher(fieldBlockMatcher.group(1));
            while (fieldMatcher.find()) {
                if (fieldMatcher.group(1) != 'rememberMe') {
                    returnValue[fieldMatcher.group(1)] = fieldMatcher.group(2);
                }
            }
        }
        return returnValue; 
    };

    Closure initConfig = {
        _options.logger = _options.logger ?: Closure.IDENTITY; //default logger is to do nothing (I choose to use Closure.IDENTITY instead of a literal closure expression here in the hopes that Closure.IDENTITY will incur less runtime overhead.)

        _options.amazonPage = _options.amazonPage ?: _options.formerRegistrationData?.amazonPage ?: defaultAmazonPage;
        _options.logger('Alexa-Cookie: Use as Login-Amazon-URL: ' + _options.amazonPage);

        _options.userAgent = _options.userAgent ?: defaultUserAgentLinux;
        _options.logger('Alexa-Cookie: Use as User-Agent: ' + _options.userAgent);
        
        _options.acceptLanguage = _options.acceptLanguage ?: defaultAcceptLanguage;
        _options.logger('Alexa-Cookie: Use as Accept-Language: ' + _options.acceptLanguage);

        if (_options.setupProxy && !_options.proxyOwnIp) {
            _options.logger('Alexa-Cookie: Own-IP Setting missing for Proxy. Disabling!');
            _options.setupProxy = false;
        }
        if (_options.setupProxy) {
            _options.setupProxy = true;
            _options.proxyPort = _options.proxyPort ?: 0;
            _options.proxyListenBind = _options.proxyListenBind ?: '0.0.0.0';
            _options.logger('Alexa-Cookie: Proxy-Mode enabled if needed: ' + _options.proxyOwnIp + ':' + _options.proxyPort + ' to listen on ' + _options.proxyListenBind);
        } else {
            _options.setupProxy = false;
            _options.logger('Alexa-Cookie: Proxy mode disabled');
        }
        _options.proxyLogLevel = _options.proxyLogLevel ?: 'warn';
        _options.amazonPageProxyLanguage = _options.amazonPageProxyLanguage ?: 'de_DE';

        if(_options.formerRegistrationData){ _options.proxyOnly = true; }
    };

    Closure getCSRFFromCookies = {Map namedArgs  -> 
        String cookie = namedArgs.cookie;
        Map options = namedArgs.options ?: [:];
        Closure callback = namedArgs.callback;
        
        String csrf = null; //our goal is to obtain a csrf token and assign it to this string. 
        for(csrfPathCandidate in csrfPathCandidates){
            options.logger && options.logger('Alexa-Cookie: Step 4: get CSRF via ' + csrfPathCandidate);
            httpGet(
                [
                    uri: "https://alexa." + options.amazonPage + csrfPathCandidate,
                    'headers': [
                        'DNT': '1',
                        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36',
                        'Connection': 'keep-alive',
                        'Referer': 'https://alexa.' + options.amazonPage + '/spa/index.html',
                        'Cookie': cookie,
                        'Accept': '*/*',
                        'Origin': 'https://alexa.' + options.amazonPage  
                    ]
                ],
                {response ->
                    cookie = addCookies(cookie, response.headers);
                    java.util.regex.Matcher csrfMatcher = (~/csrf=([^;]+)/).matcher(cookie);
                    if(csrfMatcher.find()){
                        csrf = csrfMatcher.group(1);
                        options.logger && options.logger('Alexa-Cookie: Result: csrf=' + csrf.toString() + ', Cookie=' + cookie);
                    }
                }
            );
            if(csrf){
                callback && callback(null, [
                    'cookie':cookie,
                    'csrf':csrf
                ]);
                return;
            }
        }

        //it seems like we should do something here to handle the case where no csrf could be obtained,
        // but the original javascript does not seem to do any such error handling.
    };

    Closure getLocalCookies = {Map namedArgs ->
        String amazonPage = namedArgs.amazonPage;
        String refreshToken = namedArgs.refreshToken;
        Closure callback = namedArgs.callback;

        Cookie = ''; //comment from original javascript: reset because we are switching domains
        //comment from original javascript: Token Exchange to Amazon Country Page
        Map exchangeParams = [
            'di.os.name': 'iOS',
            'app_version': '2.2.223830.0',
            'domain': '.' + amazonPage,
            'source_token': refreshToken,
            'requested_token_type': 'auth_cookies',
            'source_token_type': 'refresh_token',
            'di.hw.version': 'iPhone',
            'di.sdk.version': '6.10.0',
            'cookies': ('{„cookies“:{".' + amazonPage + '":[]}}').bytes.encodeBase64().toString(),
            'app_name': 'Amazon Alexa',
            'di.os.version': '11.4.1'
        ];
        Map requestParams = [
            uri: 'https://' + 'www.' + amazonPage + '/ap/exchangetoken',
            headers: [
                'User-Agent': 'AmazonWebView/Amazon Alexa/2.2.223830.0/iOS/11.4.1/iPhone',
                'Accept-Language': _options.acceptLanguage,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': '*/*'
            ],
            contentType: groovyx.net.http.ContentType.JSON, // type of content that we expect the response to contain //this influences the type of object that the system passes to the callback. ,
            requestContentType: groovyx.net.http.ContentType.URLENC, //type of content that the request will contain.  corresponds to the 'Content-Type' header of the request. By default, this is assumed to be the same as the expected content type of the response, unless explicitly specified //this influences how the system treats the body of the request.   
            body: exchangeParams 
        ];
        _options.logger('Alexa-Cookie: Exchange tokens for ' + amazonPage);
        _options.logger(prettyPrint(requestParams));
        httpPost(requestParams,
            {response ->
                //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                //TODO: handle malformed response data here.
                _options.logger('Exchange Token Response: ' + prettyPrint(response.data));
                // if (!body.response || !body.response.tokens || !body.response.tokens.cookies) {
                if (!response.data.response?.tokens?.cookies) {
                    callback && callback('No cookies in Exchange response', null);
                    return;
                }
                if (!response.data.response.tokens.cookies['.' + amazonPage]) {
                    callback && callback('No cookies for ' + amazonPage + ' in Exchange response', null);
                    return;
                }

                Cookie = addCookies(Cookie, response.headers);
                Map cookies = cookie_parse(Cookie);
                response.data.response.tokens.cookies['.' + amazonPage].each {cookie ->
                    if (cookies[cookie.Name] && cookies[cookie.Name] != cookie.Value) {
                        _options.logger('Alexa-Cookie: Update Cookie ' + cookie.Name + ' = ' + cookie.Value);
                    } else if (!cookies[cookie.Name]) {
                        _options.logger('Alexa-Cookie: Add Cookie ' + cookie.Name + ' = ' + cookie.Value);
                    }
                    cookies[cookie.Name] = cookie.Value;
                };

                // String localCookie = '';
                // for (String name in cookies.keySet()) {
                //     localCookie += name + '=' + cookies[name] + '; ';
                // }
                // localCookie = localCookie.replace(/[; ]*$/, '');

                String localCookie = cookies.collect{it.key + "=" + it.value}.join("; ");
                callback && callback(null, localCookie);
            }
        );
    };

    Closure handleTokenRegistration = {Map namedArgs ->
        Map options = namedArgs.options ?: [:];
        Map loginData = namedArgs.loginData ?: [:];
        Closure callback = namedArgs.callback;
        options.logger && options.logger('Handle token registration Start: ' + prettyPrint(loginData));
        String deviceSerial;
        if(options.formerRegistrationData?.deviceSerial){
            options.logger && options.logger('Proxy Init: reuse deviceSerial from former data');
            deviceSerial = options.formerRegistrationData.deviceSerial;
        } else {
            Byte[] deviceSerialBuffer = new Byte[16];
            for (def i = 0; i < deviceSerialBuffer.size(); i++) {
                deviceSerialBuffer[i] = floor(random() * 255);
            }
            deviceSerial = deviceSerialBuffer.encodeHex().toString();
        }
        loginData.deviceSerial = deviceSerial;
        Map cookies = cookie_parse(loginData.loginCookie);
        Cookie = loginData.loginCookie;

        //comment from original javascript: Register App
        Map registerData = [
            "requested_extensions": [
                "device_info",
                "customer_info"
            ],
            "cookies": [
                "website_cookies": cookies.collect{ ["Value": it.value,  "Name": it.key] },
                "domain": ".amazon.com"
            ],
            "registration_data": [
                "domain": "Device",
                "app_version": "2.2.223830.0",
                "device_type": "A2IVLV5VM2W81",
                "device_name": "%FIRST_NAME%\u0027s%DUPE_STRATEGY_1ST%ioBroker Alexa2",
                "os_version": "11.4.1",
                "device_serial": deviceSerial,
                "device_model": "iPhone",
                "app_name": "ioBroker Alexa2",
                "software_version": "1"
            ],
            "auth_data": [
                "access_token": loginData.accessToken
            ],
            "user_context_map": [
                "frc": cookies.frc
            ],
            "requested_token_type": [
                "bearer",
                "mac_dms",
                "website_cookies"
            ]
        ];

        Map requestParams0 = [
            uri: "https://api.amazon.com/auth/register",
            headers: [
                'User-Agent': 'AmazonWebView/Amazon Alexa/2.2.223830.0/iOS/11.4.1/iPhone',
                'Accept-Language': options.acceptLanguage,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'Cookie': loginData.loginCookie,
                'Accept': '*/*',
                'x-amzn-identity-auth-domain': 'api.amazon.com'
            ],
            contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
            requestContentType: groovyx.net.http.ContentType.JSON,  //this influences how the system treats the body of the request.   
            body: registerData
        ];
        options.logger && options.logger('Alexa-Cookie: Register App');
        options.logger && options.logger(prettyPrint(requestParams0));
        httpPost(requestParams0,
            {response0 ->
                //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                //TODO: handle malformed response data here.

                options.logger && options.logger('Register App Response: ' + prettyPrint(response0.data));

                if(! response0.data.response?.success?.tokens?.bearer){
                    callback && callback('No tokens in Register response', null);
                    return;
                }
                Cookie = addCookies(Cookie, response0.headers);
                loginData.refreshToken = response0.data.response.success.tokens.bearer.refresh_token;
                loginData.tokenDate = now();

                //comment from original javascript: Get Amazon Marketplace Country
                Map requestParams1 = [
                    uri: "https://alexa.amazon.com/api/users/me?platform=ios&version=2.2.223830.0",
                    headers: [
                        'User-Agent': 'AmazonWebView/Amazon Alexa/2.2.223830.0/iOS/11.4.1/iPhone',
                        'Accept-Language': options.acceptLanguage,
                        'Accept-Charset': 'utf-8',
                        'Connection': 'keep-alive',
                        'Accept': 'application/json',
                        'Cookie': Cookie
                    ],
                    contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
                ];
                options.logger && options.logger('Alexa-Cookie: Get User data');
                options.logger && options.logger(prettyPrint(requestParams1));
                httpGet(requestParams1,
                    {response1 -> 
                        //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                        options.logger && options.logger('Get User data Response: ' + prettyPrint(response1.data));
                        Cookie = addCookies(Cookie, response1.headers);
                        if (response1.data.marketPlaceDomainName) {
                            java.util.regex.Matcher amazonPageMatcher = (~/^[^\.]*\.([\S\s]*)$/).matcher(response1.data.marketPlaceDomainName);
                            if(amazonPageMatcher.find()){
                                options.amazonPage = amazonPageMatcher.group(1);
                            }
                        }
                        loginData.amazonPage = options.amazonPage;
                        loginData.loginCookie = Cookie;
                        getLocalCookies(
                            amazonPage: loginData.amazonPage, 
                            refreshToken: loginData.refreshToken, 
                            callback: {String err0, String localCookie ->
                                if (err0) {
                                    callback && callback(err0, null);
                                }
                                loginData.localCookie = localCookie;
                                getCSRFFromCookies(
                                    cookie: loginData.localCookie, 
                                    options: options,
                                    callback: {String err1, Map resData ->
                                        if (err1) {
                                            callback && callback('Error getting csrf for ' + loginData.amazonPage + ':' + err1, null);
                                            return;
                                        }
                                        loginData.localCookie = resData.cookie;
                                        loginData.csrf = resData.csrf;
                                        // loginData.removeAll{key, value -> key == 'accessToken'};
                                        loginData.remove('accessToken');
                                        options.logger && options.logger('Final Registraton Result: ' + prettyPrint(loginData));
                                        callback && callback(null, loginData);
                                    }
                                );
                            }
                        );
                    }
                );

            }
        );
    };

    //======== publicly exposed methods: ==============
    Closure generateAlexaCookie = {Map namedArgs  ->
        String email     = namedArgs.email;
        String password  = namedArgs.password;
        Map __options    = namedArgs.options ?: [:];
        Closure callback = namedArgs.callback; //expected to be a callback having signature:  void callback(String err, Map data)
        if (!email || !password) {__options.proxyOnly = true;}
        _options = __options;
        initConfig();

        if(_options.proxyOnly){
            //TO-DO: start the proxy server (not yet implemented) and instruct the user to go attempt to login using the proxy server.
        } else {
            // comment from the original javascript: get first cookie and write redirection target into referer
            _options.logger('Alexa-Cookie: Step 1: get first cookie and authentication redirect');
            Map requestParams0 = [
                'uri': "https://" +  'alexa.' + _options.amazonPage,
                'headers': [
                    'DNT': '1',
                    'Upgrade-Insecure-Requests': '1',
                    'User-Agent': _options.userAgent,
                    'Accept-Language': _options.acceptLanguage,
                    'Connection': 'keep-alive',
                    'Accept': '*/*'
                ],
                'contentType':groovyx.net.http.ContentType.TEXT //this influences the type of object that the system passes to the callback.  if we omit this request parameter, the resultant response.data object is a groovy...nodeChild (I forget the exact class name), and we are unable to extract the complete text of the response.
            ];
            _options.logger("working on step 1, we will now attempt to get " + requestParams0.uri);
            httpGet(requestParams0,
                {response0 ->
                    _options.logger('First response has been received.');
                    _options.logger("\n" + "response0.headers: " + "\n" + response0.headers.collect{"\t"*1 + it.name + ": " + it.value}.join("\n\n") + "\n");
                    _options.logger('Alexa-Cookie: Step 2: login empty to generate session');
                    //TO-DO: handle request errors here.
                    Cookie = addCookies(Cookie, response0.headers);
                    String response0Text = response0.data.getText();
                    

                    _options.logger("Cookie: " + Cookie);
                    _options.logger("cookie_parse(Cookie): " + prettyPrint(cookie_parse(Cookie)));
                    _options.logger("\n" + "response0.getContext()['http.request'].getRequestLine().getUri(): " + response0.getContext()['http.request'].getRequestLine().getUri());
                    _options.logger("\n" + "response0.getContext()['http.request'].getOriginal().getRequestLine().getUri(): " + response0.getContext()['http.request'].getOriginal().getRequestLine().getUri() + "\n");
                    _options.logger("response0.contentType: " + response0.contentType);
                    // _options.logger("response0Text.class: " + response0Text.class);
                    _options.logger("response0Text.length(): " + response0Text.length());
                    
                    // response0Text seems to be quite large, and therefoer takse a long to process (mainly, what we need to do with it is read the fields).
                    // We could probably be more intelligent about consuming response0.data (which is an input stream), to stop reading as soon as we have found all the fields, rather tahn converting the whole 
                    // enormous thing into an enormous string, and then trying to find the fields in the string.
                    // _options.logger("response0Text: " + response0Text);
                    Map response0Fields = getFields(response0Text);
                    _options.logger('response0Fields: ' + prettyPrint(response0Fields) + "\n\n" )
                    // return;
                    // _options.logger(prettyPrint(getFields(response0.data.toString())));
                    // _options.logger("\n\n" );
                    // _options.logger('response0.data: ' + response0.data.dump().take(300) + "\n\n" );
                    // _options.logger('response0.data.parent(): ' + response0.data.parent().dump().take(300)  + "\n\n" );
                    
                    // _options.logger("response0.data.text: " + response0.data.text.take(25) + (response0.data.text().length() > 25 ? "..." : '')); //throws an exception java.io.IOException: Stream closed, becauase we can only read the string once
                    // _options.logger("response0Text: " + response0Text.take(25) + (response0Text.length() > 25 ? "..." : ''));

                    // _options.logger("response0.data.toString(): " + response0.data.text.toString());
                    // _options.logger('response0.data.size(): ' + response0.data.size() + "\n\n" );
                    // _options.logger("response0.getParams(): " + response0.getParams().dump());
                    // _options.logger("response0.getContext()['http.response'].getParams(): " + response0.getContext()['http.response'].getParams().dump());
                    // _options.logger("response0.getContext()['http.response'].entity.content: " + response0.getContext()['http.response'].entity.content.getText())
                    // _options.logger("response0.entity.content: " + response0.entity.content.getText())
                    // _options.logger("groovyx.net.http.ContentType.TEXT.toString(): " + groovyx.net.http.ContentType.TEXT.toString())
                    requestParams1 = requestParams0 + [
                        'uri': "https://" +  'www.' + _options.amazonPage + '/ap/signin',
                        'body': response0Fields,
                        'requestContentType': groovyx.net.http.ContentType.URLENC                     
                    ];
                    requestParams1.headers += [
                        'Cookie': Cookie,
                        'Referer': 'https://' + {it.host + it.path}(new java.net.URI(response0.getContext()['http.request'].getOriginal().getRequestLine().getUri())),
                        'Content-Type': 'application/x-www-form-urlencoded'
                    ];

                    _options.logger("working on step 2, we will now attempt to post to " + requestParams1.uri + " the following " + prettyPrint(requestParams1.body));
                    httpPost(requestParams1, 
                        {response1 ->
                            _options.logger('Second response has been received.');
                            //TO-DO: handle request errors here.
                            //comment from the original javascript: // login with filled out form
                            //comment from the original javascript://  !!! referer now contains session in URL
                            Cookie = addCookies(Cookie, response1.headers);
                            String response1Text = response1.data.getText();
                            _options.logger("Cookie: " + Cookie);
                            _options.logger("cookie_parse(Cookie): " + prettyPrint(cookie_parse(Cookie)));

                            _options.logger("response1.contentType: " + response1.contentType);
                            _options.logger("response1Text.length(): " + response1Text.length());
                            Map response1Fields = getFields(response1Text);
                            _options.logger('response1Fields: ' + prettyPrint(response1Fields) + "\n\n" );

                            _options.logger('Alexa-Cookie: Step 3: login with filled form, referer contains session id');
                            requestParams2 = requestParams1 + [
                                'body': 
                                    response1Fields + [
                                        'email': email ?: '',
                                        'password': password ?: ''
                                    ]
                            ];
                            requestParams2.headers += [
                                'Cookie': Cookie,
                                'Referer': "https://www.${_options.amazonPage}/ap/signin/" + (~/session-id=([^;]+)/).matcher(Cookie)[0][1]
                            ];

                            _options.logger("working on step 3, we will now attempt to post to " + requestParams2.uri + " the following: " + prettyPrint(requestParams2.body));
                            httpPost(requestParams2,
                                {response2 ->
                                    _options.logger('Third response has been received.');
                                    //TO-DO: handle request errors here.
                                    //comment form original javascript: // check whether the login has been successful or exit otherwise
                                    
                                    String response2Text = response2.data.getText();
                                    _options.logger("response2.contentType: " + response2.contentType);
                                    _options.logger("response2Text.length(): " + response2Text.length());

                                    if({it.host.startsWith('alexa') && it.path.endsWith('.html')}(new java.net.URI(response0.getContext()['http.request'].getOriginal().getRequestLine().getUri())) ){
                                        //success
                                        return getCSRFFromCookies('cookie':Cookie, 'options':_options, 'callback':callback);
                                        // getCSRFFromCookies calls the callback and passes, as the first argument, an error message (if any), and as a second argument, either null, or a map containg exactly two keys 'cookie' and 'csrf'
                                        // The refresh cookie mechanism requires the 'formerRegistrationData' (which is intended to be what the callback was passed as an argumnet as a result of running generateCookie())
                                        // to contain keys 'loginCookie', 'refreshToken', and the alexa-tts-manager app expects the generateCookie function to be called with a map containing the key 'localCookie' (and possibly other keys).
                                        // If the non-proxy-based flow for generateAlexaCookie did succeed (i.e. if the above test for success were to pass), then callback would end up being called with a second argument being a map that is not suitable for future 
                                        // handling by the refresh cookie process.
                                        // This makes me think that the non-proxy-server-based flow for generateAlexaCookie() is outdated and no longer relevant.  I have observed that, in practice,
                                        // the non-proxy-server-based flow always results in Amazon's servers redirecting to a login page containing a captcha and failing the above test for success.
                                        //  I think the code here should be changed to get rid of the non-proxy based authentication flow (or at least giving it a fighting chance of returning a useable result).

                                        //It is worth noting that the refresh cookie process does not depend on the proxy server at all, and does not even invoke the proxy server as a fallback strategy in case of failure of the automatied strategy.
                                        // This makes me think that the cookie refresh process is probably much more reliable and well-understood than the process of initially acquiring the first formerRegistrationData.
                                    } else {
                                        String errMessage = 'Login unsuccessfull. Please check credentials.';
                                        java.util.regex.Matcher amazonMessageMatcher = (~/auth-warning-message-box[\S\s]*"a-alert-heading">([^<]*)[\S\s]*<li><[^>]*>\s*([^<\n]*)\s*</).matcher(response2Text);
                                        if(amazonMessageMatcher.find()){
                                            errMessage = "Amazon-Login-Error: ${amazonMessageMatcher.group(1)}: ${amazonMessageMatcher.group(2)}";
                                        }
                                        if (_options.setupProxy) {
                                            //TO-DO: present the user with the fallback option of using a proxy server (which we have not yet implemented)
                                        }
                                        callback && callback(errMessage, null);
                                        return;
                                    }
                                }
                            );                          
                        }
                    );
                }
            );
        }
    };

    Closure refreshAlexaCookie = {Map namedArgs -> 
        Map options = namedArgs.options ?: [:];
        Closure callback = namedArgs.callback;
        // namedArgs is expected to have keys 'options' and 'callback'.
        // namedArgs.callback is expected to be a closure having signature void callback(String errorMessage, Map result) .
        // callback will be called with the errorMessage argument being non-null iff. some error has occured.
        // if no errors occur, callback will be called with the 'result' argument being a Map that will contain the keys
        // 'localCookie' (whose value can be messaged to create a cookie string suitabel for calling the Alexa speech api)
        // 'loginCookie' (whose value is used as part of future cookie refresh operations)
        // 'refreshToken' (whose value is used as part of future cookie refresh operations)

        //when making a future call to refreshAlexaCookie, it is expected that namedArgs.options.formerRegistrationData will be 
        // precisely the Map that, in the last call to refreshAlexaCookie, was passed as the 'result' argument to the callback function.

        // we require that we have namedArgs.options.formerRegistrationData.loginCookie and that we have namedArgs.options.formerRegistrationData.refreshToken .
        // If these two values are not available, then we cannot proceed.

        
        
        if(!(options.formerRegistrationData?.loginCookie && options.formerRegistrationData?.refreshToken )){
            callback && callback('No former registration data provided for Cookie Refresh', null);
            return;
        }

        _options = options;
        _options.proxyOnly = true; //it is not obvious that the _options.proxyOnly key serves any real purpose.
        initConfig();

        Map refreshData = [
            "app_name": "ioBroker Alexa2",
            "app_version": "2.2.223830.0",
            "di.sdk.version": "6.10.0",
            "source_token": _options.formerRegistrationData.refreshToken,
            "package_name": "com.amazon.echo",
            "di.hw.version": "iPhone",
            "platform": "iOS",
            "requested_token_type": "access_token",
            "source_token_type": "refresh_token",
            "di.os.name": "iOS",
            "di.os.version": "11.4.1",
            "current_version": "6.10.0"
        ];
        Cookie = _options.formerRegistrationData.loginCookie;
        Map requestParams = [
            uri: "https://api.amazon.com/auth/token",
            headers: [
                'User-Agent': 'AmazonWebView/Amazon Alexa/2.2.223830.0/iOS/11.4.1/iPhone',
                'Accept-Language': _options.acceptLanguage,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': Cookie,
                'Accept': 'application/json',
                'x-amzn-identity-auth-domain': 'api.amazon.com'
            ],
            contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
            requestContentType: groovyx.net.http.ContentType.URLENC, //this influences how the system treats the body of the request.   
            body: refreshData 
        ];
        
        _options.logger('Alexa-Cookie: Refresh Token');
        _options.logger(prettyPrint(requestParams));
        httpPost(requestParams,
            {response ->
                //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                //TODO: handle malformed response data here.
                _options.logger('Refresh Token Response: ' + prettyPrint(response.data));
                _options.formerRegistrationData.loginCookie = addCookies(_options.formerRegistrationData.loginCookie, response.headers);
                if (!response.data.access_token) {
                    callback && callback('No new access token in Refresh Token response', null);
                    return;
                }
                _options.formerRegistrationData.accessToken = response.data.access_token;
                getLocalCookies(
                    amazonPage: 'amazon.com', 
                    refreshToken: _options.formerRegistrationData.refreshToken, 
                    callback: {String err, String comCookie -> 
                        if (err) {
                            callback && callback(err, null);
                            // In the original alexa-cookie.js, there was no return statement here.
                            // However, I am guessing that there ought to be one, because if 
                            // we have failed to getLocalCookies, we are screwed.
                            // Actually maybe that is not the case.
                            // Even if we have failed to get local cookies this time around,
                            // we might be able to make another attempt later.
                        }
                        //comment from original javascript: // Restore frc and map-md
                        _options.logger("_options.formerRegistrationData.loginCookie: " + _options.formerRegistrationData.loginCookie + "\n");
                        Map initCookies = cookie_parse(_options.formerRegistrationData.loginCookie);
                        _options.logger("initCookies: " + "\n" + prettyPrint(initCookies) + "\n\n");
                        String newCookie = 'frc=' + initCookies.frc + '; ';
                        newCookie += 'map-md=' + initCookies['map-md'] + '; ';
                        newCookie += comCookie ?: '';
                        _options.logger("newCookie: " + newCookie + "\n");
                        _options.formerRegistrationData.loginCookie = newCookie;
                        handleTokenRegistration(
                            options: _options, 
                            loginData: _options.formerRegistrationData, 
                            callback: callback
                        );
                    }
                );
            }
        );



    };

    return [
        'refreshAlexaCookie': refreshAlexaCookie,
        'generateAlexaCookie': generateAlexaCookie,
        //'addCookies': addCookies, //just for debugging
        //'cookie_parse': cookie_parse //just for debugging
    ].asImmutable();
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