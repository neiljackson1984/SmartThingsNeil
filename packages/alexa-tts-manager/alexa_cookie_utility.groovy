//This file is intended to be included into a hubitat app source code file.

def newAlexaCookieUtility(Map namedArgs1) {
    Closure     logger              = namedArgs1?.logger              ?: Closure.IDENTITY ;
    Map         alexaCredential     = namedArgs1?.alexaCredential     ?: [:]              ;
    
    //default logger is to do nothing (I choose to use Closure.IDENTITY
    //instead of a literal closure expression here in the hopes that
    //Closure.IDENTITY will incur less runtime overhead.)


    String Cookie='';

    final List<String> csrfPathCandidates = [
        '/api/language',
        '/spa/index.html',
        '/api/devices-v2/device?cached=false',
        '/templates/oobe/d-device-pick.handlebars',
        '/api/strings'
    ].asImmutable();
    //Groovy does not respect my final, nor my "<String>" type specification,
    //but they are my intent nonetheless.

    final String baseAmazonPageHandle = ''
    final String baseAmazonPage = "amazon.com"
    // really ought to be called baseAmazonDomain
    final String language="en_US"
    final String apiCallVersion = '2.2.485407.0'
    final String apiCallUserAgent =  "AmazonWebView/Amazon Alexa/${apiCallVersion}/iOS/15.5/iPhone"
    final String userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36'
    final String appName = 'ioBroker Alexa2'
    final String deviceIdSuffix = '23413249564c5635564d32573831'
    final String officialUserAgent = 'AppleWebKit PitanguiBridge/2.2.483723.0-[HARDWARE=iPhone10_4][SOFTWARE=15.5][DEVICE=iPhone]';

 

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



    /**
    *  applies any cookies that may be present in a set of http headers (an
    *  iterable of org.apache.http.Header)  to an existing Cookie string (adding
    *  any cookies that that do not already exist, and updating any that do.)
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
        logger(internalDebugMessage);
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



    Closure getCSRFFromCookies = {Map namedArgs  -> 
        String cookie = namedArgs.cookie;
        Closure callback = namedArgs.callback;
        
        String csrf = null; //our goal is to obtain a csrf token and assign it to this string. 
        for(csrfPathCandidate in csrfPathCandidates){
            logger('Alexa-Cookie: Step 4: get CSRF via ' + csrfPathCandidate);
            httpGet(
                [
                    uri: "https://alexa." + baseAmazonPage + csrfPathCandidate,
                    'headers': [
                        'DNT': '1',
                        'User-Agent': userAgent,
                        'Connection': 'keep-alive',
                        'Referer': 'https://alexa.' + baseAmazonPage + '/spa/index.html',
                        'Cookie': cookie,
                        'Accept': '*/*',
                        'Origin': 'https://alexa.' + baseAmazonPage 
                    ]
                ],
                {response ->
                    cookie = addCookies(cookie, response.headers);
                    java.util.regex.Matcher csrfMatcher = (~/csrf=([^;]+)/).matcher(cookie);
                    if(csrfMatcher.find()){
                        csrf = csrfMatcher.group(1);
                        logger('Alexa-Cookie: Result: csrf=' + csrf.toString() + ', Cookie=' + cookie);
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
                'User-Agent': userAgent,
                'Accept-Language': language,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': '*/*'
            ],
            contentType: groovyx.net.http.ContentType.JSON, // type of content that we expect the response to contain //this influences the type of object that the system passes to the callback. ,
            requestContentType: groovyx.net.http.ContentType.URLENC, //type of content that the request will contain.  corresponds to the 'Content-Type' header of the request. By default, this is assumed to be the same as the expected content type of the response, unless explicitly specified //this influences how the system treats the body of the request.   
            body: exchangeParams 
        ];
        logger('Alexa-Cookie: Exchange tokens for ' + amazonPage);
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
                if (!response.data.response.tokens.cookies['.' + amazonPage]) {
                    callback && callback('No cookies for ' + amazonPage + ' in Exchange response', null);
                    return;
                }

                Cookie = addCookies(Cookie, response.headers);
                Map cookies = cookie_parse(Cookie);
                response.data.response.tokens.cookies['.' + amazonPage].each {cookie ->
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

                String localCookie = cookies.collect{it.key + "=" + it.value}.join("; ");
                callback && callback(null, localCookie);
            }
        );
    };

    Closure handleTokenRegistration = {Map namedArgs ->
        Closure callback = namedArgs.callback;
        logger('Handle token registration Start: ' + prettyPrint(alexaCredential));
        String deviceSerial;

        Map cookies = cookie_parse(alexaCredential.loginCookie);
        Cookie = alexaCredential.loginCookie;

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
                "app_name": appName,
                "software_version": "1"
            ],
            "auth_data": [
                "access_token": alexaCredential.accessToken
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
            uri: "https://api.${baseAmazonPage}/auth/register",
            headers: [
                'User-Agent': apiCallUserAgent,
                'Accept-Language': language,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'Cookie': alexaCredential.loginCookie,
                'Accept': '*/*',
                'x-amzn-identity-auth-domain': 'api.${baseAmazonPage}'
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
                Cookie = addCookies(Cookie, response0.headers);
                alexaCredential.refreshToken = response0.data.response.success.tokens.bearer.refresh_token;
                alexaCredential.tokenDate = now();

                //comment from original javascript: Get Amazon Marketplace Country
                Map requestParams1 = [
                    uri: "https://alexa.${baseAmazonPage}/api/users/me?platform=ios&version=2.2.223830.0",
                    headers: [
                        'User-Agent': apiCallUserAgent,
                        'Accept-Language': language,
                        'Accept-Charset': 'utf-8',
                        'Connection': 'keep-alive',
                        'Accept': 'application/json',
                        'Cookie': Cookie
                    ],
                    contentType: groovyx.net.http.ContentType.JSON, //this influences the type of object that the system passes to the callback. ,
                ];
                logger('Alexa-Cookie: Get User data');
                logger(prettyPrint(requestParams1));
                httpGet(requestParams1,
                    {response1 -> 
                        //TODO: handle response errors here (or maybe outside with a try{}catch(){} statement.)
                        logger('Get User data Response: ' + prettyPrint(response1.data));
                        Cookie = addCookies(Cookie, response1.headers);
                        if (response1.data.marketPlaceDomainName) {
                            java.util.regex.Matcher amazonPageMatcher = (~/^[^\.]*\.([\S\s]*)$/).matcher(response1.data.marketPlaceDomainName);
                            if(amazonPageMatcher.find()){
                                options.amazonPage = amazonPageMatcher.group(1);
                            }
                        }
                        alexaCredential.amazonPage = amazonPage;
                        alexaCredential.loginCookie = Cookie;
                        getLocalCookies(
                            amazonPage: alexaCredential.amazonPage, 
                            refreshToken: alexaCredential.refreshToken, 
                            callback: {String err0, String localCookie ->
                                if (err0) {
                                    callback && callback(err0, null);
                                }
                                alexaCredential.localCookie = localCookie;
                                getCSRFFromCookies(
                                    cookie: alexaCredential.localCookie, 
                                    options: options,
                                    callback: {String err1, Map resData ->
                                        if (err1) {
                                            callback && callback('Error getting csrf for ' + alexaCredential.amazonPage + ':' + err1, null);
                                            return;
                                        }
                                        alexaCredential.localCookie = resData.cookie;
                                        alexaCredential.csrf = resData.csrf;
                                        // alexaCredential.removeAll{key, value -> key == 'accessToken'};
                                        alexaCredential.remove('accessToken');
                                        logger('Final Registraton Result: ' + prettyPrint(alexaCredential));
                                        callback && callback(null, alexaCredential);
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

        // Closure saveContext = namedArgs.saveContext;
        // // saveContext is expected to be a callback having signature void saveContext(Map context)

        Closure redirectUserToUrl = namedArgs.redirectUserToUrl
        // // redirectUserToUrl is expected to be a callback having signature void redirectUserToUrl(String url)

        alexaCredential = [:]

        alexaCredential.deviceSerial    = alexaCredential.deviceSerial ?: randomBytes(16).encodeHex().toString();
        alexaCredential.deviceId        = alexaCredential.deviceId ?: ( alexaCredential.deviceSerial + deviceIdSuffix );
        alexaCredential.frc             = alexaCredential.frc ?: base64Encode(randomBytes(313));
        alexaCredential.mapMd           = alexaCredential.mapMd ?: (
                groovy.json.JsonOutput.toJson(
                    [
                        "device_user_dictionary":[],
                        "device_registration_data":[
                            "software_version":"1"
                        ],
                        "app_identifier":[
                            "app_version": apiCallVersion,
                            "bundle_id":"com.amazon.echo"
                        ]
                    ]
                ).bytes.encodeBase64().toString()
            ) 

        alexaCredential.code_verifier   = base64UrlEncode(randomBytes(32))
        code_challenge  = base64UrlEncode(java.security.MessageDigest.getInstance("SHA-256").digest(alexaCredential.code_verifier.bytes))

        signInUrl = (""
            + "https://www.${baseAmazonPage}/ap/signin" 

            // # I would like to find a url that would not require sign-in if the user was already logged in.
            // # f"https://www.{self.baseAmazonPage}/ap" 
            // # f"https://www.{self.baseAmazonPage}/ap/oa" 
            // # f"https://www.{self.baseAmazonPage}"
            // #f"https://www.{self.baseAmazonPage}/ap/maplanding"
            // #
            // # the trick, it seems, is to omit the openid.pape.max_auth_age parameter.  

            + "?" + ([
                'openid.return_to'                  : "https://www.${baseAmazonPage}/ap/maplanding",
                //# 'openid.return_to'                  : f"https://localhost", # doesn't work
                'openid.assoc_handle'               : "amzn_dp_project_dee_ios${baseAmazonPageHandle}",
                'openid.identity'                   : "http://specs.openid.net/auth/2.0/identifier_select",
                'pageId'                            : "amzn_dp_project_dee_ios${baseAmazonPageHandle}",
                'accountStatusPolicy'               : "P1",
                'openid.claimed_id'                 : "http://specs.openid.net/auth/2.0/identifier_select",
                //# 'openid.mode'                       : "setup",
                //# 'openid.mode'                       : "id_res",
                'openid.mode'                       : "checkid_setup",
                //# 'openid.mode'                       : "checkid_immediate",
                'openid.ns.oa2'                     : "http://www.${baseAmazonPage}/ap/ext/oauth/2",
                'openid.oa2.client_id'              : "device:${alexaCredential.deviceId}",
                'openid.ns.pape'                    : "http://specs.openid.net/extensions/pape/1.0",
                'openid.oa2.response_type'          : "code",
                'openid.ns'                         : "http://specs.openid.net/auth/2.0",
                //# 'openid.pape.max_auth_age'          : "0",
                'openid.oa2.scope'                  : "device_auth_access",
                'openid.oa2.code_challenge_method'  : "S256",
                'openid.oa2.code_challenge'         : code_challenge,
                'language'                          : language
            ].collect{ k,v -> "${k}=${java.net.URLEncoder.encode(v)}" }).join("&")
        );
        
        redirectUserToUrl(signInUrl)

    }

    Closure finishOauth = {Map namedArgs ->


        String response = namedArgs.response;

        // authorizationCode : str = myUrlSplit(resultantUrl)['firstedQuerydict']['openid.oa2.authorization_code']
    }


    Closure refreshAlexaCookie = {Map namedArgs -> 
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

        
        
        if(!(alexaCredential?.loginCookie && alexaCredential?.refreshToken )){
            callback && callback('No former registration data provided for Cookie Refresh', null);
            return;
        }


        Map refreshData = [
            "app_name": appName,
            "app_version": "2.2.223830.0",
            "di.sdk.version": "6.10.0",
            "source_token": alexaCredential.refreshToken,
            "package_name": "com.amazon.echo",
            "di.hw.version": "iPhone",
            "platform": "iOS",
            "requested_token_type": "access_token",
            "source_token_type": "refresh_token",
            "di.os.name": "iOS",
            "di.os.version": "11.4.1",
            "current_version": "6.10.0"
        ];
        Cookie = alexaCredential.loginCookie;
        Map requestParams = [
            uri: "https://api.${baseAmazonPage}/auth/token",
            headers: [
                'User-Agent': apiCallUserAgent,
                'Accept-Language': language,
                'Accept-Charset': 'utf-8',
                'Connection': 'keep-alive',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': Cookie,
                'Accept': 'application/json',
                'x-amzn-identity-auth-domain': "api.${baseAmazonPage}"
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
                alexaCredential.loginCookie = addCookies(alexaCredential.loginCookie, response.headers);
                if (!response.data.access_token) {
                    callback && callback('No new access token in Refresh Token response', null);
                    return;
                }
                alexaCredential.accessToken = response.data.access_token;
                getLocalCookies(
                    amazonPage: baseAmazonPage, 
                    refreshToken: alexaCredential.refreshToken, 
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
                        logger("alexaCredential.loginCookie: " + alexaCredential.loginCookie + "\n");
                        Map initCookies = cookie_parse(alexaCredential.loginCookie);
                        logger("initCookies: " + "\n" + prettyPrint(initCookies) + "\n\n");
                        String newCookie = 'frc=' + initCookies.frc + '; ';
                        newCookie += 'map-md=' + initCookies['map-md'] + '; ';
                        newCookie += comCookie ?: '';
                        logger("newCookie: " + newCookie + "\n");
                        alexaCredential.loginCookie = newCookie;
                        handleTokenRegistration(
                            callback: callback
                        );
                    }
                );
            }
        );



    };

    Closure getAlexaCredential = {
        return alexaCredential;
    }

    return [
        'refreshAlexaCookie': refreshAlexaCookie,
        'generateAlexaCookie': generateAlexaCookie,
        'initiateOauth' : initiateOauth,
        'getAlexaCredential' : getAlexaCredential
        //'addCookies': addCookies, //just for debugging
        //'cookie_parse': cookie_parse //just for debugging
    ].asImmutable();
};