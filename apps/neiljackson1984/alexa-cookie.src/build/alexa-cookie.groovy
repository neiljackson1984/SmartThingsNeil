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
    if (state.enableCollectionOfDebugMessage){
        state.debugMessage = x;
    }
    return x;
}
def startCollectionOfDebugMessage(){
    state.enableCollectionOfDebugMessage = 1;
    setDebugMessage("");
}
def stopCollectionOfDebugMessage(){
    state['enableCollectionOfDebugMessage'] = 0;
}
def appendDebugMessage(x){
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
        internalDebugMessage += (
            "\n\n" + "================================================" + "\n" +
            (new Date()).format("yyyy/MM/dd HH:mm:ss.SSS", location.getTimeZone()) + "\n" +
            "encountered an exception: \n${e}\n"
        );
        try{
            def stackTraceItems = [];
            def stackTrace;
            try{ stackTrace = e.getStackTrace();} catch(java.lang.SecurityException e1) {
                stackTrace = e1.getStackTrace();
            }
            for(item in stackTrace){stackTraceItems << item; }
            def filteredStackTrace = stackTraceItems.findAll{ it['fileName']?.startsWith("user_") };
   if(!filteredStackTrace.isEmpty()){
    filteredStackTrace = filteredStackTrace.init();
   }
            filteredStackTrace.each{internalDebugMessage += (" @line " + it['lineNumber'] + " (" + it['methodName'] + ")" + "\n"); }
        } catch(ee){
            internalDebugMessage += ("encountered an exception while trying to investigate the stack trace: \n${ee}\n");
            internalDebugMessage += ("ee.getStackTrace(): " + ee.getStackTrace() + "\n");
        }
        internalDebugMessage += "\n";
        appendDebugMessage(internalDebugMessage);
    }
    stopCollectionOfDebugMessage();
    return respondFromTestCode(debugMessage);
}
def respondFromTestCode(message){
    log.debug("debugMessage: " + "\n" + "<code>" + groovy.xml.XmlUtil.escapeXml(message) + "</code>");
 return render(contentType: "text/html", data: message, status: 200);
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
            }
        }
    );
}
def handleNewAlexaRefreshOptions(String newAlexaRefreshOptions){
    sendEvent(
        name: "alexaRefreshOptions",
        value: newAlexaRefreshOptions,
        data: [alexaRefreshOptions: new groovy.json.JsonSlurper().parseText(newAlexaRefreshOptions)]
    );
    state.alexaRefreshOptions = newAlexaRefreshOptions;
    state.alexaCookie = getCookieFromOptions(state.alexaRefreshOptions);
}
def mainTestCode5(){
    alexaCookie.generateAlexaCookie(
        'email': settings.amazon_username,
        'password': settings.amazon_password,
        'options': [
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
            appendDebugMessage("response.headers: " + response.headers.dump() + "\n");
            appendDebugMessage("response.headers: " + "\n" + response.headers.collect{"\t"*1 + it.getName() + ": " + it.getValue()}.join("\n") + "\n");
            appendDebugMessage("alexaCookie.addCookies('', response.headers): " + alexaCookie.addCookies('', response.headers) + "\n");
            appendDebugMessage("\n");
            appendDebugMessage("response.getContext().delegate.map: " + "\n" + response.getContext().delegate.map.collect{"\t"*1 + it.key + ": " + it.value.dump()}.join("\n") + "\n"*2);
            appendDebugMessage("response.getContext().delegate.map.keySet(): " + "\n" + response.getContext().delegate.map.keySet().collect{"\t"*1 + it}.join("\n") + "\n"*2);
            appendDebugMessage("\n");
        }
    );
    alexaCookie.generateAlexaCookie(
        'email': settings.amazon_username,
        'password': settings.amazon_password,
        'options': [
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
}
def mainTestCode2(){
    startCollectionOfDebugMessage();
 appendDebugMessage("\n\n");
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
            appendDebugMessage("\n");
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
            appendDebugMessage("response.context['http.request']: " + response.context['http.request'].dump() + "\n"*2);
            appendDebugMessage("response.context['http.request'].original: " + response.context['http.request'].original.dump() + "\n"*2);
            appendDebugMessage("response.context['http.request'].original: " + response.context['http.request'].original.toString() + "\n"*2);
            appendDebugMessage("response.context['http.connection']: " + response.context['http.connection'].dump() + "\n"*2);
            appendDebugMessage("response.context['http.connection'].class.getMethods(): " + response.context['http.connection'].class.getMethods().collect{"\t" + it.toString()}.join("\n") + "\n"*2);
            appendDebugMessage("response.context['http.connection'].isStale(): " + response.context['http.connection'].isStale().toString() + "\n"*2);
        }
    );
    stopCollectionOfDebugMessage();
   return respondFromTestCode(debugMessage);
}
def mainPage() {
 def myDate = new Date();
    def myDateFormat = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    myDateFormat.setTimeZone(location.timeZone);
    dynamicPage(
     name: "mainPage",
        title: "",
        install: true,
        uninstall: true
    ) {
     section( ) {
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
 unsubscribe()
 initialize()
}
def initialize() {
    String hashOfManuallyEnteredAlexaRefreshOptions = sha1(settings.manuallyEnteredAlexaRefreshOptions);
 if(state.hashOfLastManuallyEnteredAlexaRefreshOptions != hashOfManuallyEnteredAlexaRefreshOptions){
        state.hashOfLastManuallyEnteredAlexaRefreshOptions = hashOfManuallyEnteredAlexaRefreshOptions;
        handleNewAlexaRefreshOptions(settings.manuallyEnteredAlexaRefreshOptions);
        state.timeOfLastManualEntryOfAlexaRefreshOptions = now();
    }
}
private Map getAlexaCookie() {
Map alexaCookieUtility = {
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
    Closure prettyPrint = {
            return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(it));
    };
    Closure cookie_parse = {String str, Map options=[:] ->
        if (! str instanceof String ) {
            throw new Exception("argument str must be a string");
        }
        if(!str){str='';}
        def pairSplitRegExp = "; *";
        def obj = [:];
        def opt = options + [:];
        def pairs = str.split(pairSplitRegExp);
        for (def i = 0; i < pairs.size(); i++) {
            def pair = pairs[i];
            def eq_idx = pair.indexOf('=');
            if (eq_idx < 0) {
                continue;
            }
            def key = pair.substring(0, eq_idx).trim();
            def val = pair.substring(++eq_idx, pair.length()).trim();
            if ('"' == val[0]) {
                val = val[1..-2];
            }
            if (! obj.containsKey(key)) {
                obj[key] = URLDecoder.decode(val);
            }
        }
        return obj;
    };
    Closure addCookies = {String cookie, headers ->
        String internalDebugMessage = "";
        internalDebugMessage += "addCookies run summary:" + "\n";
        internalDebugMessage += "starting with: " + cookie + "\n";
        String returnValue;
        if (!headers || !headers.any{it.name.toLowerCase() == "set-cookie"} ){
            internalDebugMessage += ("could not find a 'set-cookie' header in headers." + "\n");
            returnValue = cookie;
        } else {
            if(!cookie){
                cookie='';
            }
            def cookies = cookie_parse(cookie);
            for (def headerValue in headers.findAll{it.name.toLowerCase() == "set-cookie"}.collect{it.value}){
                cookieMatch = (~/^([^=]+)=([^;]+);.*/).matcher(headerValue)[0];
                if (cookieMatch && cookieMatch.size() == 3) {
                    if (cookieMatch[1] == 'ap-fid' && cookieMatch[2] == '""'){ continue;}
                    if( (cookieMatch[1] in cookies) && (cookies[cookieMatch[1]] != cookieMatch[2]) ){
                        internalDebugMessage += ('Alexa-Cookie: Update Cookie ' + cookieMatch[1] + ' = ' + cookieMatch[2]) + "\n";
                    } else if (!(cookieMatch[1] in cookies) ) {
                        internalDebugMessage += ('Alexa-Cookie: Add Cookie ' + cookieMatch[1] + ' = ' + cookieMatch[2]) + "\n";
                    } else {
                    }
                    cookies[cookieMatch[1]] = cookieMatch[2];
                }
            }
            returnValue = cookies.collect{it.key + "=" + it.value}.join("; ");
        }
        internalDebugMessage += "addCookies is returning: " + returnValue + "\n";
        _options.logger && _options.logger(internalDebugMessage);
        return returnValue;
    };
    Closure getFields = {String body ->
        Map returnValue = [:];
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
        _options.logger = _options.logger ?: Closure.IDENTITY;
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
    Closure getCSRFFromCookies = {Map namedArgs ->
        String cookie = namedArgs.cookie;
        Map options = namedArgs.options ?: [:];
        Closure callback = namedArgs.callback;
        String csrf = null;
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
    };
    Closure getLocalCookies = {Map namedArgs ->
        String amazonPage = namedArgs.amazonPage;
        String refreshToken = namedArgs.refreshToken;
        Closure callback = namedArgs.callback;
        Cookie = '';
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
            contentType: groovyx.net.http.ContentType.JSON,
            requestContentType: groovyx.net.http.ContentType.URLENC,
            body: exchangeParams
        ];
        _options.logger('Alexa-Cookie: Exchange tokens for ' + amazonPage);
        _options.logger(prettyPrint(requestParams));
        httpPost(requestParams,
            {response ->
                _options.logger('Exchange Token Response: ' + prettyPrint(response.data));
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
        Map registerData = [
            "requested_extensions": [
                "device_info",
                "customer_info"
            ],
            "cookies": [
                "website_cookies": cookies.collect{ ["Value": it.value, "Name": it.key] },
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
            contentType: groovyx.net.http.ContentType.JSON,
            requestContentType: groovyx.net.http.ContentType.JSON,
            body: registerData
        ];
        options.logger && options.logger('Alexa-Cookie: Register App');
        options.logger && options.logger(prettyPrint(requestParams0));
        httpPost(requestParams0,
            {response0 ->
                options.logger && options.logger('Register App Response: ' + prettyPrint(response0.data));
                if(! response0.data.response?.success?.tokens?.bearer){
                    callback && callback('No tokens in Register response', null);
                    return;
                }
                Cookie = addCookies(Cookie, response0.headers);
                loginData.refreshToken = response0.data.response.success.tokens.bearer.refresh_token;
                loginData.tokenDate = now();
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
                    contentType: groovyx.net.http.ContentType.JSON,
                ];
                options.logger && options.logger('Alexa-Cookie: Get User data');
                options.logger && options.logger(prettyPrint(requestParams1));
                httpGet(requestParams1,
                    {response1 ->
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
    Closure generateAlexaCookie = {Map namedArgs ->
        String email = namedArgs.email;
        String password = namedArgs.password;
        Map __options = namedArgs.options ?: [:];
        Closure callback = namedArgs.callback;
        if (!email || !password) {__options.proxyOnly = true;}
        _options = __options;
        initConfig();
        if(_options.proxyOnly){
        } else {
            _options.logger('Alexa-Cookie: Step 1: get first cookie and authentication redirect');
            Map requestParams0 = [
                'uri': "https://" + 'alexa.' + _options.amazonPage,
                'headers': [
                    'DNT': '1',
                    'Upgrade-Insecure-Requests': '1',
                    'User-Agent': _options.userAgent,
                    'Accept-Language': _options.acceptLanguage,
                    'Connection': 'keep-alive',
                    'Accept': '*/*'
                ],
                'contentType':groovyx.net.http.ContentType.TEXT
            ];
            _options.logger("working on step 1, we will now attempt to get " + requestParams0.uri);
            httpGet(requestParams0,
                {response0 ->
                    _options.logger('First response has been received.');
                    _options.logger("\n" + "response0.headers: " + "\n" + response0.headers.collect{"\t"*1 + it.name + ": " + it.value}.join("\n\n") + "\n");
                    _options.logger('Alexa-Cookie: Step 2: login empty to generate session');
                    Cookie = addCookies(Cookie, response0.headers);
                    String response0Text = response0.data.getText();
                    _options.logger("Cookie: " + Cookie);
                    _options.logger("cookie_parse(Cookie): " + prettyPrint(cookie_parse(Cookie)));
                    _options.logger("\n" + "response0.getContext()['http.request'].getRequestLine().getUri(): " + response0.getContext()['http.request'].getRequestLine().getUri());
                    _options.logger("\n" + "response0.getContext()['http.request'].getOriginal().getRequestLine().getUri(): " + response0.getContext()['http.request'].getOriginal().getRequestLine().getUri() + "\n");
                    _options.logger("response0.contentType: " + response0.contentType);
                    _options.logger("response0Text.length(): " + response0Text.length());
                    Map response0Fields = getFields(response0Text);
                    _options.logger('response0Fields: ' + prettyPrint(response0Fields) + "\n\n" )
                    requestParams1 = requestParams0 + [
                        'uri': "https://" + 'www.' + _options.amazonPage + '/ap/signin',
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
                                    String response2Text = response2.data.getText();
                                    _options.logger("response2.contentType: " + response2.contentType);
                                    _options.logger("response2Text.length(): " + response2Text.length());
                                    if({it.host.startsWith('alexa') && it.path.endsWith('.html')}(new java.net.URI(response0.getContext()['http.request'].getOriginal().getRequestLine().getUri())) ){
                                        return getCSRFFromCookies('cookie':Cookie, 'options':_options, 'callback':callback);
                                    } else {
                                        String errMessage = 'Login unsuccessfull. Please check credentials.';
                                        java.util.regex.Matcher amazonMessageMatcher = (~/auth-warning-message-box[\S\s]*"a-alert-heading">([^<]*)[\S\s]*<li><[^>]*>\s*([^<\n]*)\s*</).matcher(response2Text);
                                        if(amazonMessageMatcher.find()){
                                            errMessage = "Amazon-Login-Error: ${amazonMessageMatcher.group(1)}: ${amazonMessageMatcher.group(2)}";
                                        }
                                        if (_options.setupProxy) {
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
        if(!(options.formerRegistrationData?.loginCookie && options.formerRegistrationData?.refreshToken )){
            callback && callback('No former registration data provided for Cookie Refresh', null);
            return;
        }
        _options = options;
        _options.proxyOnly = true;
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
            contentType: groovyx.net.http.ContentType.JSON,
            requestContentType: groovyx.net.http.ContentType.URLENC,
            body: refreshData
        ];
        _options.logger('Alexa-Cookie: Refresh Token');
        _options.logger(prettyPrint(requestParams));
        httpPost(requestParams,
            {response ->
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
                        }
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
    ].asImmutable();
}();
    return alexaCookieUtility;
}
private String sha1(String x){
    return java.security.MessageDigest.getInstance('SHA-1').digest(x.getBytes()).encodeHex().toString();
}
private String getCookieFromOptions(options) {
    try{
        def cookie = new groovy.json.JsonSlurper().parseText(options);
        if (!cookie || cookie == "") {
            log.error("'getCookieFromOptions()': wrong options format!");
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
        return "";
    }
}
