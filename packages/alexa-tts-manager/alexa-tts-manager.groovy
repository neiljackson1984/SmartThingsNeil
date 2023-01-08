/*
 *  Alexa TTS Manager
 *
 *  https://raw.githubusercontent.com/ogiewon/Hubitat/master/AlexaTTS/Apps/alexa-tts-manager.src/alexa-tts-manager.groovy
 *
 *
 *  Copyright 2018 Daniel Ogorchock - Special thanks to Chuck Schwer for his tips and prodding me
 *                                    to not let this idea fall through the cracks!  
 *  Copyright 2018 Gabriele         - Automatic cookie refresh with Apollon77/Alexa-Cookie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Version   Date        Who             What
 *    -------   ----        ---             ----
 *     v0.1.0   2018-10-20  Dan Ogorchock   Original Creation - with help from Chuck Schwer!
 *     v0.1.1   2018-10-21  Dan Ogorchock   Trapped an error when invalid data returned from Amazon due to cookie issue
 *     v0.2.0   2018-10-21  Stephan Hackett Modified to include more Alexa Devices for selection
 *     v0.3.0   2018-10-22  Dan Ogorchock   Added support for Canada and United Kingdom, and ability to rename the app
 *     v0.4.0   2018-11-18  Stephan Hackett Added support for Virtual Container
 *     v0.4.1   2018-11-18  Dan Ogorchock   Optimized multi-country support code and added Notification support for errors
 *     v0.4.2   2018-11-27  Dan Ogorchock   Improved error handling for notifications when cookie expires (via live logging and optoinally, via push notification)
 *     v0.4.3   2018-12-07  Dan Ogorchock   Prevent sending empty string TTS messages to Amazon.
 *     v0.4.4   2018-12-10  Dan Ogorchock   Detect and notify via logging and notification, message rate exceeded errors to avoid confusion with cookie expiration errors.
 *     v0.4.5   2018-12-14  Stephan Hackett Added ability to paste in the entire Raw Cookie.  No manual editing required.  Improved setup page flow.
 *     v0.4.6   2018-12-23  Dan Ogorchock   Added support for Italy.  Thank you @gabriele!
 *     v0.5.0   2019-01-02  Gabriele        Added support for automatic cookie refresh with external NodeJS webserver
 *     v0.5.1   2019-02-12  Dan Ogorchock   Corrected contentType to prevent errors in response parsing
 *     v0.5.2   2019-04-04  Thomas Howard   Added get/set Volume Control (not working currently - Dan O 4/6/19)
 *     v0.5.3   2019-04-16  Gabriele        Added app events to have some historic logging
 *     v0.5.4   2019-06-24  Dan Ogorchock   Attempt to add Australia
 *     v0.5.5   2019-07-18  Dan Ogorchock   Reduced Debug Logging
 *     v0.5.6   2020-01-02  Dan Ogorchock   Add support for All Echo Device Broadcast
 *     v0.5.7   2020-01-02  Bob Butler      Add an override switch that disables all voice messages when off 
 *     v0.5.8   2020-01-07  Marco Felicio   Added support for Brazil
 *     v0.5.9   2020-01-26  Dan Ogorchock   Changed automatic cookie refresh time to 1am to avoid hub maintenance window
 */

definition(
    name: "Alexa TTS Manager",
    namespace: "ogiewon",
    author: "Dan Ogorchock",
    description: "Manages your Alexa TTS Child Devices",
    iconUrl: "",
    iconX2Url: "")



def mainTestCode1(){
	def message = ""
	message += "\n\n";

    message += "ahoy there " + "\n";
    

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
   

    def r = "MiTgMov8r9NxQJOq5xbvsSGBAjtywj/YE2c0WtEB9e8=".decodeBase64()
    def codeVerifier = base64UrlEncode(r)
    def codeChallenge = base64UrlEncode(java.security.MessageDigest.getInstance("SHA-256").digest(codeVerifier.bytes))
    message += "r                           : " + r + "\n";
    message += "r.encodeBase64().toString() : " + r.encodeBase64().toString() + "\n";
    message += "base64Encode(r)             : " + base64Encode(r) + "\n";
    message += "base64UrlEncode(r)          : " + base64UrlEncode(r) + "\n";
    message += "codeVerifier                : " + codeVerifier + "\n";
    message += "codeVerifier.bytes          : " + (codeVerifier.bytes).toString() + "\n";
    message += "codeChallenge               : " + codeChallenge + "\n";
 

    message += "codeChallenge               : " + codeChallenge + "\n";

    def alexaCookieUtility = newAlexaCookieUtility()
    alexaCookieUtility.initiateOauth( 
        redirectUserToUrl : {url ->  
            message += "\n";
            message += "url               : " + url + "\n";
            message += "\n";
        }
    )
    def alexaCredential = alexaCookieUtility.getAlexaCredential()
    // message += "alexaCredential               : " + alexaCredential + "\n";
    // message += "groovy.json.JsonOutput.toJson(alexaCredential) : " + groovy.json.JsonOutput.toJson(alexaCredential) + "\n";
    encodedAlexaCredential = base64Encode(groovy.json.JsonOutput.toJson(alexaCredential).bytes)
    message += "\n";
    message += "encodedAlexaCredential        : " + encodedAlexaCredential + "\n";
    message += "\n";
    // deserializedAlexaCredential = new groovy.json.JsonSlurper().parseText(encodedAlexaCredential.decodeBase64())
    

    //==========================================
    // url: https://www.amazon.com/ap/signin?openid.return_to=https%3A%2F%2Fwww.amazon.com%2Fap%2Fmaplanding&openid.assoc_handle=amzn_dp_project_dee_ios&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&pageId=amzn_dp_project_dee_ios&accountStatusPolicy=P1&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.mode=checkid_setup&openid.ns.oa2=http%3A%2F%2Fwww.amazon.com%2Fap%2Fext%2Foauth%2F2&openid.oa2.client_id=device%3A3e0d023cf36de749ca5aa17f1a4d807823413249564c5635564d32573831&openid.ns.pape=http%3A%2F%2Fspecs.openid.net%2Fextensions%2Fpape%2F1.0&openid.oa2.response_type=code&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.oa2.scope=device_auth_access&openid.oa2.code_challenge_method=S256&openid.oa2.code_challenge=cgBEd0-Yz2dlQ0uL1pXD86uPY8OXMBwjQ__yJx4gzFM&language=en_US
    // encodedAlexaCredential : "eyJkZXZpY2VTZXJpYWwiOiIzZTBkMDIzY2YzNmRlNzQ5Y2E1YWExN2YxYTRkODA3OCIsImRldmljZUlkIjoiM2UwZDAyM2NmMzZkZTc0OWNhNWFhMTdmMWE0ZDgwNzgyMzQxMzI0OTU2NGM1NjM1NTY0ZDMyNTczODMxIiwiZnJjIjoidGg0bmhuUXF6K0lmc1FjRjROTUR6RmRCeXBEZWZxZWlUOTh1Nm9FQlEyVm90UkVRZ21YRnRqMlR3WGxWek9yc0JkOVdyYTdlVkc0WGJCYlNhTmRIYmtsc2lHbmV2MXhVUVZ0YkN6NEVGSVFBcnBPYlhqQTdCaUVPd29FWCt1TTdvVWMrMkRQTVdrdzJvU2o2ZEYxUHNYcHExc2s3a1Z6eGIvQXh2OWw3NWk0dEVNOUJrL29RR2JvWUluRFZiL0FxV3VqQVZUanVYS25hL2dpalB3RVpySjN0bnl2d1lNMUpmZThOS3NIRjVhYTcxWUJ4YXRSbVRZMnNUSTJhUlQ2Y0Y5RklDNlFUYktQR2UrV0FueWpMRTJwTlZhZU0wYWx4aUFybi9vS3ZyS2F5cjlBQjZnMkw0KzA2MG45Vy9VamdhemxFTUh4a0ZNOGVMdVV2VXZYMnQveU1SbHRpcWlKQzRoOWNKTlN3VWtkMGhTbUd2SDBycVhmbnEzK3AwVlJpZFZXQkU1R3JoTU9sTWFNL2l6UVdHSEFoaW5UeWp5SGFEdz09IiwibWFwTWQiOiJleUprWlhacFkyVmZkWE5sY2w5a2FXTjBhVzl1WVhKNUlqcGJYU3dpWkdWMmFXTmxYM0psWjJsemRISmhkR2x2Ymw5a1lYUmhJanA3SW5OdlpuUjNZWEpsWDNabGNuTnBiMjRpT2lJeEluMHNJbUZ3Y0Y5cFpHVnVkR2xtYVdWeUlqcDdJbUZ3Y0Y5MlpYSnphVzl1SWpvaU1pNHlMalE0TlRRd055NHdJaXdpWW5WdVpHeGxYMmxrSWpvaVkyOXRMbUZ0WVhwdmJpNWxZMmh2SW4xOSIsImNvZGVfdmVyaWZpZXIiOiJreUJ3ejRyMGpRSWZVOVB4WUF1V2pBWERrczNxOHZFZ204SlN3aldoVlQwIn0=" 
    encodedAlexaCredential = "eyJkZXZpY2VTZXJpYWwiOiIzZTBkMDIzY2YzNmRlNzQ5Y2E1YWExN2YxYTRkODA3OCIsImRldmljZUlkIjoiM2UwZDAyM2NmMzZkZTc0OWNhNWFhMTdmMWE0ZDgwNzgyMzQxMzI0OTU2NGM1NjM1NTY0ZDMyNTczODMxIiwiZnJjIjoidGg0bmhuUXF6K0lmc1FjRjROTUR6RmRCeXBEZWZxZWlUOTh1Nm9FQlEyVm90UkVRZ21YRnRqMlR3WGxWek9yc0JkOVdyYTdlVkc0WGJCYlNhTmRIYmtsc2lHbmV2MXhVUVZ0YkN6NEVGSVFBcnBPYlhqQTdCaUVPd29FWCt1TTdvVWMrMkRQTVdrdzJvU2o2ZEYxUHNYcHExc2s3a1Z6eGIvQXh2OWw3NWk0dEVNOUJrL29RR2JvWUluRFZiL0FxV3VqQVZUanVYS25hL2dpalB3RVpySjN0bnl2d1lNMUpmZThOS3NIRjVhYTcxWUJ4YXRSbVRZMnNUSTJhUlQ2Y0Y5RklDNlFUYktQR2UrV0FueWpMRTJwTlZhZU0wYWx4aUFybi9vS3ZyS2F5cjlBQjZnMkw0KzA2MG45Vy9VamdhemxFTUh4a0ZNOGVMdVV2VXZYMnQveU1SbHRpcWlKQzRoOWNKTlN3VWtkMGhTbUd2SDBycVhmbnEzK3AwVlJpZFZXQkU1R3JoTU9sTWFNL2l6UVdHSEFoaW5UeWp5SGFEdz09IiwibWFwTWQiOiJleUprWlhacFkyVmZkWE5sY2w5a2FXTjBhVzl1WVhKNUlqcGJYU3dpWkdWMmFXTmxYM0psWjJsemRISmhkR2x2Ymw5a1lYUmhJanA3SW5OdlpuUjNZWEpsWDNabGNuTnBiMjRpT2lJeEluMHNJbUZ3Y0Y5cFpHVnVkR2xtYVdWeUlqcDdJbUZ3Y0Y5MlpYSnphVzl1SWpvaU1pNHlMalE0TlRRd055NHdJaXdpWW5WdVpHeGxYMmxrSWpvaVkyOXRMbUZ0WVhwdmJpNWxZMmh2SW4xOSIsImNvZGVfdmVyaWZpZXIiOiJreUJ3ejRyMGpRSWZVOVB4WUF1V2pBWERrczNxOHZFZ204SlN3aldoVlQwIn0=" 
    alexaCookieUtility = newAlexaCookieUtility(
        alexaCredential: new groovy.json.JsonSlurper().parseText(
                (
                    encodedAlexaCredential
                ).decodeBase64()
            )
    )

    alexaCookieUtility.finishOauth(
        response: "https://www.amazon.com/ap/maplanding?openid.assoc_handle=amzn_dp_project_dee_ios&openid.claimed_id=https%3A%2F%2Fwww.amazon.com%2Fap%2Fid%2Famzn1.account.AFN4B34X5Y7FFPGQCQN4ISCA2LHQ&openid.identity=https%3A%2F%2Fwww.amazon.com%2Fap%2Fid%2Famzn1.account.AFN4B34X5Y7FFPGQCQN4ISCA2LHQ&openid.mode=id_res&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.op_endpoint=https%3A%2F%2Fwww.amazon.com%2Fap%2Fsignin&openid.response_nonce=2023-01-05T01%3A39%3A57Z-6219812993597259350&openid.return_to=https%3A%2F%2Fwww.amazon.com%2Fap%2Fmaplanding&openid.signed=assoc_handle%2Cclaimed_id%2Cidentity%2Cmode%2Cns%2Cop_endpoint%2Cresponse_nonce%2Creturn_to%2Cns.pape%2Cpape.auth_policies%2Cpape.auth_time%2Coa2.authorization_code%2Csigned&openid.ns.pape=http%3A%2F%2Fspecs.openid.net%2Fextensions%2Fpape%2F1.0&openid.pape.auth_policies=http%3A%2F%2Fschemas.openid.net%2Fpape%2Fpolicies%2F2007%2F06%2Fmulti-factor&openid.pape.auth_time=2023-01-05T01%3A13%3A27Z&openid.sig=CjJ7Nh%2BLdy8fGe4ta29elOnP7qQG%2FkXbSfxiML0veMo%3D&serial=&openid.oa2.authorization_code=ANONOPrAgoybaMZTbuFwIvye&openid.ns.oa2=http%3A%2F%2Fwww.amazon.com%2Fap%2Fext%2Foauth%2F2"
    )



    // message += "deserializedAlexaCredential               : " + deserializedAlexaCredential + "\n";
    // def x = new groovyx.net.http.URIBuilder("https://blarg.foo")



    /*
    #%%
    python <<-'    EOL'
		if True:
            import base64, hashlib; 

            def base64URLEncode(x: bytes) -> str:
                return base64.urlsafe_b64encode(x).decode('ASCII').replace("=","")

            def sha256(x: str) -> bytes:
                m = hashlib.sha256()
                m.update(x.encode('ASCII'))
                return m.digest()

            r=base64.b64decode('MiTgMov8r9NxQJOq5xbvsSGBAjtywj/YE2c0WtEB9e8='); 
            code_verifier = base64URLEncode(r)
            code_challenge = base64URLEncode(sha256(code_verifier))
            print(f"code_verifier: {code_verifier}")
            print(f"code_challenge: {code_challenge}")

    EOL
    #>>>     code_verifier: MiTgMov8r9NxQJOq5xbvsSGBAjtywj_YE2c0WtEB9e8
    #>>>     code_challenge: xg-n8Xk4Ll9wf_LQnSTlFp2ePfnff1PCwxDX-mmT_3w
    #%%
    */
    // message += "r.digest('SHA1') : " + r.digest('SHA1')  + "\n";

    // message += org.codehaus.groovy.runtime.EncodingGroovyMethods.encodeBase64(r).toString() + "\n";
    // message += org.codehaus.groovy.runtime.EncodingGroovyMethods.digest(base64Encode(r), "SHA-256") + "\n";

    // message += (java.security.MessageDigest.getInstance("SHA-256").digest(r)).toString() + "\n";
    // message += (java.security.MessageDigest.getInstance("SHA-256").digest(codeVerifier.bytes)).toString() + "\n";
    // message += "ahoy there " + "\n";
    // // message += "r.encodeBase64Url().toString(): " + Base64.getEncoder().encode(r).toString() + "\n";
    // // message += "r.encodeBase64().toString(): " + r.encodeBase64().toString().replace('+','-').replace('/','_').replace('=','') + "\n";

    // message += "base64UrlEncode(r): " + base64UrlEncode(r) + "\n";

   return message;
}


def mainTestCode(){
	def message = ""
	message += "\n\n";

    
    Closure uriToQueryMap = {String uriString ->
        // takes a uri as an argument.  Returns a map
        // that represents the query part of the url.
        // only the one value (typically the last, but this is not guaranteed) for any given key is represented in the map.
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
    
    uriString = "https://foo.bar?a=100&b=great%20god=thisis=not=supposed=to=happen&c=5&a=3"
    uriString = "https://foo.com:8888/a/b/c?x=0&y=3&z=blarg%20yarg&y=3"
    java.net.URI uri = new java.net.URI(uriString)
    String rawQuery = uri.getRawQuery()
    x = rawQuery.split("&").collectEntries{
            x=it.split("=",2)
            [
                (java.net.URLDecoder.decode(x[0])): 
                    java.net.URLDecoder.decode(x.length > 1 ? x[1] : "")
            ]
        }

    message += "uriToQueryMap(uriString): ${uriToQueryMap(uriString)}" + "\n"
    message += "uriToQueryMap(uriString)[\"zzzz\"]: ${uriToQueryMap(uriString)["zzzz"]}" + "\n"
    message += "rawQuery.split(\"&\"): ${rawQuery.split("&")}" + "\n"
    message += "x: ${x}" + "\n"
    message += "uriString.split(\"=\",2): ${uriString.split("=",2)}" + "\n"
    // message += "${"sdfgsdfgsdfgh".split("=",2)[1]}" + "\n"
    // message += "${"sdfgsdfgsdfgh".split("=",2).getAt(1)}" + "\n"
    message += "${"sdfgsdfgsdfgh".split("=",2).length}" + "\n"
    message += "${["sdfgsdfgsdfgh"][1]}" + "\n"

    y = [100,101,102,103,104,105]
    z = [100]
    // message += "${y[1..-1]}" + "\n"
    // message += "${z[1..-1]}" + "\n"
    // message += (rawQuery.split("&")).toString() + "\n"
    // message += (uriToQueryMap("https://foo.bar?a=100")).toString() + "\n"

   return message;
}


    
preferences {
    page(name: "pageOne")
    page(name: "pageTwo")
}
 
def pageOne(){
    dynamicPage(name: "pageOne", title: "Alexa Cookie and Country selections", nextPage: "pageTwo", uninstall: true) {
        section("Please Enter your alexa.amazon.com 'cookie' file string here (end with a semicolon)") {
            input("alexaCookie", "text", title: "Raw or edited Cookie", submitOnChange: true, required: true)
        }
        if(alexaCookie != null && alexaCookie.contains("Cookie: ")){
            def finalForm
            def preForm = alexaCookie.split("Cookie: ")
            if(preForm.size() > 1) finalForm = preForm[1]?.replace("\"", "") + ";"
            app.updateSetting("alexaCookie",[type:"text", value: finalForm])
        }
        section(hideable:true, hidden:true, "Settings for automatic cookie refresh") {
            paragraph(removeCosmeticHeredocWhitespace("""
                In order to send commands to Amazon's servers (which in turn relay those
                commands to your Alexa device(s) and thereby cause your Alexa device(s) to
                speak), this app needs to have a valid cookie -- a string that this app sends
                along with each transmission to Amazon's servers. Amazon's servers, by design,
                require a valid cookie to be sent with each transmission in order to know who
                the request is coming from, and in order to know which Amazon account the
                request is related to. A computer program (like this app) obtains a valid cookie
                by communicating with Amazon's servers, which will send a valid cookie to the
                computer program once the computer program has convinced Amazon's servers to do
                so. 

                There are two ways for a computer program to convince Amazon's servers to cough
                up a valid cookie: 
                <dl>
                    <dt>method 1</dt><dd>by sending, to Amazon's servers, your Amazon username and password</dd>
                    <dt>method 2</dt><dd>by sending, to Amazon's servers, an existing valid cookie, along with a few other parameters generated while obtaining the last valid cookie.</dd>
                </dl>
                We use the term <q>Alexa cookie refresh options</q> to denote a data structure
                containing the existing valid cookie and the aforementioned &apos;few other
                parameters&apos;.  An <q>Alexa cookie refresh options</q> object contains all
                the information that must be sent to Amazon's servers to obtain a valid cookie
                using method 2.  This app stores the <q>Alexa cookie refresh options</q> object
                as a string.

                Each cookie expires about 2 weeks after Amazon's servers issue it. Therefore, in
                order for this app to be able to communciate with Amazon's servers indefinitely,
                this app needs to periodically &apos;refresh&apos; its stored cookie, obtaining
                a fresh cookie from Amazon's servers before its current cookie expires. Due to
                limitations inherent in the Groovy environment in which this app runs (and due
                to the inherent technical difficulty of the problem), this app is not capable of
                obtaining a valid cookie using method 1; in other words, this app is not capable
                of using your Amazon username and password to obtain a valid cookie. However,
                this app is capable of obtaining a valid cookie via method 2; once this app has
                a valid <q>Alexa cookie refresh options</q> string, it should, in principle be
                able to keep itself supplied with a fresh cookie indefinitely, provided that
                your Hubitat does not remain continuously offline long enough for the current
                cookie to expire. 

                To bootstrap the cookie refresh process when you first install this app, you
                will need to enter a valid <q>Alexa cookie refresh options</q> string into the
                <q>Alexa cookie refresh options</q> field, below.  This can be accomplished by
                means of <a
                href="https://github.com/gabriele-v/hubitat/tree/master/AlexaCookieNodeJs/AlexaCookieNodeJs"
                target="_blank">gabriele-v's AlexaCookieNodeJs program</a>, which you can run on
                your own computer. The AlexaCookieNodeJs program will prompt you for your Amazon
                username and password, will perform the special handshake with Amazon's servers,
                and will then spit out a string suitable for pasting into the Alexa cookie
                refresh options field. 

                If so desired (but by no means a requirement), you can leave gabriele-v's
                AlexaCookieNodeJs program running continuously on your own NodeJS server, and
                punch the server's URL, username, and password into the fields below. The
                AlexaCookieNodeJs program is designed to run as a server and to perform the
                cookie refresh procedure using method 2 on behalf of computer programs (like
                this app) that communicate with it over the network. 

                When this app goes to refresh its cookie (which it does every 6th day at 1:00
                am), this app will first attempt to use a running instance of the
                AlexaCookieNodeJs program at the URL that you supply below to refresh its
                cookie. If this attempt fails (as it will if you enter no URL whatsoever), then
                this app will perform method 2 itself. 
            """))
            input("alexaRefreshURL", "text", title: "NodeJS service URL", required: false)
            input("alexaRefreshUsername", "text", title: "NodeJS service Username (not Amazon one)", required: false)
            input("alexaRefreshPassword", "password", title: "NodeJS service Password (not Amazon one)", required: false)
            input("alexaRefreshOptions", "text", title: "Alexa cookie refresh options", required: false, submitOnChange: true)
            input("alexaRefresh", "bool", title: "Force refresh now? (Procedure will require 5 minutes)", submitOnChange: true)
        }
        if(alexaRefreshOptions == null) {
            unschedule()
        }
        else {
            // Schedule automatic update
            unschedule()
            schedule("0 0 1 1/6 * ? *", refreshCookie) //  Check for updates every 6 days at 1:00 AM
            //Extract cookie from options if cookie is empty
            if(alexaCookie == null){
                app.updateSetting("alexaCookie",[type:"text", value: getCookieFromOptions(alexaRefreshOptions)])
            }
        }
        if(alexaRefresh) {
            refreshCookie()
            app.updateSetting("alexaRefresh",[type:"bool", value: false])
        }
        section("Please choose your country") {
            input "alexaCountry", "enum", multiple: false, required: true, options: getURLs().keySet().collect()
        }
        section("Notification Device") {
            paragraph "Optionally assign a device for error notifications (like when the cookie is invalid or refresh fails)"
            input "notificationDevice", "capability.notification", multiple: false, required: false
        }
        section("Override Switch") {
            paragraph "Optionally assign a switch that will disable voice when turned off"
            input "overrideSwitch", "capability.switch", multiple: false, required: false
        }
        section("App Name") {
            label title: "Optionally assign a custom name for this app", required: false
        }
    }
}

def pageTwo(){
    dynamicPage(name: "pageTwo", title: "Amazon Alexa Device Selection", install: true, uninstall: true) {  
        section("Please select devices to create Alexa TTS child devices for") {
            input "alexaDevices", "enum", multiple: true, required: false, options: getDevices()
        }
        section("") {
            paragraph     "<span style='color:red'>Warning!!\nChanging the option below will delete any previously created child devices!!\n"+
                        "Virtual Container driver v1.1.20181118 or higher must be installed on your hub!!</span>"+
                        "<a href='https://github.com/stephack/Hubitat/blob/master/drivers/Virtual%20Container/Virtual%20Container.groovy' target='_blank'> [driver] </a>"+
                        "<a href='https://community.hubitat.com/t/release-virtual-container-driver/4440' target='_blank'> [notes] </a>"
            input "alexaVC", "bool", title: "Add Alexa TTS child devices to a Virtual Container?"
        }
    }
}

String removeCosmeticHeredocWhitespace(String x) {
    paragraphs = x.split("\\n([ \\t]*\\n)+")
    paragraphs.collect{ it.replaceAll("\\s+"," ").trim() }.join("\n\n")
}

def speakMessage(String message, String device) {
    
    if (overrideSwitch != null && overrideSwitch.currentSwitch == 'off') {
        log.info "${overrideSwitch} is off, AlexaTTS will not speak message '${message}'"
        return
    } 
    
    log.debug "Sending '${message}' to '${device}'"
	sendEvent(name:"speakMessage", value: message, descriptionText: "Sending message to '${device}'")
    if (message == '' || message.length() == 0) {
        log.warn "Message is empty. Skipping sending request to Amazon"
    }
    else {
        atomicState.alexaJSON.devices.any {it->
            if ((it.accountName == device) || (device == "All Echos")) {
                //log.debug "${it.accountName}"
                //log.debug "${it.deviceType}"
                //log.debug "${it.serialNumber}"
                //log.debug "${it.deviceOwnerCustomerId}"

                try{
                    def SEQUENCECMD = "Alexa.Speak"
                    def DEVICETYPE = "${it.deviceType}"
                    def DEVICESERIALNUMBER = "${it.serialNumber}"
                    def MEDIAOWNERCUSTOMERID = "${it.deviceOwnerCustomerId}"
                    def LANGUAGE = getURLs()."${alexaCountry}".Language
                    
                    def command = ""
                    if (device == "All Echos") { 
                      //command = "{\"behaviorId\":\"PREVIEW\",\"sequenceJson\":\"{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.Sequence\\\",\\\"startNode\\\":{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode\\\",\\\"operationPayload\\\":{\\\"customerId\\\":\\\"${MEDIAOWNERCUSTOMERID}\\\",\\\"expireAfter\\\":\\\"PT5S\\\",\\\"content\\\":[{\\\"locale\\\":\\\"${LANGUAGE}\\\",\\\"display\\\":{\\\"title\\\":\\\"AlexaTTS\\\",\\\"body\\\":\\\"${message}\\\"},\\\"speak\\\":{\\\"type\\\":\\\"text\\\",\\\"value\\\":\\\"${message}\\\"}}],\\\"target\\\":{\\\"customerId\\\":\\\"${MEDIAOWNERCUSTOMERID}\\\"}},\\\"type\\\":\\\"AlexaAnnouncement\\\"}}\",\"status\":\"ENABLED\"}"
                        command = "{\"behaviorId\":\"PREVIEW\",\
                                    \"sequenceJson\":\"{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.Sequence\\\",\
                                                        \\\"startNode\\\":{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode\\\",\
                                                                           \\\"operationPayload\\\":{\\\"customerId\\\":\\\"${MEDIAOWNERCUSTOMERID}\\\",\
                                                                           \\\"expireAfter\\\":\\\"PT5S\\\",\
                                                                           \\\"content\\\":[{\\\"locale\\\":\\\"${LANGUAGE}\\\",\
                                                                                             \\\"display\\\":{\\\"title\\\":\\\"AlexaTTS\\\",\
                                                                                                              \\\"body\\\":\\\"${message}\\\"},\
                                                                                                              \\\"speak\\\":{\\\"type\\\":\\\"text\\\",\
                                                                                                                             \\\"value\\\":\\\"${message}\\\"}}],\
                                                                           \\\"target\\\":{\\\"customerId\\\":\\\"${MEDIAOWNERCUSTOMERID}\\\"}},\
                                                                           \\\"type\\\":\\\"AlexaAnnouncement\\\"}}\",\
                                    \"status\":\"ENABLED\"}"
                    }
                    else {
                      //command = "{\"behaviorId\":\"PREVIEW\",\"sequenceJson\":\"{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.Sequence\\\",\\\"startNode\\\":{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode\\\",\\\"type\\\":\\\"${SEQUENCECMD}\\\",\\\"operationPayload\\\":{\\\"deviceType\\\":\\\"${DEVICETYPE}\\\",\\\"deviceSerialNumber\\\":\\\"${DEVICESERIALNUMBER}\\\",\\\"locale\\\":\\\"${LANGUAGE}\\\",\\\"customerId\\\":\\\"${MEDIAOWNERCUSTOMERID}\\\"${TTS}}}}\",\"status\":\"ENABLED\"}"
                        command = "{\"behaviorId\":\"PREVIEW\",\
                                    \"sequenceJson\":\"{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.Sequence\\\",\
                                                        \\\"startNode\\\":{\\\"@type\\\":\\\"com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode\\\",\
                                                        \\\"type\\\":\\\"${SEQUENCECMD}\\\",\
                                                        \\\"operationPayload\\\":{\\\"deviceType\\\":\\\"${DEVICETYPE}\\\",\
                                                                                  \\\"deviceSerialNumber\\\":\\\"${DEVICESERIALNUMBER}\\\",\
                                                                                  \\\"locale\\\":\\\"${LANGUAGE}\\\",\
                                                                                  \\\"customerId\\\":\\\"${MEDIAOWNERCUSTOMERID}\\\",\
                                                                                  \\\"textToSpeak\\\":\\\"${message}\\\"}}}\",\
                                    \"status\":\"ENABLED\"}"
                    }
                    
                    def csrf = (alexaCookie =~ "csrf=(.*?);")[0][1]

                    def params = [uri: "https://" + getURLs()."${alexaCountry}".Alexa + "/api/behaviors/preview",
                                  headers: ["Cookie":"""${alexaCookie}""",
                                            "Referer": "https://" + getURLs()."${alexaCountry}".Amazon + "/spa/index.html",
                                            "Origin": "https://" + getURLs()."${alexaCountry}".Amazon,
                                            "csrf": "${csrf}",
                                            "Connection": "keep-alive",
                                            "DNT":"1"],
                                          //requestContentType: "application/json",
                                            contentType: "text/plain",
                                            body: command
                                ]
    				//log.debug "Command = ${params}"

                    httpPost(params) { resp ->
                        //log.debug resp.contentType
                        //log.debug resp.status
                        //log.debug resp.data   
                        if (resp.status != 200) {
                            log.error "'speakMessage()':  httpPost() resp.status = ${resp.status}"
                            notifyIfEnabled("Alexa TTS: Please check your cookie!")
                        }
                    }
                }
               catch (groovyx.net.http.HttpResponseException hre) {
                    //Noticed an error in parsing the http response.  For now, catch it to prevent errors from being logged
                    if (hre.getResponse().getStatus() != 200) {
                        log.error "'speakMessage()': Error making Call (Data): ${hre.getResponse().getData()}"
                        log.error "'speakMessage()': Error making Call (Status): ${hre.getResponse().getStatus()}"
                        log.error "'speakMessage()': Error making Call (getMessage): ${hre.getMessage()}"
                        if (hre.getResponse().getStatus() == 400) {
                            notifyIfEnabled("Alexa TTS: ${hre.getResponse().getData()}")
                        }
                        else {
                            notifyIfEnabled("Alexa TTS: Please check your cookie!")
                        }
                    }
                }
                catch (e) {
                    log.error "'speakMessage()': error = ${e}"
                    //log.error "'speakMessage()':  httpPost() resp.contentType = ${e.response.contentType}"
                    notifyIfEnabled("Alexa TTS: Please check your cookie!")
                }

                return true
            }
        }
    }
}


def getDevices() {
    if (alexaCookie == null) {log.debug "No cookie yet"
                              return}   
    try{
        def csrf = (alexaCookie =~ "csrf=(.*?);")[0][1]
        def params = [uri: "https://" + getURLs()."${alexaCountry}".Alexa + "/api/devices-v2/device?cached=false",
                      headers: ["Cookie":"""${alexaCookie}""",
                                "Referer": "https://" + getURLs()."${alexaCountry}".Amazon + "/spa/index.html",
                                "Origin": "https://" + getURLs()."${alexaCountry}".Amazon,
                                "csrf": "${csrf}",
                                "Connection": "keep-alive",
                                "DNT":"1"],
                      requestContentType: "application/json; charset=UTF-8"
                     ]
 
       httpGet(params) { resp ->
            //log.debug resp.contentType
            //log.debug resp.status
            //log.debug resp.data
            if ((resp.status == 200) && (resp.contentType == "application/json")) {
                def validDevices = ["All Echos"]
                atomicState.alexaJSON = resp.data
                //log.debug state.alexaJSON.devices.accountName
                atomicState.alexaJSON.devices.each {it->
                    if (it.deviceFamily in ["ECHO", "ROOK", "KNIGHT", "THIRD_PARTY_AVS_SONOS_BOOTLEG", "TABLET"]) {
                        //log.debug "${it.accountName} is valid"
                        validDevices << it.accountName
                    }
                    if (it.deviceFamily == "THIRD_PARTY_AVS_MEDIA_DISPLAY" && it.capabilities.contains("AUDIBLE")) {
                        validDevices << it.accountName
                    }
                }
                log.debug "getDevices(): validDevices = ${validDevices}"
                return validDevices
            }
            else {
                log.error "Encountered an error. http resp.status = '${resp.status}'. http resp.contentType = '${resp.contentType}'. Should be '200' and 'application/json'. Check your cookie string!"
                notifyIfEnabled("Alexa TTS: Please check your cookie!")
                return "error"
            }
        }
    }
    catch (e) {
        log.error "getDevices: error = ${e}"
        notifyIfEnabled("Alexa TTS: Please check your cookie!")
    }
}


private void createChildDevice(String deviceName) {
    log.debug "'createChildDevice()': Creating Child Device '${deviceName}'"
        
    try {
        def child = addChildDevice("ogiewon", "Child Alexa TTS", "AlexaTTS${app.id}-${deviceName}", null, [name: "AlexaTTS-${deviceName}", label: "AlexaTTS ${deviceName}", completedSetup: true]) 
    } catch (e) {
           log.error "Child device creation failed with error = ${e}"
    }
}

def installed() {
    log.debug "'Installed()' called with settings: ${settings}"
    updated()
}

def uninstalled() {
    log.debug "'uninstalled()' called"
    childDevices.each { deleteChildDevice(it.deviceNetworkId) }
}

def getURLs() {
    def URLs = ["United States": [Alexa: "pitangui.amazon.com", Amazon: "alexa.amazon.com", Language: "en-US"], 
                "Canada": [Alexa: "alexa.amazon.ca", Amazon: "alexa.amazon.ca", Language: "en-US"], 
                "United Kingdom": [Alexa: "layla.amazon.co.uk", Amazon: "amazon.co.uk", Language: "en-GB"], 
                "Italy": [Alexa: "alexa.amazon.it", Amazon: "alexa.amazon.it", Language: "it-IT"],
                "Australia": [Alexa: "alexa.amazon.com.au", Amazon: "alexa.amazon.com.au", Language: "en-AU"],
                "Brazil": [Alexa: "alexa.amazon.com.br", Amazon: "alexa.amazon.com.br", Language: "pt-BR"]]
    return URLs
}

def updated() {
    log.debug "'updated()' called"
    //log.debug "'Updated' with settings: ${settings}"
    //log.debug "AlexaJSON = ${atomicState.alexaJSON}"
    //log.debug "Alexa Devices = ${atomicState.alexaJSON.devices.accountName}"
    
    def devicesToRemove
    if(alexaVC) {
        devicesToRemove = getChildDevices().findAll{it.typeName == "Child Alexa TTS"}
        if(devicesToRemove) purgeNow(devicesToRemove)
        settings.alexaDevices.each {alexaName->
                createContainer(alexaName)
        }
    }
    else {
        devicesToRemove = getChildDevices().findAll{it.typeName == "Virtual Container"}
        if(devicesToRemove) purgeNow(devicesToRemove)
        
        try {
            settings.alexaDevices.each {alexaName->
                def childDevice = null
                if(childDevices) {
                    childDevices.each {child->
                        if (child.deviceNetworkId == "AlexaTTS${app.id}-${alexaName}") {
                            childDevice = child
                            //log.debug "Child ${app.label}-${alexaName} already exists"
                        }
                    }
                }
                if (childDevice == null) {
                    createChildDevice(alexaName)
                    log.debug "Child ${app.label}-${alexaName} has been created"
                }
            }
        }
        catch (e) {
            log.error "Error in updated() routine, error = ${e}"
        }
    }
}
 
def purgeNow(devices){
    log.debug "Purging: ${devices}"
    devices.each { deleteChildDevice(it.deviceNetworkId) }
}

def createContainer(alexaName){
    def container = getChildDevices().find{it.typeName == "Virtual Container"}
    if(!container){
        log.info "Creating Alexa TTS Virtual Container"
        try {
            container = addChildDevice("stephack", "Virtual Container", "AlexaTTS${app.id}", null, [name: "AlexaTTS-Container", label: "AlexaTTS Container", completedSetup: true]) 
        } catch (e) {
            log.error "Container device creation failed with error = ${e}"
        }
        createVchild(container, alexaName)
    }
    else {createVchild(container, alexaName)}
}

def createVchild(container, alexaName){
    def vChildren = container.childList()
    if(vChildren.find{it.data.vcId == "${alexaName}"}){
        log.info alexaName + " already exists...skipping"
    }
    else {
        log.info "Creating TTS Device: " + alexaName
        try{
            container.appCreateDevice("AlexaTTS ${alexaName}", "Child Alexa TTS", "ogiewon", "${alexaName}")
        }
        catch (e) {
            log.error "Child device creation failed with error = ${e}"
        }
    }
}

def initialize() {
    log.debug "'initialize()' called"
}

private def getCookieFromOptions(options) {
    try
    {
        def cookie = new groovy.json.JsonSlurper().parseText(options)
        if (!cookie || cookie == "") {
            log.error("'getCookieFromOptions()': wrong options format!")
            notifyIfEnabled("Alexa TTS: Error parsing cookie, see logs for more information!")
            return ""
        }
        cookie = cookie.localCookie.replace('"',"")
        if(cookie.endsWith(",")) {
            cookie = cookie.reverse().drop(1).reverse()
        }
        cookie += ";"
        log.info("Alexa TTS: new cookie parsed succesfully")
        return cookie
    }
    catch(e)
    {
        log.error("'getCookieFromOptions()': error = ${e}")
        notifyIfEnabled("Alexa TTS: Error parsing cookie, see logs for more information!")
        return ""
    } 
}

def refreshCookie() {
    log.info("Alexa TTS: starting cookie refresh procedure")
    try {
        def authHeaders = ""
        if(alexaRefreshUsername != "")
            authHeaders = "Basic " + (alexaRefreshUsername + ":" + alexaRefreshPassword).bytes.encodeBase64().toString() + "}"
        def params =[
            uri: alexaRefreshURL,
            headers: [
                "Authorization":"${authHeaders}",
                "Connection": "keep-alive",
                "DNT":"1"
            ],
            requestContentType: "application/json; charset=UTF-8",
            body: alexaRefreshOptions
        ]

       httpPost(params) { resp ->
            if ((resp.status == 200)) {
                //log.debug resp.contentType
                //log.debug resp.status
                //log.debug resp.data
                def respGuid = resp.data.toString()
                log.info("Alexa TTS: Request for new cookie sent succesfully, guid: " + respGuid)
                runIn(60*5, getCookie, [data: [guid: respGuid]])
            }
            else {
                log.error "Encountered an error. http resp.status = '${resp.status}'. http resp.contentType = '${resp.contentType}'. Should be '200' and 'application/json; charset=utf-8'"
                notifyIfEnabled("Alexa TTS: Error sending request for cookie refresh, see logs for more information!")
                refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
                return "error"
            }
       }
    }
    catch (groovyx.net.http.HttpResponseException hre) {
        // Noticed an error in parsing the http response
        if (hre.getResponse().getStatus() != 200) {
            log.error "'refreshCookie()': Error making Call (Data): ${hre.getResponse().getData()}"
            log.error "'refreshCookie()': Error making Call (Status): ${hre.getResponse().getStatus()}"
            log.error "'refreshCookie()': Error making Call (getMessage): ${hre.getMessage()}"
            if (hre.getResponse().getStatus() == 400) {
                notifyIfEnabled("Alexa TTS: ${hre.getResponse().getData()}")
            }
            else {
                notifyIfEnabled("Alexa TTS: Error sending request for cookie refresh, see logs for more information!")
            }
        }
        refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
    }
    catch (e) {
        log.error "'refreshCookie()': error = ${e}"
        notifyIfEnabled("Alexa TTS: Error sending request for cookie refresh, see logs for more information!")
        refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
    }
}  
def getCookie(data){
    log.info("Alexa TTS: starting cookie download procedure")
    if(!data.guid || data.guid == "") {
        log.error "'getCookie()': error = guid not provided!"
        notifyIfEnabled("Alexa TTS: Error downloading cookie, see logs for more information!")
        refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
        return "error"
    }
    try {
        def authHeaders = ""
        if(alexaRefreshUsername != "")
            authHeaders = "Basic " + (alexaRefreshUsername + ":" + alexaRefreshPassword).bytes.encodeBase64().toString() + "}"
        def params =[
            uri: alexaRefreshURL,
            headers: [
                "Authorization":"${authHeaders}",
                "Connection": "keep-alive",
                "DNT":"1"
            ],
            requestContentType: "application/json; charset=UTF-8",
            query: [guid: data.guid]
        ]

       httpGet(params) { resp ->
            //log.debug resp.contentType
            //log.debug resp.status
            //log.debug resp.data
            if ((resp.status == 200) && (resp.contentType == "application/json")) {
                //If saved directly as resp.data then double quotes are stripped
                def newOptions = new groovy.json.JsonBuilder(resp.data).toString()
                app.updateSetting("alexaRefreshOptions",[type:"text", value: newOptions])
                log.info("Alexa TTS: cookie downloaded succesfully")
                app.updateSetting("alexaCookie",[type:"text", value: getCookieFromOptions(newOptions)])
				sendEvent(name:"GetCookie", descriptionText: "New cookie downloaded succesfully")
            }
            else {
                log.error "Encountered an error. http resp.status = '${resp.status}'. http resp.contentType = '${resp.contentType}'. Should be '200' and 'application/json; charset=utf-8'"
                notifyIfEnabled("Alexa TTS: Error downloading cookie, see logs for more information!")
                refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
                return "error"
            }
       }
    }
    catch (groovyx.net.http.HttpResponseException hre) {
        // Noticed an error in parsing the http response
        if (hre.getResponse().getStatus() != 200) {
            log.error "'getCookie()': Error making Call (Data): ${hre.getResponse().getData()}"
            log.error "'getCookie()': Error making Call (Status): ${hre.getResponse().getStatus()}"
            log.error "'getCookie()': Error making Call (getMessage): ${hre.getMessage()}"
            if (hre.getResponse().getStatus() == 400) {
                notifyIfEnabled("Alexa TTS: ${hre.getResponse().getData()}")
            }
            else {
                notifyIfEnabled("Alexa TTS: Error downloading cookie, see logs for more information!")
            }
        }
        refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
    }
    catch (e) {
        log.error "'getCookie()': error = ${e}"
        notifyIfEnabled("Alexa TTS: Error dowloading cookie, see logs for more information!")
        refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
    }
}
def notifyIfEnabled(message) {
    if (notificationDevice) {
        notificationDevice.deviceNotification(message)
    }
}

/** refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()
 *  This function is invoked by the logic within the refreshCookie() and getCookie() functions
 *  whenever we encounter a failure while attempting to refresh the cookie by means of the NodeJS server.
 *  Provided that the current value of alexaRefreshOptions is valid, and contains an unexpired cookie,
 *  this function will refresh the cookie without relying on an external NodeJS server.
 */
def refreshAlexaCookieWithoutRelyingOnTheNodeJsServer() {
    log.info("Alexa TTS: starting the cookie refresh procedure that does not rely on the Node JS server")
    try {
// #import "alexa_cookie_utility.groovy"
        alexaCookieUtility = newAlexaCookieUtility(
            logger          : {log.debug("refreshCookie: " + it + "\n");},
            alexaCredential : (new groovy.json.JsonSlurper()).parseText(alexaRefreshOptions)
        )

        alexaCookieUtility.refreshAlexaCookie(
            callback: {String error, Map result ->
                if(error){
                    log.debug("error string that resulted from attempting to refresh the alexa cookie: " + error + "\n");
                    notifyIfEnabled("Alexa TTS: Error refreshing cookie, see logs for more information!");
                } else if(!result){
                    log.debug("alexaCookieUtility.refreshAlexaCookie did not return an explicit error, but returned a null result." + "\n");
                    notifyIfEnabled("Alexa TTS: Error refreshing cookie, see logs for more information!");
                } else {
                    log.debug("alexaCookieUtility.refreshAlexaCookie returned the following succesfull result: " + "\n" + groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(result)) + "\n");
                    def newOptions = new groovy.json.JsonBuilder(result).toString()
                    app.updateSetting("alexaRefreshOptions",[type:"text", value: newOptions])
                    log.info("Alexa TTS: cookie downloaded succesfully")
                    app.updateSetting("alexaCookie",[type:"text", value: getCookieFromOptions(newOptions)])
                    sendEvent(name:"GetCookie", descriptionText: "New cookie downloaded succesfully")
                }
            }
        );
    }
    catch (e) {
        log.error "'refreshAlexaCookieWithoutRelyingOnTheNodeJsServer()': error = ${e}"
        notifyIfEnabled("Alexa TTS: Error refreshing the cookie locally, see logs for more information!")
    }
}

//TODO: insert calls to refreshAlexaCookieWithoutRelyingOnTheNodeJsServer() in the various error handling statements in refreshCookie() and getCookie(), above, so that,
// if the default cookie retrieval process fails in any way, we will then attempt the refreshAlexaCookieWithoutRelyingOnTheNodeJsServer() procedure as a fallback.
 


//==========  WE DO ALL OUR INCLUDES AT THE BOTTOM IN ORDER TO PRESERVE THE MEANINGFULLNESS OF 
// LINE NUMBERS IN WARNING MESSAGES THROWN BY THE HUBITAT (AT LEAST IF THE WARNING MESSAGES ARE COMPLAINING
// ABOUT THINGS HAPPENING IN THE MAIN CODE, ABOVE THIS POINT).
#include "debugging.lib.groovy"
#include "alexa_cookie_utility.groovy"