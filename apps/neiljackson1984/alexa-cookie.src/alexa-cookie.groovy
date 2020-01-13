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


def runTheTestCode(){
    startCollectionOfDebugMessage();
    try{
        mainTestCode();
    } catch (e)
    {
        // def debugMessage = ""
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
    }
    stopCollectionOfDebugMessage();
    return respondFromTestCode(debugMessage);
}


def respondFromTestCode(message){
	// log.debug(message);
	// sendEvent( name: 'testEndpointResponse', value: message )
	// return message;
	return  render( contentType: "text/html", data: message, status: 200);
}



def mainTestCode(){

	debugMessage += "\n\n";
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
            // debugMessage += "response.entity.content: " + response.entity.content.getText() + "\n"*2; //throws a "Stream closed" exception
            debugMessage += "response.headers: " + response.headers.dump() + "\n";
            debugMessage += "response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n";
            debugMessage += "alexaCookie.addCookies('', response.headers): " + alexaCookie.addCookies('', response.headers) + "\n";


            debugMessage += "\n";
            debugMessage += "response.getContext().delegate.map: " + "\n" + response.getContext().delegate.map.collect{"\t"*1 + it.key + ": " + it.value.dump()}.join("\n") + "\n"*2;
            debugMessage += "response.getContext().delegate.map.keySet(): " + "\n" + response.getContext().delegate.map.keySet().collect{"\t"*1 + it}.join("\n") + "\n"*2;
            debugMessage += "\n";
            // debugMessage += "response.getContext()['http.request'].getURI(): " + response.getContext()['http.request'].getURI().dump() + "\n";
            // debugMessage += "response.getContext()['http.request'].getTarget(): " + response.getContext()['http.request'].getTarget().dump() + "\n";
            // debugMessage += "response.getContext()['http.request'].getRequestLine(): " + response.getContext()['http.request'].getRequestLine().dump() + "\n";
            // debugMessage += "response.getContext()['http.request'].getRequestLine().getUri(): " + response.getContext()['http.request'].getRequestLine().getUri() + "\n";


            // debugMessage += "response.getContext()['http.request'].getOriginal().getRequestLine(): " + response.getContext()['http.request'].getOriginal().getRequestLine().dump() + "\n";
            // debugMessage += "response.getContext()['http.request'].getOriginal().getRequestLine().getUri(): " + response.getContext()['http.request'].getOriginal().getRequestLine().getUri() + "\n";

            // debugMessage += "response.getContext()['http.target_host'].hostname: " + response.getContext()['http.target_host'].hostname + "\n";

            // debugMessage += "response.data: " + response.data.dump() + "\n";
            // debugMessage += "response.entity: " + response.entity.dump() + "\n"*2;
            // debugMessage += "response.getContext()['http.response']: " + response.getContext()['http.response'].dump() + "\n"*2;
            // debugMessage += "response.getContext()['http.response'].original: " + response.getContext()['http.response'].original.dump() + "\n"*2;
            // debugMessage += "response.getContext()['http.response'].original: " + response.getContext()['http.response'].original.toString() + "\n"*2;
            // debugMessage += "response.entity.content: " + response.entity.content.dump() + "\n"*2;
            // // debugMessage += "response.entity.content: " + response.entity.content.getText() + "\n"*2;
            // // debugMessage += "response.getContext()['http.response'].entity.content: " + response.getContext()['http.response'].entity.content.getText() + "\n"*2;
            // // debugMessage += "response.getContext()['http.response'].original.entity.content: " + response.getContext()['http.response'].original.entity.content.getText() + "\n"*2;
            // debugMessage += "response.getContext()['http.response'].params: " + response.getContext()['http.response'].params.dump() + "\n"*2;
        }
    );

    // a = new hubitat.helper.InterfaceUtils();

    // debugMessage += "hubitat.helper.InterfaceUtils: " + hubitat.helper.InterfaceUtils.dump() + "\n";

    // debugMessage += "hubitat.helper.InterfaceUtils.getMethods(): " + "\n" + hubitat.helper.InterfaceUtils.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n";

    // debugMessage += "hubitat.helper.InterfaceUtils: " + hubitat.helper.InterfaceUtils.dump() + "\n";
    // debugMessage += "hubitat.helper.InterfaceUtils.getMethods(): " + "\n" + hubitat.helper.InterfaceUtils.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n";

    // // debugMessage += "hubitat: " + hubitat.dump() + "\n";
    // // debugMessage += "hubitat.getMethods(): " + "\n" + hubitat.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n";

    // debugMessage += "location.hub: " + location.hub.dump() + "\n";
    // debugMessage += "location.hub.class.getMethods(): " + "\n" + location.hub.class.getMethods().collect{"\t"*1 + it.toString()}.join("\n") + "\n";

    // debugMessage += 'HTTPBuilder: ' + (new HTTPBuilder()).dump();

    alexaCookie.generateAlexaCookie(
        'email':   ,
        'password': ,
        'options':  [
            'logger': {debugMessage += it + "\n";},
            'amazonPage': "amazon.com",
            'acceptLanguage':'en-US',
            'userAgent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36'
        ],
        'callback': {String error, Map result ->
            if(error){
                debugMessage += "error string that resulted from attempting to generate alexa cookie: " + error + "\n";
            }
            if(result){
                debugMessage += "result of attempting to generate alexa cookie: " + prettyPrint(result) + "\n";
            }
        }
    );

   

}


def mainTestCode2(){
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
    //        //debugMessage += "\n"*2;
    //         appendDebugMessage("AlexaCookie().addCookies('', response.headers): " + AlexaCookie().addCookies('', response.headers) + "\n");
    //         appendDebugMessage("response.data: " + response.data.toString() + "\n");
    //     }
    // );

    //   List<Integer> a = [11,22,33];
    // final List<Integer> b = [11,22,33];
    //       List<Integer> c = [11,22,33].asImmutable();
    // final List<Integer> d = [11,22,33].asImmutable();

    // try{a << 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to append to a: " + e + "\n";}
    // try{b << 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to append to b: " + e + "\n";}
    // try{c << 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to append to c: " + e + "\n";}
    // try{d << 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to append to d: " + e + "\n";}


    // try{a[0] = 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to modify a: " + e + "\n";}
    // try{b[0] = 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to modify b: " + e + "\n";}
    // try{c[0] = 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to modify c: " + e + "\n";}
    // try{d[0] = 44; } catch(Exception e) {debugMessage+= "encountered exception when attempting to modify d: " + e + "\n";}

    // try{a[1] = "ahoy"; } catch(Exception e) {debugMessage+= "encountered exception when attempting to assign a string to an alement of a: " + e + "\n";}
    // try{b[1] = "ahoy"; } catch(Exception e) {debugMessage+= "encountered exception when attempting to assign a string to an alement of b: " + e + "\n";}
    // try{c[1] = "ahoy"; } catch(Exception e) {debugMessage+= "encountered exception when attempting to assign a string to an alement of c: " + e + "\n";}
    // try{d[1] = "ahoy"; } catch(Exception e) {debugMessage+= "encountered exception when attempting to assign a string to an alement of d: " + e + "\n";}

    // try{a = [55,66]; } catch(Exception e) {debugMessage+= "encountered exception when attempting to overwrite a: " + e + "\n";}
    // try{b = [55,66]; } catch(Exception e) {debugMessage+= "encountered exception when attempting to overwrite b: " + e + "\n";}
    // try{c = [55,66]; } catch(Exception e) {debugMessage+= "encountered exception when attempting to overwrite c: " + e + "\n";}
    // try{d = [55,66]; } catch(Exception e) {debugMessage+= "encountered exception when attempting to overwrite d: " + e + "\n";}



    // debugMessage += "a.dump(): " + a.dump() + "\n";
    // debugMessage += "a: " + "\n" + a.collect{"\t"*1 + it}.join("\n") + "\n";

    // debugMessage += "b.dump(): " + b.dump() + "\n";
    // debugMessage += "b: " + "\n" + b.collect{"\t"*1 + it}.join("\n") + "\n";

    // debugMessage += "c.dump(): " + c.dump() + "\n";
    // debugMessage += "c: " + "\n" + c.collect{"\t"*1 + it}.join("\n") + "\n";

    // debugMessage += "d.dump(): " + d.dump() + "\n";
    // debugMessage += "d: " + "\n" + d.collect{"\t"*1 + it}.join("\n") + "\n";

    // debugMessage += "(new org.apache.http.protocol.HttpCoreContext()).HTTP_REQUEST: " + (new HttpCoreContext()).HTTP_REQUEST + "\n";
    // debugMessage += "HTTP_REQUEST: " + HTTP_REQUEST + "\n";

//    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
//    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder().uri(URI.create("http://foo.com/")).build();


    httpGet(
        [
            'uri': "https://postman-echo.com/cookies/set?foo1=bar1&foo2=bar2",
            'query': [
                'foo1':'bar1',
                'foo2':'bar2'
            ]
        ],
        { response ->
            debugMessage += "response.headers: " + response.headers.dump() + "\n";
            debugMessage += "response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n";
            debugMessage += "AlexaCookie().addCookies('', response.headers): " + AlexaCookie().addCookies('', response.headers) + "\n";
            debugMessage += "response.data: " + response.data.dump() + "\n";
            debugMessage += "response.getContext(): " + response.getContext().dump() + "\n";
            debugMessage += "\n";
            debugMessage += "prettyPrint(response.getContext()): " + prettyPrint(response.getContext()) + "\n";
            debugMessage += "\n";
            debugMessage += "response.getContext().getClass().getFields(): " + "\n" + response.getContext().delegate.getProperties().collect{"\t"*1 + it.toString()}.join("\n") + "\n"; 
            // debugMessage += "response.getContext(): " + response.getContext().collect{it.toString()}.join("\n") + "\n";
            debugMessage += "\n";
            //see the org.apache.http.protocol.HttpContext and org.apache.http.protocol.HttpCoreContext sections of https://hc.apache.org/httpcomponents-core-4.4.x/httpcore/apidocs/constant-values.html
            // to know which keys are available in the context.
            // debugMessage += "response.getContext()['http.request']: " + response.getContext()['http.request'].dump() + "\n";
            debugMessage += "response.getContext().delegate: " + response.getContext().delegate.dump() + "\n";
            debugMessage += "\n";




            candidateKeys = ["http.", "http.connection","http.request_sent", "http.request", "http.response", "http.target_host"];

            debugMessage += candidateKeys.collect{
                candidateKey ->
                "response.getContext()['${candidateKey}']: " + response.getContext()[candidateKey]?.dump()
            }.join("\n"*2);
 

            debugMessage += "response.getContext().delegate.map: " + response.getContext().delegate.map.dump() + "\n"*2;
            debugMessage += "response.getContext().delegate.map: " + "\n" + response.getContext().delegate.map.collect{"\t"*1 + it.key + ": " + it.value.dump()}.join("\n") + "\n"*2;

            // // debugMessage += "httpGet: " + this.httpGet.dump() + "\n";
            // debugMessage += "this: " + this.dump() + "\n";
            // debugMessage += "this.delegate: " + this.delegate.dump() + "\n";
            // debugMessage += "this.class.getMethods(): " + "\n" +  this.class.getMethods().collect{"\t" + it.toString()}.join("\n") + "\n";

            // def xxx = new HTTPBuilder();


            debugMessage += "response.context['http.request']: " + response.context['http.request'].dump() + "\n"*2;
            debugMessage += "response.context['http.request'].original: " + response.context['http.request'].original.dump() + "\n"*2;
            debugMessage += "response.context['http.request'].original: " + response.context['http.request'].original.toString() + "\n"*2;

            debugMessage += "response.context['http.connection']: " + response.context['http.connection'].dump() + "\n"*2;
            debugMessage += "response.context['http.connection'].class.getMethods(): " + response.context['http.connection'].class.getMethods().collect{"\t" + it.toString()}.join("\n") + "\n"*2;

            // debugMessage += "response.context['http.connection'].getMetrics(): " + response.context['http.connection'].getMetrics().dump() + "\n"*2;
            debugMessage += "response.context['http.connection'].isStale(): " + response.context['http.connection'].isStale().toString() + "\n"*2;
            // debugMessage += "response.context['http.connection'].newProxy(): " + response.context['http.connection'].getMetrics().newProxy() + "\n"*2;
            

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
def getAlexaCookie() {

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
     *  applies any cookies that may be present in 
     *  a set of http headers (an iterable of org.apache.http.Header)  to an existing Cookie string (adding any cookies that
     *  that do not already exist, and updating any that do.)
     *  Returns the updated version of the cookie string.
     */
    Closure addCookies = {String cookie, headers ->
        // if (!headers || !('set-cookie' in headers)){
        if (!headers || !headers.any{it.name.toLowerCase() == "set-cookie"} ){
            appendDebugMessage("could not find a 'set-cookie' header in headers." + "\n");
            return cookie; 
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

        for (def headerValue in headers.findAll{it.name == "set-cookie"}.collect{it.value}){
            // original javascript: cookie = cookie.match(/^([^=]+)=([^;]+);.*/);
            // we expect headerValue to be a string that looks like "foo=blabbedy blabbedy blabbedy ;"
            // appendDebugMessage("headerValue: " + headerValue + "\n");
            cookieMatch = (~/^([^=]+)=([^;]+);.*/).matcher(headerValue)[0];

            //original javascript:  if (cookie && cookie.length === 3) {
            if (cookieMatch && cookieMatch.size() == 3) {
                // appendDebugMessage("cookieMatch: " + cookieMatch[1] + "--" + cookieMatch[2] + "--" + cookieMatch[3] + "\n");
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
        cookie = '';
        for (name in cookies.keySet()){
            cookie += name + '=' + cookies[name] + '; ';
        }

        return cookie;  //>    return Cookie;
    };

    Closure getFields = { String body ->
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
        String csrf = null; //our goal is to obtain a csrf token and assign it to this string. 
        for(csrfPathCandidate in csrfPathCandidates){
            _options.logger('Alexa-Cookie: Step 4: get CSRF via ' + csrfPathCandidate);
            httpGet(
                [
                    'headers': [
                        'DNT': '1',
                        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36',
                        'Connection': 'keep-alive',
                        'Referer': 'https://alexa.' + namedArgs.options.amazonPage + '/spa/index.html',
                        'Cookie': namedArgs.cookie,
                        'Accept': '*/*',
                        'Origin': 'https://alexa.' + namedArgs.options.amazonPage  
                    ]
                ],
                {response ->
                    namedArgs.cookie = addCookies(namedArgs.cookie, response.headers);
                    csrf = (~/csrf=([^;]+)/).matcher(namedArgs.cookie).getAt(0)?.getAt(1);
                    _options.logger('Alexa-Cookie: Result: csrf=' + csrf.toString() + ', Cookie=' + namedArgs.cookie);
                }
            );
            if(csrf){
                namedArgs.callback && namedArgs.callback(null, [
                    'cookie':namedArgs.cookie,
                    'csrf':csrf
                ]);
                return;
            }
        }

        //it seems like we should do something here to handle the case where no csrf could be obtained,
        // but the original javascript does not seem to do any such error handling.
    };

    Closure generateAlexaCookie = {Map namedArgs  ->
        String email     = namedArgs.email;
        String password  = namedArgs.password;
        Map __options    = namedArgs.options ?: [:];
        Closure callback = namedArgs.callback;
        Map requestParams; // many of the requestParams stay the same from one request to the next, so we will keep track of requestParams in this variable, and modify them as needed before each new request.
        if (!email || !password) {__options.proxyOnly = true;}
        _options = __options;
        initConfig();

        if(_options.proxyOnly){
            //TO-DO: start the proxy server (not yet implemented) and instruct the user to go attempt to login using the proxy server.
        } else {
            // comment from the original javascript: get first cookie and write redirection target into referer
            _options.logger('Alexa-Cookie: Step 1: get first cookie and authentication redirect');
            requestParams = [
                'uri': "https://" +  'alexa.' + _options.amazonPage,
                'headers': [
                    'DNT': '1',
                    'Upgrade-Insecure-Requests': '1',
                    'User-Agent': _options.userAgent,
                    'Accept-Language': _options.acceptLanguage,
                    'Connection': 'keep-alive',
                    'Accept': '*/*'
                ],
                'contentType':groovyx.net.http.ContentType.TEXT //this influences the type of object that the system passes to the callback.
            ];
            
            httpGet(requestParams,
                {response0 ->
                    //TO-DO: handle request errors here.
                    Cookie = addCookies(Cookie, response0.headers);
                    String responseText = response0.data.getText();
                    _options.logger("\n" + "response0.getContext()['http.request'].getRequestLine().getUri(): " + response0.getContext()['http.request'].getRequestLine().getUri());
                    _options.logger("\n" + "response0.getContext()['http.request'].getOriginal().getRequestLine().getUri(): " + response0.getContext()['http.request'].getOriginal().getRequestLine().getUri());
                    _options.logger("\n")
                    _options.logger("response0.contentType: " + response0.contentType)
                    // _options.logger("response0.data.toString(): " + response0.data.toString())
                    // _options.logger("\n" + "response0.headers: " + "\n" + response0.headers.collect{"\t"*1 + it.name + ": " + it.value}.join("\n\n") + "\n")
                    _options.logger('Alexa-Cookie: Step 2: login empty to generate session');
                    _options.logger('getFields(responseText): ' + prettyPrint(getFields(responseText)) )
                    return;
                    // _options.logger(prettyPrint(getFields(response0.data.toString())));
                    _options.logger("\n\n" )
                    _options.logger('response0.data: ' + response0.data.dump().take(300) + "\n\n" );
                    // _options.logger('response0.data.parent(): ' + response0.data.parent().dump().take(300)  + "\n\n" );
                    
                    // _options.logger("response0.data.text: " + response0.data.text.take(25) + (response0.data.text().length() > 25 ? "..." : '')); //throws an exception java.io.IOException: Stream closed, becauase we can only read the string once
                    _options.logger("responseText: " + responseText.take(25) + (responseText.length() > 25 ? "..." : ''));

                    // _options.logger("response0.data.toString(): " + response0.data.text.toString());
                    // _options.logger('response0.data.size(): ' + response0.data.size() + "\n\n" );
                    // _options.logger("response0.getParams(): " + response0.getParams().dump());
                    // _options.logger("response0.getContext()['http.response'].getParams(): " + response0.getContext()['http.response'].getParams().dump());
                    // _options.logger("response0.getContext()['http.response'].entity.content: " + response0.getContext()['http.response'].entity.content.getText())
                    // _options.logger("response0.entity.content: " + response0.entity.content.getText())
                    _options.logger("groovyx.net.http.ContentType.TEXT.toString(): " + groovyx.net.http.ContentType.TEXT.toString())
                    requestParams += [
                        'uri': "https://" +  'www.' + _options.amazonPage + '/ap/signin',
                        'body': getFields(responseText),
                        'requestContentType': groovyx.net.http.ContentType.URLENC                     
                    ];
                    requestParams.headers += [
                        'Cookie': Cookie,
                        'Referer': 'https://' + {it.host + it.path}(new java.net.URI(response0.getContext()['http.request'].getOriginal().getRequestLine().getUri())),
                        'Content-Type': 'application/x-www-form-urlencoded'
                    ];

                    _options.logger("we will now attempt to post to " + requestParams.uri + " using " + prettyPrint(requestParams.body));
                    httpPost(requestParams, 
                        {response1 ->
                            //TO-DO: handle request errors here.
                            //comment from the original javascript: // login with filled out form
                            //comment from the original javascript://  !!! referer now contains session in URL
                            Cookie = addCookies(Cookie, response1.headers);
                            _options.logger('Alexa-Cookie: Step 3: login with filled form, referer contains session id');
                            requestParams += [
                                'body': 
                                    getFields(response1.data) + [
                                        'email': email ?: '',
                                        'password': password ?: ''
                                    ]
                            ];
                            requestParams.headers += [
                                'Cookie': Cookie,
                                'Referer': "https://www.${_options.amazonPage}/ap/signin/" + (~/session-id=([^;]+)/).matcher(Cookie)[0][1]
                            ];
                            httpPost(requestParams,
                                {response2 ->
                                    //TO-DO: handle request errors here.
                                    //comment form original javascript: // check whether the login has been successful or exit otherwise
                                    if({it.host.startsWith('alexa') && it.path.endsWith('.html')}(new java.net.URI(response0.getContext()['http.request'].getOriginal().getRequestLine().getUri())) ){
                                        //success
                                        return getCSRFFromCookies('cookie':Cookie, 'options':_options, 'callback':callback);
                                    } else {
                                        String errMessage = 'Login unsuccessfull. Please check credentials.';
                                        java.util.regex.Matcher amazonMessageMatcher = (~/auth-warning-message-box[\S\s]*"a-alert-heading">([^<]*)[\S\s]*<li><[^>]*>\s*([^<\n]*)\s*</).matcher(response2.data);
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

    return [
        'refreshAlexaCookie': refreshAlexaCookie,
        'generateAlexaCookie': generateAlexaCookie,
        'addCookies': addCookies //just for debugging
    ];
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