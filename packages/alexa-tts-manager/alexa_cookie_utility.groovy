//This file is intended to be included into a hubitat app source code file.

def newAlexaCookieUtility(Map namedArgs1) {
    
    Closure     logger             
    Map         _alexaCredential 
    // alexaCredential would be more aptly named "serializableState".  This is
    // the store of state that we are in the business of curating, updating, and
    // maintaining.  This is the data structure that all the callbacks are
    // passing around to one another.
    
    //default logger is to do nothing (I choose to use Closure.IDENTITY
    //instead of a literal closure expression here in the hopes that
    //Closure.IDENTITY will incur less runtime overhead.)

    // CONSTANTS: 
    final List<String> csrfPathCandidates = [
        '/api/language',
        '/spa/index.html',
        '/api/devices-v2/device?cached=false',
        '/templates/oobe/d-device-pick.handlebars',
        '/api/strings'
    ].asImmutable();
    //Groovy does not respect my final, nor my "<String>" type specification,
    //but they are my intent nonetheless.

    
    Closure normalizedAlexaCredential = { Map inputAlexaCredential ->
        
        // really I mean nullable Map.
        //returns a new map that is formed by augmenting inputAlexaCredential
        // with any default values that might be missing. 
        // 
        // This function is
        // roughly analogous to a constructor for our "AlexaCredential class" (at
        // least that is how I am thinking about what is going on)
        final Map defaults = [
            baseAmazonPageHandle            : '',
            baseAmazonPage                  : "amazon.com",
            language                        : "en_US",
            apiCallVersion                  : '2.2.485407.0',
            apiCallUserAgent                :  "AmazonWebView/Amazon Alexa/2.2.485407.0/iOS/15.5/iPhone",
            userAgent                       : 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36',
            appName                         : 'ioBroker Alexa2',
            deviceIdSuffix                  : '23413249564c5635564d32573831',
            officialUserAgent               : 'AppleWebKit PitanguiBridge/2.2.483723.0-[HARDWARE=iPhone10_4][SOFTWARE=15.5][DEVICE=iPhone]',
        ]

        //  (?<!_alexaCredential\.)(baseAmazonPageHandle|baseAmazonPage|language|apiCallVersion|apiCallUserAgent|userAgent|appName|deviceIdSuffix|officialUserAgent|loginCookie|localCookie)
        // ==> _alexaCredential.$1
        // 
        return defaults + (inputAlexaCredential ?: [:])

        // possibly, some of these parameters could be stored as static constants. 

    }

    Closure construct = {Map namedArgs ->
        logger              = namedArgs?.logger              ?: Closure.IDENTITY   
        _alexaCredential    = normalizedAlexaCredential(namedArgs.alexaCredential)
    }

    Closure  getAlexaCredential = {
        return _alexaCredential
    }
    
    // Map         alexaCredential     = normalizedAlexaCredential(namedArgs1?.alexaCredential) ;
    Closure randomBytes = {int count ->
        byte[] buffer = new byte[count];
        new java.util.Random().nextBytes(buffer);
        return buffer;
    }

    // Closure base64Encode = { byte[] buffer ->
    Closure base64Encode = { buffer ->
        return buffer.encodeBase64().toString();
    }

    // Closure base64UrlEncode = { byte[] buffer ->
    Closure base64UrlEncode = { buffer ->
        base64Encode(buffer).replace('+','-').replace('/','_').replace('=','')
    }

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
    * @return {object}
    * @public
    */
    Closure cookie_parse = {String str ->
        if (! str instanceof String ) {
            throw new Exception("argument str must be a string");
        }
        if(!str){str='';}

        //these were originally global variables:
        def pairSplitRegExp = "; *";
        // def decode = decodeURIComponent;
        // def encode = encodeURIComponent;

        def obj = [:];
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

    Closure uriToQueryMap = {String uriString ->
        // takes a uri (a string) as an argument.  Returns a map
        // that represents the query part of the uri.
        // only one value (typically the last, but this is not guaranteed) for any given key is represented in the map.
        // EXAMPLE:
        // urlToQueryMap("https://foo.com:8888/a/b/c?x=0&y=3&z=blarg%20yarg&y=3")
        // returns [ 'x': '0', 'y':'3', 'z': 'blarg yarg' ]
        java.net.URI uri = new java.net.URI(uriString)
        String rawQuery = uri.getRawQuery()
        return rawQuery.split("&").collectEntries{
            x=it.split("=",2)
            [
                (java.net.URLDecoder.decode(x[0])): 
                    java.net.URLDecoder.decode(x.length > 1 ? x[1] : "")
            ]
        }
    }

    /**
    *  applies any cookies that may be present in a set of http headers (an
    *  iterable of org.apache.http.Header)  to an existing Cookie string (adding
    *  any cookies that that do not already exist, and updating any that do.)
    *  Returns the updated version of the cookie string.
    */
    Closure addCookies = {String cookiesString, headers ->
        String internalDebugMessage = "";
        internalDebugMessage += "addCookies run summary:" + "\n";
        internalDebugMessage += "starting with: " + cookiesString + "\n";
        String returnValue;
        // if (!headers || !('set-cookie' in headers)){
        if (!headers || !headers.any{it.name.toLowerCase() == "set-cookie"} ){
            internalDebugMessage += ("could not find a 'set-cookie' header in headers." + "\n");
            returnValue =  cookiesString; 
        } else {
            if(!cookiesString){
                cookiesString='';
            }   

            // original javascript:   
            //      const cookies = cookieTools.parse(Cookie);
            def cookies = cookie_parse(cookiesString); 

            

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
        logger(internalDebugMessage);
        return returnValue;
    };


    Closure augmentCookieJar = {Map namedArgs ->
        String cookieJar = namedArgs.cookieJar ?: ''
        def headers = namedArgs.headers
        Map registrationResponse = namedArgs.registrationResponse

        if(headers){
            String internalDebugMessage = "";
            internalDebugMessage += "augmentCookieJar (from headers) run summary:" + "\n";
            internalDebugMessage += "starting with: " + cookieJar + "\n";
            // if (!headers || !('set-cookie' in headers)){
            if (!headers || !headers.any{it.name.toLowerCase() == "set-cookie"} ){
                internalDebugMessage += ("could not find a 'set-cookie' header in headers." + "\n");
            } else {
                // original javascript:   
                //      const cookies = cookieTools.parse(Cookie);
                def cookies = cookie_parse(cookieJar); 

                

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
                cookieJar = cookies.collect{it.key + "=" + it.value}.join("; ");
            }
            internalDebugMessage += "augmentCookieJar (from headers) has finished, and cookieJar is now: ${cookieJar}\n";
            logger(internalDebugMessage);
        }

        if(registrationResponse){
            (
                registrationResponse?.response?.success?.tokens?.website_cookies ?: []
            ).collect
        }
        // 2023-01-08-2045  START HERE
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================
        //===============================================================

        return cookieJar
    }


    Closure getCSRF = {Map namedArgs  -> 
        // String cookiesString = namedArgs.cookiesString;
        // Map _alexaCredential = normalizedAlexaCredential(namedArgs.alexaCredential)
        // Map _alexaCredential = namedArgs.alexaCredential
        Closure callback = namedArgs.callback
        
        _alexaCredential.csrf = null; //our goal is to obtain a csrf token and assign it to this string. 
        for(csrfPathCandidate in csrfPathCandidates){
            logger('Alexa-Cookie: Step 4: get CSRF via ' + csrfPathCandidate);
            httpGet(
                [
                    uri: "https://alexa." + _alexaCredential.baseAmazonPage + csrfPathCandidate,
                    'headers': [
                        'DNT': '1',
                        'User-Agent': _alexaCredential.userAgent,
                        'Connection': 'keep-alive',
                        'Referer': 'https://alexa.' + _alexaCredential.baseAmazonPage + '/spa/index.html',
                        'Cookie': _alexaCredential.localCookie,
                        'Accept': '*/*',
                        'Origin': 'https://alexa.' + _alexaCredential.baseAmazonPage 
                    ]
                ],
                {response ->
                    _alexaCredential.localCookie = addCookies(_alexaCredential.localCookie, response.headers);
                    java.util.regex.Matcher csrfMatcher = (~/csrf=([^;]+)/).matcher(_alexaCredential.localCookie);
                    if(csrfMatcher.find()){
                        _alexaCredential.csrf = csrfMatcher.group(1);
                        logger('Alexa-Cookie: Result: _alexaCredential.csrf=' + _alexaCredential.csrf.toString() + ', _alexaCredential.localCookie=' + _alexaCredential.localCookie);
                    }
                }
            );
            if(_alexaCredential.csrf){
                callback && callback(null, null);
                return;
            }
        }

        //it seems like we should do something here to handle the case where no csrf could be obtained,
        // but the original javascript does not seem to do any such error handling.
    };

    Closure getLocalCookies = {Map namedArgs ->
        // String amazonPage = namedArgs.amazonPage;
        // String refreshToken = namedArgs.refreshToken;
        // Map _alexaCredential = normalizedAlexaCredential(namedArgs.alexaCredential)
        Closure callback = namedArgs.callback;

        Cookie = ''; //comment from original javascript: reset because we are switching domains
        //comment from original javascript: Token Exchange to Amazon Country Page
        Map exchangeParams = [
            'di.os.name': 'iOS',
            'app_version': _alexaCredential.apiCallVersion,
            'domain': '.' + _alexaCredential.amazonPage,
            'source_token': _alexaCredential.refreshToken,
            'requested_token_type': 'auth_cookies',
            'source_token_type': 'refresh_token',
            'di.hw.version': 'iPhone',
            'di.sdk.version': '6.10.0',
            // 'cookies': ('{„cookies“:{".' + _alexaCredential.amazonPage + '":[]}}').bytes.encodeBase64().toString(),
            // this appears to be unnecessary.
            'app_name': 'Amazon Alexa',
            //'app_name': _alexaCredential.appName,
            'di.os.version': '11.4.1'
        ];
        Map requestParams = [
            uri: 'https://' + 'www.' + _alexaCredential.amazonPage + '/ap/exchangetoken',
            headers: [
                'User-Agent': _alexaCredential.userAgent,
                'Accept-Language': _alexaCredential.language,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': '*/*'
            ],
            contentType: groovyx.net.http.ContentType.JSON, 
            // type of content that we expect the response to contain //this
            // influences the type of object that the system passes to the
            // callback. ,
            
            requestContentType: groovyx.net.http.ContentType.URLENC, 
            //type of content that the request will contain.  corresponds to the
            //'Content-Type' header of the request. By default, this is assumed
            //to be the same as the expected content type of the response,
            //unless explicitly specified //this influences how the system
            //treats the body of the request.   
            body: exchangeParams 
        ];
        logger('Alexa-Cookie: Exchange tokens for ' + _alexaCredential.amazonPage);
        logger(prettyPrint(requestParams));
        httpPost(requestParams,
            {response ->
                //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                //TODO: handle malformed response data here.
                logger('Exchange Token Response: ' + prettyPrint(response.data));
                // if (!body.response || !body.response.tokens || !body.response.tokens.cookies) {
                if (!response.data.response?.tokens?.cookies) {
                    callback && callback('No cookies in Exchange response', null);
                    return;
                }
                if (!response.data.response.tokens.cookies['.' + _alexaCredential.amazonPage]) {
                    callback && callback('No cookies for ' + _alexaCredential.amazonPage + ' in Exchange response', null);
                    return;
                }

                Cookie = addCookies(Cookie, response.headers);
                Map cookies = cookie_parse(Cookie);
                response.data.response.tokens.cookies['.' + _alexaCredential.amazonPage].each {cookie ->
                    if (cookies[cookie.Name] && cookies[cookie.Name] != cookie.Value) {
                        logger('Alexa-Cookie: Update Cookie ' + cookie.Name + ' = ' + cookie.Value);
                    } else if (!cookies[cookie.Name]) {
                        logger('Alexa-Cookie: Add Cookie ' + cookie.Name + ' = ' + cookie.Value);
                    }
                    cookies[cookie.Name] = cookie.Value;
                };

                // String localCookie = '';
                // for (String name in cookies.keySet()) {
                //     localCookie += name + '=' + cookies[name] + '; ';
                // }
                // localCookie = localCookie.replace(/[; ]*$/, '');

                _alexaCredential.localCookie = cookies.collect{it.key + "=" + it.value}.join("; ");
                callback && callback(null, null);
            }
        );
    };

    Closure handleTokenRegistration = {Map namedArgs ->
        Closure callback = namedArgs.callback;
        // Map _alexaCredential = normalizedAlexaCredential(namedArgs.alexaCredential);

        logger('Handle token registration Start: ' + prettyPrint(_alexaCredential));


        
        // workingCookiesString = _alexaCredential.loginCookie;

        //comment from original javascript: Register App
        Map registerData = [
            "requested_extensions": [
                "device_info",
                "customer_info"
            ],
            "cookies": [
                "website_cookies": cookie_parse(_alexaCredential.loginCookie).collect{ ["Value": it.value,  "Name": it.key] },
                "domain": ".amazon.com"
            ],
            "registration_data": [
                "domain": "Device",
                "app_version": _alexaCredential.apiCallVersion,
                "device_type": "A2IVLV5VM2W81",
                "device_name": "%FIRST_NAME%\u0027s%DUPE_STRATEGY_1ST%ioBroker Alexa2",
                "os_version": "11.4.1",
                "device_serial": _alexaCredential.deviceSerial,
                "device_model": "iPhone",
                "app_name": _alexaCredential.appName,
                "software_version": "1"
            ],
            "auth_data": (
                _alexaCredential.accessToken ? 
                (
                    [
                        "access_token": _alexaCredential.accessToken
                    ]
                ) : (
                    [
                        'client_id'             : _alexaCredential.deviceId,
                        'authorization_code'    : _alexaCredential.authorizationCode,
                        'code_verifier'         : _alexaCredential.code_verifier,
                        'code_algorithm'        : 'SHA-256',
                        'client_domain'         : 'DeviceLegacy'  
                    ]
                )
            ),
            "user_context_map": [
                "frc": _alexaCredential.frc
            ],
            "requested_token_type": [
                "bearer",
                "mac_dms",
                "website_cookies"
            ]
        ];

        Map requestParams0 = [
            uri: "https://api.${_alexaCredential.baseAmazonPage}/auth/register",
            headers: [
                'User-Agent': _alexaCredential.apiCallUserAgent,
                'Accept-Language': _alexaCredential.language,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'Cookie': _alexaCredential.loginCookie,
                'Accept': '*/*',
                'x-amzn-identity-auth-domain': "api.${_alexaCredential.baseAmazonPage}"
            ],
            contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
            requestContentType: groovyx.net.http.ContentType.JSON,  //this influences how the system treats the body of the request.   
            body: registerData
        ];
        logger('Alexa-Cookie: Register App');
        logger(prettyPrint(requestParams0));
        httpPost(requestParams0,
            {response0 ->
                //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                //TODO: handle malformed response data here.

                logger('Register App Response: ' + prettyPrint(response0.data));

                if(! response0.data.response?.success?.tokens?.bearer){
                    callback && callback('No tokens in Register response', null);
                    return;
                }

                _alexaCredential.registrationResponse = response0.data
                _alexaCredential.timeOfRegistrationResponse = now()

                //   /\   /\   /\
                //   THESE THINGS ARE MUTUALLY REDUNDANT.
                //   TODO: prefer the above ("registrationResponse") to the below. 
                //   \/   \/   \/

                _alexaCredential.refreshToken = response0.data.response.success.tokens.bearer.refresh_token;
                _alexaCredential.tokenDate = now();

                
                //_alexaCredential.loginCookie = addCookies(_alexaCredential.loginCookie, response0.headers);
                _alexaCredential.loginCookie = augmentCookieJar(
                    cookieJar: _alexaCredential.loginCookie,
                    headers: response0.headers
                )

                _alexaCredential.loginCookie = augmentCookieJar(
                    cookieJar: _alexaCredential.loginCookie,
                    registrationResponse: _alexaCredential.registrationResponse
                )

                //comment from original javascript: Get Amazon Marketplace Country
                Map requestParams1 = [
                    uri: "https://alexa.${_alexaCredential.baseAmazonPage}/api/users/me?platform=ios&version=${_alexaCredential.apiCallVersion}",
                    headers: [
                        'User-Agent': _alexaCredential.apiCallUserAgent,
                        'Accept-Language': _alexaCredential.language,
                        'Accept-Charset': 'utf-8',
                        'Connection': 'keep-alive',
                        'Accept': 'application/json',
                        'Cookie': _alexaCredential.loginCookie
                    ],
                    contentType: groovyx.net.http.ContentType.JSON
                    //this influences the type of object that the system passes to the callback. ,
                ];
                logger('Alexa-Cookie: Get User data');
                logger(prettyPrint(requestParams1));
                httpGet(requestParams1,
                    {response1 -> 
                        //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                        logger('Get User data Response: ' + prettyPrint(response1.data));
                        _alexaCredential.loginCookie = addCookies(_alexaCredential.loginCookie, response1.headers);
                        if (response1.data.marketPlaceDomainName) {
                            java.util.regex.Matcher amazonPageMatcher = (~/^[^\.]*\.([\S\s]*)$/).matcher(response1.data.marketPlaceDomainName);
                            if(amazonPageMatcher.find()){
                                _alexaCredential.amazonPage = amazonPageMatcher.group(1);
                            }
                        }
                        getLocalCookies(
                            // alexaCredential: _alexaCredential,
                            callback: {String err0, String result1 ->
                                if (err0) {
                                    callback && callback(err0, null);
                                }
                                // Map _alexaCredential = normalizedAlexaCredential(alexaCredential)
                                getCSRF(
                                    // cookiesString: alexaCredential.localCookie, 
                                    // alexaCredential: alexaCredential,
                                    callback: {String err1, String result2 ->
                                        if (err1) {
                                            callback && callback('Error getting csrf for ' + _alexaCredential.amazonPage + ':' + err1, null);
                                            return;
                                        }
                                        // Map _alexaCredential = normalizedAlexaCredential(alexaCredential)
                                        // alexaCredential.removeAll{key, value -> key == 'accessToken'};
                                        _alexaCredential.remove('accessToken');
                                        _alexaCredential.remove('authorizationCode');
                                        logger('Final Registraton Result: ' + prettyPrint(_alexaCredential));
                                        callback && callback(null, null);
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
    Closure initiateOauth = {Map namedArgs ->
        Closure requestResponseFromUser = namedArgs.requestResponseFromUser
       


        _alexaCredential.deviceSerial    = _alexaCredential.deviceSerial ?: randomBytes(16).encodeHex().toString();
        _alexaCredential.deviceId        = _alexaCredential.deviceId ?: ( _alexaCredential.deviceSerial + _alexaCredential.deviceIdSuffix );
        _alexaCredential.frc             = _alexaCredential.frc ?: base64Encode(randomBytes(313));
        _alexaCredential.mapMd           = _alexaCredential.mapMd ?: (
                groovy.json.JsonOutput.toJson(
                    [
                        "device_user_dictionary":[],
                        "device_registration_data":[
                            "software_version":"1"
                        ],
                        "app_identifier":[
                            "app_version": _alexaCredential.apiCallVersion,
                            "bundle_id":"com.amazon.echo"
                        ]
                    ]
                ).bytes.encodeBase64().toString()
            ) 

        _alexaCredential.code_verifier   = base64UrlEncode(randomBytes(32))

        signInUrl = (""
            + "https://www.${_alexaCredential.baseAmazonPage}/ap/signin" 

            // # I would like to find a url that would not require sign-in if the user was already logged in.
            // # f"https://www.{self.baseAmazonPage}/ap" 
            // # f"https://www.{self.baseAmazonPage}/ap/oa" 
            // # f"https://www.{self.baseAmazonPage}"
            // #f"https://www.{self.baseAmazonPage}/ap/maplanding"
            // #
            // # the trick, it seems, is to omit the openid.pape.max_auth_age parameter.  

            + "?" + ([
                'openid.return_to'                  : "https://www.${_alexaCredential.baseAmazonPage}/ap/maplanding",
                //# 'openid.return_to'                  : f"https://localhost", # doesn't work
                'openid.assoc_handle'               : "amzn_dp_project_dee_ios${_alexaCredential.baseAmazonPageHandle}",
                'openid.identity'                   : "http://specs.openid.net/auth/2.0/identifier_select",
                'pageId'                            : "amzn_dp_project_dee_ios${_alexaCredential.baseAmazonPageHandle}",
                'accountStatusPolicy'               : "P1",
                'openid.claimed_id'                 : "http://specs.openid.net/auth/2.0/identifier_select",
                //# 'openid.mode'                       : "setup",
                //# 'openid.mode'                       : "id_res",
                'openid.mode'                       : "checkid_setup",
                //# 'openid.mode'                       : "checkid_immediate",
                'openid.ns.oa2'                     : "http://www.${_alexaCredential.baseAmazonPage}/ap/ext/oauth/2",
                'openid.oa2.client_id'              : "device:${_alexaCredential.deviceId}",
                'openid.ns.pape'                    : "http://specs.openid.net/extensions/pape/1.0",
                'openid.oa2.response_type'          : "code",
                'openid.ns'                         : "http://specs.openid.net/auth/2.0",
                //# 'openid.pape.max_auth_age'          : "0",
                'openid.oa2.scope'                  : "device_auth_access",
                'openid.oa2.code_challenge_method'  : "S256",
                'openid.oa2.code_challenge'         : base64UrlEncode(java.security.MessageDigest.getInstance("SHA-256").digest(_alexaCredential.code_verifier.bytes)),
                'language'                          : _alexaCredential.language
            ].collect{ k,v -> "${k}=${java.net.URLEncoder.encode(v)}" }).join("&")
        );
        
        requestResponseFromUser(
            url:signInUrl,
            instructionalMessageForUser: (""
                + "Please go to signInUrl and sign in.  The "
                + "sign in process will conclude with your "
                + "browser being redirected to some URL, possibly "
                + "the page contents of which will be a 404 " 
                + "error message -- that's acceptable for our purposes. "
                + " Copy the URL that"
                + "your browser lands on, and enter it here."
            ),
            // callback: {
            //     String error1, String result1 ->
            //     // handle the case where error.
            //     _alexaCredential.authorizationCode = uriToQueryMap(result1)['openid.oa2.authorization_code']
            //     logger("obtained authorization code ${_alexaCredential.authorizationCode}")
            //     handleTokenRegistration(
            //         callback: {
            //             String error2, result2 ->
            //             if(error2){
            //                 logger("encountered error ${error2}")
            //             } else {
            //                 logger("token registration completed succesfully. ${error2}")
            //             }
            //         }
            //     )

            // }
        )
    }

    Closure finishOauth = {Map namedArgs ->
        String oauthResponse = namedArgs.oauthResponse
        Closure callback = namedArgs.callback
        // todo: error handling
        _alexaCredential.authorizationCode = uriToQueryMap(oauthResponse)['openid.oa2.authorization_code']
        logger("obtained authorization code ${_alexaCredential.authorizationCode}")
        handleTokenRegistration( callback: callback )
    }


    Closure refreshAlexaCookie = {Map namedArgs -> 
        // Map _alexaCredential = normalizedAlexaCredential(namedArgs.alexaCredential)
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

        
        
        if(!(_alexaCredential?.loginCookie && _alexaCredential?.refreshToken )){
            callback && callback('No former registration data provided for Cookie Refresh', null);
            return;
        }


        Map refreshData = [
            "app_name": _alexaCredential.appName,
            "app_version": _alexaCredential.apiCallVersion,
            "di.sdk.version": "6.10.0",
            "source_token": _alexaCredential.refreshToken,
            "package_name": "com.amazon.echo",
            "di.hw.version": "iPhone",
            "platform": "iOS",
            "requested_token_type": "access_token",
            "source_token_type": "refresh_token",
            "di.os.name": "iOS",
            "di.os.version": "11.4.1",
            "current_version": "6.10.0"
        ];
        Cookie = _alexaCredential.loginCookie;
        Map requestParams = [
            uri: "https://api.${_alexaCredential.baseAmazonPage}/auth/token",
            headers: [
                'User-Agent': _alexaCredential.apiCallUserAgent,
                'Accept-Language': _alexaCredential.language,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': Cookie,
                'Accept': 'application/json',
                'x-amzn-identity-auth-domain': "api.${_alexaCredential.baseAmazonPage}"
            ],
            contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
            requestContentType: groovyx.net.http.ContentType.URLENC, //this influences how the system treats the body of the request.   
            body: refreshData 
        ];
        
        logger('Alexa-Cookie: Refresh Token');
        logger(prettyPrint(requestParams));
        httpPost(requestParams,
            {response ->
                //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                //TODO: handle malformed response data here.
                logger('Refresh Token Response: ' + prettyPrint(response.data));
                _alexaCredential.loginCookie = addCookies(_alexaCredential.loginCookie, response.headers);
                if (!response.data.access_token) {
                    callback && callback('No new access token in Refresh Token response', null);
                    return;
                }
                _alexaCredential.accessToken = response.data.access_token;
                getLocalCookies(
                    amazonPage: _alexaCredential.baseAmazonPage, 
                    refreshToken: _alexaCredential.refreshToken, 
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
                        logger("_alexaCredential.loginCookie: " + _alexaCredential.loginCookie + "\n");
                        Map initCookies = cookie_parse(_alexaCredential.loginCookie);
                        logger("initCookies: " + "\n" + prettyPrint(initCookies) + "\n\n");
                        String newCookie = 'frc=' + initCookies.frc + '; ';
                        newCookie += 'map-md=' + initCookies['map-md'] + '; ';
                        newCookie += comCookie ?: '';
                        logger("newCookie: " + newCookie + "\n");
                        _alexaCredential.loginCookie = newCookie;
                        handleTokenRegistration(
                            // alexaCredential: _alexaCredential,
                            callback: callback
                        );
                    }
                );
            }
        );



    };

    construct(namedArgs1);
    return [
        'refreshAlexaCookie': refreshAlexaCookie,
        // 'generateAlexaCookie': generateAlexaCookie,
        'initiateOauth' : initiateOauth,
        'finishOauth' : finishOauth,
        'getAlexaCredential' : getAlexaCredential
        //'addCookies': addCookies, //just for debugging
        //'cookie_parse': cookie_parse //just for debugging
    ].asImmutable();
};

def newCookieStore(Map namedArgs) {
    // https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/client/CookieStore.html
    // https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/client/BasicCookieStore.html

    // List<Cookie> 
    _cookies = []



    // void     addCookie(Cookie cookie)
    // Adds an Cookie, replacing any existing equivalent cookies.
    Closure addCookie = {
        cookie ->

    }


    // void     clear()
    // Clears all cookies.
    Closure clear = {

    }

    // boolean     clearExpired(Date date)
    // Removes all of Cookies in this store that have expired by the specified Date.
    Closure clearExpired = {Date date ->

    }


    // List<Cookie>     getCookies()
    // Returns all cookies contained in this store.
    Closure getCookies = {
        return _cookies
    }

    Closure toSring = {
        
    }

    return [
        'addCookie': addCookie,
        'clear': clear,
        'clearExpired':clearExpired,
        'getCookies':getCookies,
        'toString':toString
    ]
}

def newBasicClientCookie2(Map namedArgs) {                                                                        
    /** 
     *  org.apache.http.impl.cookie
     *  Class BasicClientCookie2
     * 
     *  java.lang.Object
     *      org.apache.http.impl.cookie.BasicClientCookie
     *      org.apache.http.impl.cookie.BasicClientCookie2 
     * 
     *  All Implemented Interfaces:
     *      Serializable, Cloneable, ClientCookie, Cookie, SetCookie, SetCookie2     
     *                                                                           
     *  [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     */ 


    
                                                                                                                      
    /** 
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  constructor:                                                                                                                                                                                                
     *  BasicClientCookie                                                                                                                                                                               
     *                                                                                                                                                                                                  
     *  public BasicClientCookie(String name,                                                                                                                                                           
     *                      String value)                                                                                                                                                               
     *                                                                                                                                                                                                  
     *  Default Constructor taking a name and a value. The value may be null.                                                                                                                           
     *                                                                                                                                                                                                  
     *  Parameters:                                                                                                                                                                                     
     *      name - The name.                                                                                                                                                                            
     *      value - The value.                                                                                                                                                                          
     *                                                                                                                                                                                                  
     * 
     * 
     * 
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  constructor:
     *  BasicClientCookie2                                                                                                                          
     *                                                                                                                                              
     *  public BasicClientCookie2(String name,                                                                                                      
     *                      String value)                                                                                                           
     *                                                                                                                                              
     *  Default Constructor taking a name and a value. The value may be null.                                                                       
     *                                                                                                                                              
     *  Parameters:                                                                                                                                 
     *      name - The name.                                                                                                                        
     *      value - The value.    
     */     

    String _name  = namedArgs.name
    String _value = namedArgs.value
    String _comment = null
    String _commentURL = null
    String _domain = null
    String _path = null
    int _version = 0
    int[] _ports = []
    Date _expiryDate = null
    Date _creationDate = null
    boolean _persistent = False // what is the correct default value here?
    boolean _discard = False
    boolean _secure = False

    Map _publicSelf = [:]
    Map _attributes = [:]

                                                                                                                                            

     /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getPorts                                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  public int[] getPorts()                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  Returns null. Cookies prior to RFC2965 do not set this attribute                                                                                                                                
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getPorts in interface Cookie                                                                                                                                                                
     *  
     *                                                                                                                                                                                                   
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  getPorts                                                                                                                                    
     *                                                                                                                                              
     *  public int[] getPorts()                                                                                                                     
     *                                                                                                                                              
     *  Description copied from class: BasicClientCookie                                                                                            
     *  Returns null. Cookies prior to RFC2965 do not set this attribute                                                                            
     *                                                                                                                                              
     *  Specified by:                                                                                                                               
     *      getPorts in interface Cookie                                                                                                            
     *  Overrides:                                                                                                                                  
     *      getPorts in class BasicClientCookie                                                                                                     
     *                                                                                                                                                                                                                                                                         
     */ 
    _publicSelf.getPorts = { return _ports }

    // /**
    //  *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
    //  *  method:
    //  *  setPorts   
    //  *  public void setPorts(int[] ports)                                                                                                           
    //  *                                                                                                                                              
    //  *  Description copied from interface: SetCookie2                                                                                               
    //  *  Sets the Port attribute. It restricts the ports to which a cookie may be returned in a Cookie request header.                               
    //  *                                                                                                                                              
    //  *  Specified by:                                                                                                                               
    //  *      setPorts in interface SetCookie2                                                                                                        
    //  */                                                                                                                                              
    // _publicSelf.setPorts =  { int[] ports -> _ports = ports;  }
    _publicSelf.setPorts = {  ports -> _ports = ports;  }



    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getCommentURL                                                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  public String getCommentURL()                                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  Returns null. Cookies prior to RFC2965 do not set this attribute                                                                                                                                
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getCommentURL in interface Cookie                                                                                                                                                           
     *
     *                                                                                                                                                                                                   
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  getCommentURL                                                                                                                               
     *                                                                                                                                              
     *  public String getCommentURL()                                                                                                               
     *                                                                                                                                              
     *  Description copied from class: BasicClientCookie                                                                                            
     *  Returns null. Cookies prior to RFC2965 do not set this attribute                                                                            
     *                                                                                                                                              
     *  Specified by:                                                                                                                               
     *      getCommentURL in interface Cookie                                                                                                       
     *  Overrides:                                                                                                                                  
     *      getCommentURL in class BasicClientCookie                                                                                                
     *                                                                                                                                              
     */ 
    _publicSelf.getCommentURL =  { return null }


    
    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  setCommentURL                                                                                                                               
     *                                                                                                                                              
     *  public void setCommentURL(String commentURL)                                                                                                
     *                                                                                                                                              
     *  Description copied from interface: SetCookie2                                                                                               
     *  If a user agent (web browser) presents this cookie to a user, the cookie's purpose will be described by the information at this URL.        
     *                                                                                                                                              
     *  Specified by:                                                                                                                               
     *      setCommentURL in interface SetCookie2                                                                                                   
     *                                                                                                                                              
     */                                                                                                                                              
    _publicSelf.setCommentURL =  { String commentURL -> _commentURL = commentURL; }


    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  setDiscard                                                                                                                                  
     *                                                                                                                                              
     *  public void setDiscard(boolean discard)                                                                                                     
     *                                                                                                                                              
     *  Description copied from interface: SetCookie2                                                                                               
     *  Set the Discard attribute. Note: Discard attribute overrides Max-age.                                                                       
     *                                                                                                                                              
     *  Specified by:                                                                                                                               
     *      setDiscard in interface SetCookie2                                                                                                      
     *  See Also:                                                                                                                                   
     *      Cookie.isPersistent()                                                                                                                   
     *                                                                                                                                              
     */                                                                                                                                              
    _publicSelf.setDiscard =  { boolean discard -> _discard = discard;  }

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  isPersistent                                                                                                                                                                                    
     *                                                                                                                                                                                                  
     *  public boolean isPersistent()                                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  Returns false if the cookie should be discarded at the end of the "session"; true otherwise.                                                                                                    
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      isPersistent in interface Cookie                                                                                                                                                            
     *  Returns:                                                                                                                                                                                        
     *      false if the cookie should be discarded at the end of the "session"; true otherwise                                                                                                         
     *
     *                                                                                                                                                                                                   
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  isPersistent                                                                                                                                
     *                                                                                                                                              
     *  public boolean isPersistent()                                                                                                               
     *                                                                                                                                              
     *  Description copied from class: BasicClientCookie                                                                                            
     *  Returns false if the cookie should be discarded at the end of the "session"; true otherwise.                                                
     *                                                                                                                                              
     *  Specified by:                                                                                                                               
     *      isPersistent in interface Cookie                                                                                                        
     *  Overrides:                                                                                                                                  
     *      isPersistent in class BasicClientCookie                                                                                                 
     *  Returns:                                                                                                                                    
     *      false if the cookie should be discarded at the end of the "session"; true otherwise                                                     
     *                                                                                                                                              
     */                                                                                                                                              
    _publicSelf.isPersistent =  { return _persistent }



    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  isExpired                                                                                                                                                                                       
     *                                                                                                                                                                                                  
     *  public boolean isExpired(Date date)                                                                                                                                                             
     *                                                                                                                                                                                                  
     *  Returns true if this cookie has expired.                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      isExpired in interface Cookie                                                                                                                                                               
     *  Parameters:                                                                                                                                                                                     
     *      date - Current time                                                                                                                                                                         
     *  Returns:                                                                                                                                                                                        
     *      true if the cookie has expired.                                                                                                                                                             
     *
     *                                                                                                                                                                                                   
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  isExpired                                                                                                                                   
     *                                                                                                                                              
     *  public boolean isExpired(Date date)                                                                                                         
     *                                                                                                                                              
     *  Description copied from class: BasicClientCookie                                                                                            
     *  Returns true if this cookie has expired.                                                                                                    
     *                                                                                                                                              
     *  Specified by:                                                                                                                               
     *      isExpired in interface Cookie                                                                                                           
     *  Overrides:                                                                                                                                  
     *      isExpired in class BasicClientCookie                                                                                                    
     *  Parameters:                                                                                                                                 
     *      date - Current time                                                                                                                     
     *  Returns:                                                                                                                                    
     *      true if the cookie has expired.                                                                                                         
     *                                                                                                                                              
     */                                                                                                                                              
    _publicSelf.isExpired =  { Date date ->
        // Args.notNull(date, "Date");
        return (_expiryDate != null
            && _expiryDate.getTime() <= date.getTime());
    }

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method:
     *  clone
     *  
     *  public Object clone()                                                                                                                                                                           
     *                  throws CloneNotSupportedException                                                                                                                                               
     *                                                                                                                                                                                                  
     *  Overrides:                                                                                                                                                                                      
     *      clone in class Object                                                                                                                                                                       
     *  Throws:                                                                                                                                                                                         
     *      CloneNotSupportedException                                                                                                                                                                  
     *
     *                                                                                                                                                                                                   
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie2.html]
     *  method:
     *  clone                                                                                                                                       
     *                                                                                                                                              
     *  public Object clone()                                                                                                                       
     *                  throws CloneNotSupportedException                                                                                           
     *                                                                                                                                              
     *  Overrides:                                                                                                                                  
     *      clone in class BasicClientCookie                                                                                                        
     *  Throws:                                                                                                                                     
     *      CloneNotSupportedException                                                                                                              
     *
     */                                                                                                                                              
                                                                                                                                                

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getName                                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  public String getName()                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  Returns the name.                                                                                                                                                                               
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getName in interface Cookie                                                                                                                                                                 
     *  Returns:                                                                                                                                                                                        
     *      String name The name                                                                                                                                                                        
     *                                                                                                                                                                                                  
     */
    _publicSelf.getName =  { return _name }
                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getValue                                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  public String getValue()                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  Returns the value.                                                                                                                                                                              
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getValue in interface Cookie                                                                                                                                                                
     *  Returns:                                                                                                                                                                                        
     *      String value The current value.                                                                                                                                                             
     *                                                                                                                                                                                                  
     */
    _publicSelf.getValue =  { return _value }


    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setValue                                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  public void setValue(String value)                                                                                                                                                              
     *                                                                                                                                                                                                  
     *  Sets the value                                                                                                                                                                                  
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setValue in interface SetCookie                                                                                                                                                             
     *  Parameters:                                                                                                                                                                                     
     *      value -                                                                                                                                                                                     
     *                                                                                                                                                                                                  
     */
    _publicSelf.setValue =  { String value -> _value = value;  }
                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getComment                                                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  public String getComment()                                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  Returns the comment describing the purpose of this cookie, or null if no such comment has been defined.                                                                                         
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getComment in interface Cookie                                                                                                                                                              
     *  Returns:                                                                                                                                                                                        
     *      comment                                                                                                                                                                                     
     *  See Also:                                                                                                                                                                                       
     *      setComment(String)                                                                                                                                                                          
     *                                                                                                                                                                                                  
     */
    _publicSelf.getComment =  { return _comment }




    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setComment                                                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  public void setComment(String comment)                                                                                                                                                          
     *                                                                                                                                                                                                  
     *  If a user agent (web browser) presents this cookie to a user, the cookie's purpose will be described using this comment.                                                                        
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setComment in interface SetCookie                                                                                                                                                           
     *  Parameters:                                                                                                                                                                                     
     *      comment -                                                                                                                                                                                   
     *  See Also:                                                                                                                                                                                       
     *      getComment()                                                                                                                                                                                
     *                                                                                                                                                                                                  
     */
    _publicSelf.setComment =  { String comment -> _comment = comment;  }
                                                                                                                                                                                                      
                                                                                                                                                                                       
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getExpiryDate                                                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  public Date getExpiryDate()                                                                                                                                                                     
     *                                                                                                                                                                                                  
     *  Returns the expiration Date of the cookie, or null if none exists.                                                                                                                              
     *                                                                                                                                                                                                  
     *  Note: the object returned by this method is considered immutable. Changing it (e.g. using setTime()) could result in undefined behaviour. Do so at your peril.                                  
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getExpiryDate in interface Cookie                                                                                                                                                           
     *  Returns:                                                                                                                                                                                        
     *      Expiration Date, or null.                                                                                                                                                                   
     *  See Also:                                                                                                                                                                                       
     *      setExpiryDate(java.util.Date)                                                                                                                                                               
     *                                                                                                                                                                                                  
     */
    _publicSelf.getExpiryDate =  { return _expiryDate }


    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setExpiryDate                                                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  public void setExpiryDate(Date expiryDate)                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  Sets expiration date.                                                                                                                                                                           
     *                                                                                                                                                                                                  
     *  Note: the object returned by this method is considered immutable. Changing it (e.g. using setTime()) could result in undefined behaviour. Do so at your peril.                                  
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setExpiryDate in interface SetCookie                                                                                                                                                        
     *  Parameters:                                                                                                                                                                                     
     *      expiryDate - the Date after which this cookie is no longer valid.                                                                                                                           
     *  See Also:                                                                                                                                                                                       
     *      getExpiryDate()                                                                                                                                                                             
     *                                                                                                                                                                                                  
     */
    _publicSelf.setExpiryDate =  { Date expiryDate -> _expiryDate = expiryDate;  }

                                                                                                                                                                                                      

                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getDomain                                                                                                                                                                                       
     *                                                                                                                                                                                                  
     *  public String getDomain()                                                                                                                                                                       
     *                                                                                                                                                                                                  
     *  Returns domain attribute of the cookie.                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getDomain in interface Cookie                                                                                                                                                               
     *  Returns:                                                                                                                                                                                        
     *      the value of the domain attribute                                                                                                                                                           
     *  See Also:                                                                                                                                                                                       
     *      setDomain(java.lang.String)                                                                                                                                                                 
     *                                                                                                                                                                                                  
     */
    _publicSelf.getDomain =  { return _domain }


    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setDomain                                                                                                                                                                                       
     *                                                                                                                                                                                                  
     *  public void setDomain(String domain)                                                                                                                                                            
     *                                                                                                                                                                                                  
     *  Sets the domain attribute.                                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setDomain in interface SetCookie                                                                                                                                                            
     *  Parameters:                                                                                                                                                                                     
     *      domain - The value of the domain attribute                                                                                                                                                  
     *  See Also:                                                                                                                                                                                       
     *      getDomain()                                                                                                                                                                                 
     *                                                                                                                                                                                                  
     */
    _publicSelf.setDomain =  { String domain -> _domain = domain;  }


                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getPath                                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  public String getPath()                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  Returns the path attribute of the cookie                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getPath in interface Cookie                                                                                                                                                                 
     *  Returns:                                                                                                                                                                                        
     *      The value of the path attribute.                                                                                                                                                            
     *  See Also:                                                                                                                                                                                       
     *      setPath(java.lang.String)                                                                                                                                                                   
     *                                                                                                                                                                                                  
     */
    _publicSelf.getPath =  {return _path}
                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setPath                                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  public void setPath(String path)                                                                                                                                                                
     *                                                                                                                                                                                                  
     *  Sets the path attribute.                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setPath in interface SetCookie                                                                                                                                                              
     *  Parameters:                                                                                                                                                                                     
     *      path - The value of the path attribute                                                                                                                                                      
     *  See Also:                                                                                                                                                                                       
     *      getPath()                                                                                                                                                                                   
     *                                                                                                                                                                                                  
     */
    _publicSelf.setPath =  { String path -> _path = path;  }
                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  isSecure                                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  public boolean isSecure()                                                                                                                                                                       
     *                                                                                                                                                                                                  
     *  Description copied from interface: Cookie                                                                                                                                                       
     *  Indicates whether this cookie requires a secure connection.                                                                                                                                     
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      isSecure in interface Cookie                                                                                                                                                                
     *  Returns:                                                                                                                                                                                        
     *      true if this cookie should only be sent over secure connections.                                                                                                                            
     *  See Also:                                                                                                                                                                                       
     *      setSecure(boolean)                                                                                                                                                                          
     *                                                                                                                                                                                                  
     */
    _publicSelf.isSecure =  {return _secure}

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setSecure                                                                                                                                                                                       
     *                                                                                                                                                                                                  
     *  public void setSecure(boolean secure)                                                                                                                                                           
     *                                                                                                                                                                                                  
     *  Sets the secure attribute of the cookie.                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  When true the cookie should only be sent using a secure protocol (https). This should only be set when the cookie's originating server used a secure protocol to set the cookie's value.        
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setSecure in interface SetCookie                                                                                                                                                            
     *  Parameters:                                                                                                                                                                                     
     *      secure - The value of the secure attribute                                                                                                                                                  
     *  See Also:                                                                                                                                                                                       
     *      isSecure()                                                                                                                                                                                  
     *                                                                                                                                                                                                  
     */
    _publicSelf.setSecure =  { boolean secure -> _secure = secure;  }                                                                                                                                                                                                  

                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getVersion                                                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  public int getVersion()                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  Returns the version of the cookie specification to which this cookie conforms.                                                                                                                  
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getVersion in interface Cookie                                                                                                                                                              
     *  Returns:                                                                                                                                                                                        
     *      the version of the cookie.                                                                                                                                                                  
     *  See Also:                                                                                                                                                                                       
     *      setVersion(int)                                                                                                                                                                             
     *                                                                                                                                                                                                  
     */
    _publicSelf.getVersion =  {return _version}
                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setVersion                                                                                                                                                                                      
     *                                                                                                                                                                                                  
     *  public void setVersion(int version)                                                                                                                                                             
     *                                                                                                                                                                                                  
     *  Sets the version of the cookie specification to which this cookie conforms.                                                                                                                     
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      setVersion in interface SetCookie                                                                                                                                                           
     *  Parameters:                                                                                                                                                                                     
     *      version - the version of the cookie.                                                                                                                                                        
     *  See Also:                                                                                                                                                                                       
     *      getVersion()                                                                                                                                                                                
     *                                                                                                                                                                                                  
     */
    _publicSelf.setVersion =  { int version -> _version = version;  }                                                                                                                                                                                                      
                                                                                                                                                                                           
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getCreationDate                                                                                                                                                                                 
     *                                                                                                                                                                                                  
     *  public Date getCreationDate()                                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  Since:                                                                                                                                                                                          
     *      4.4                                                                                                                                                                                         
     *                                                                                                                                                                                                  
     */
    _publicSelf.getCreationDate =  { return _creationDate }

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setCreationDate                                                                                                                                                                                 
     *                                                                                                                                                                                                  
     *  public void setCreationDate(Date creationDate)                                                                                                                                                  
     *                                                                                                                                                                                                  
     *  Since:                                                                                                                                                                                          
     *      4.4                                                                                                                                                                                         
     *                                                                                                                                                                                                  
     */
    _publicSelf.setCreationDate =  { Date creationDate -> _creationDate = creationDate;  }  

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  setAttribute                                                                                                                                                                                    
     *                                                                                                                                                                                                  
     *  public void setAttribute(String name,                                                                                                                                                           
     *                  String value)                                                                                                                                                                   
     *                                                                                                                                                                                                  
     */
    _publicSelf.setAttribute =  { String name, String value -> 
        _attributes.put(name, value); 
        
    }  

    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  getAttribute                                                                                                                                                                                    
     *                                                                                                                                                                                                  
     *  public String getAttribute(String name)                                                                                                                                                         
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      getAttribute in interface ClientCookie                                                                                                                                                      
     *                                                                                                                                                                                                  
     */
    _publicSelf.getAttribute =  { String name -> 
        return _attributes.get(name)
    }  


    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  containsAttribute                                                                                                                                                                               
     *                                                                                                                                                                                                  
     *  public boolean containsAttribute(String name)                                                                                                                                                   
     *                                                                                                                                                                                                  
     *  Specified by:                                                                                                                                                                                   
     *      containsAttribute in interface ClientCookie                                                                                                                                                 
     *                                                                                                                                                                                                  
     */
    _publicSelf.containsAttribute =  { String name -> 
        return _attributes.containsKey(name)
    }  

                                                                                                                                                                                                      
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  removeAttribute                                                                                                                                                                                 
     *                                                                                                                                                                                                  
     *  public boolean removeAttribute(String name)                                                                                                                                                     
     *                                                                                                                                                                                                  
     *  Since:                                                                                                                                                                                          
     *      4.4                                                                                                                                                                                         
     *                                                                                                                                                                                                  
     *                                                                                                                                                                                             
     *                                                                                                                                                                                                  
     */
    _publicSelf.removeAttribute =  { String name -> 
        return _attributes.remove(name) != null;
    }                                                                                                                                                                                                    
                                                                                                                                                                             
    /**  
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/cookie/BasicClientCookie.html]
     *  method: 
     *  toString                                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  public String toString()                                                                                                                                                                        
     *                                                                                                                                                                                                  
     *  Overrides:                                                                                                                                                                                      
     *      toString in class Object                                                                                                                                                                    
     *                                                                                                                                                                                                  
     */
    _publicSelf.toString =  { 
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[version: ");
        buffer.append(Integer.toString(_version));
        buffer.append("]");
        buffer.append("[name: ");
        buffer.append(_name);
        buffer.append("]");
        buffer.append("[value: ");
        buffer.append(_value);
        buffer.append("]");
        buffer.append("[domain: ");
        buffer.append(_domain);
        buffer.append("]");
        buffer.append("[path: ");
        buffer.append(_path);
        buffer.append("]");
        buffer.append("[expiry: ");
        buffer.append(_expiryDate);
        buffer.append("]");
        return buffer.toString();
    }     

    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  VERSION_ATTR                                                                                                                                                                                          
     *                                                                                                                                                                                                        
     *  @Obsolete                                                                                                                                                                                             
     *  static final String VERSION_ATTR                                                                                                                                                                      
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.VERSION_ATTR]                                                                                                                                                                            
     *                                                                                                                                                                                                        
     */
    final String    VERSION_ATTR     = "version"


    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  PATH_ATTR                                                                                                                                                                                             
     *                                                                                                                                                                                                        
     *  static final String PATH_ATTR                                                                                                                                                                         
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.PATH_ATTR]                                                                                                                                                                            
     *                                                                                                                                                                                                        
     */
    final String    PATH_ATTR        = "path"

    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  DOMAIN_ATTR                                                                                                                                                                                           
     *                                                                                                                                                                                                        
     *  static final String DOMAIN_ATTR                                                                                                                                                                       
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.DOMAIN_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    DOMAIN_ATTR      = "domain"

    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  MAX_AGE_ATTR                                                                                                                                                                                          
     *                                                                                                                                                                                                        
     *  static final String MAX_AGE_ATTR                                                                                                                                                                      
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.MAX_AGE_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    MAX_AGE_ATTR     = "max-age"


    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  SECURE_ATTR                                                                                                                                                                                           
     *                                                                                                                                                                                                        
     *  static final String SECURE_ATTR                                                                                                                                                                       
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.SECURE_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    SECURE_ATTR      = "secure"

    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  COMMENT_ATTR                                                                                                                                                                                          
     *                                                                                                                                                                                                        
     *  @Obsolete                                                                                                                                                                                             
     *  static final String COMMENT_ATTR                                                                                                                                                                      
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.COMMENT_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    COMMENT_ATTR     = "comment"


    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  EXPIRES_ATTR                                                                                                                                                                                          
     *                                                                                                                                                                                                        
     *  static final String EXPIRES_ATTR                                                                                                                                                                      
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.EXPIRES_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    EXPIRES_ATTR     = "expires"

    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  PORT_ATTR                                                                                                                                                                                             
     *                                                                                                                                                                                                        
     *  @Obsolete                                                                                                                                                                                             
     *  static final String PORT_ATTR                                                                                                                                                                         
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.PORT_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    PORT_ATTR        = "port"

    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  COMMENTURL_ATTR                                                                                                                                                                                       
     *                                                                                                                                                                                                        
     *  @Obsolete                                                                                                                                                                                             
     *  static final String COMMENTURL_ATTR                                                                                                                                                                   
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.COMMENTURL_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */
    final String    COMMENTURL_ATTR  = "commenturl"


    /**
     *  from [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/cookie/ClientCookie.html
     *  field:                                                                                                                                                                                                      
     *  DISCARD_ATTR                                                                                                                                                                                          
     *                                                                                                                                                                                                        
     *  @Obsolete                                                                                                                                                                                             
     *  static final String DISCARD_ATTR                                                                                                                                                                      
     *                                                                                                                                                                                                        
     *  See Also:                                                                                                                                                                                             
     *      Constant Field Values [https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/constant-values.html#org.apache.http.cookie.ClientCookie.DISCARD_ATTR]                                                                                                                                                                                 
     *                                                                                                                                                                                                        
     */ 
    final String    DISCARD_ATTR     = "discard"      


    //============================================
    return _publicSelf;    

                                                                                                                                                                                                                  
                                                                                                                                                                                                        
                                                                                                                                                                                 
                                                                                                                                                                                                        
                                                                                                                                                                                                        
                                                                                                                                                                                                        
                                                                                                                                                                                                        
}                                                                                                                                                                                                       