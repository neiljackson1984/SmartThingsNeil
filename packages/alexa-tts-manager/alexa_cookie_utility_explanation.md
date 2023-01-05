2022-10-22

All of the functions in the alexa cookie utility are essentially in the business
of maintaining and updating a single persistent store of state that I will call
"alexaRefreshOptions" (abbreviated ``aro``) (although, confusingly, this same store
of state goes by many other names in Appollon's code).  alexaRefreshOptions may
be thought of as a json object, and actually is a map from String to String.

There seems to be some ambiguity in the meaning of "cookie".  In an http
request, there is at most one header named "cookie", whose value is a
semicolon-delimeted list of name-value pairs.  I will use the singular "cookie"
to denote a single name-value pair within this list, and I will use the word
"cookieList" to denote the whole list.  The "cookie" header really ought to be
called "cookies" or "cookieList".  Some people, probably confused by the
singular name of the request header, seem to occasionally call the whole list a
(single) cookie.

We probably should regard a "cookie" in the fullest sense as being not just a
name-value pair, but also containing some metadata, like domain name and
expiration date.  The metadata is not included explicitly in the value of the
"cookie" request header, but the metadata is (or can be) specified within each
of the "set-cookie" response headers that the server sends to the client, and
the client typically saves the metadata in its cookie jar.

A "cookie jar" is a collection of cookies (including the aforementioned
metadata), maintained and persistently stored by the http client.  When the http
client goes to make an http request for some url, the client forms the
cookieList for that request by looking through its cookieJar for relevant
cookies, and then forming the semicolon-delimited list of name-value pairs to be
submitted as the value of the ``cookie`` request header.  

One extremely simple cookie jar implementation might be to store a list of
name-value pairs with no metadata, possibly as a single semicolon-delimited
string in exactly the same form as the value of a ``cookie`` request header.  The
client would submit the entire list of name-value pairs in every http request to
any url.  aro.loginCookie and aro.localCookie serve as precisely this sort of
ultra-simple cookie jar.


- ``aro.loginCookie`` 

    This is an ultra-simple cookie jar that the alexa cookie utility uses for
    all of its http requests.

- ``aro.localCookie``

    This is an ultra-simple cookie jar.  This is the proverbial "alexa cookie"
    that the end user of the alexa cookie utility needs in order to talk to the
    alexa api.

- ``aro.csrf``





In Appollon's alexa-cookie.js, unpacking the callbacks, ignoring the
error-handling branches, and ignoring anything other than named function calls
 and http requests, and not bothering to document what values the functions to
 pass to one another (i.e.  assuming that all functions are operating on some
 shared global state), and ignoring the non-proxy strategy for initial cookie
 generation, and omitting the http request parameters, we have  the following
 program skeleton.

- public generateAlexaCookie():
    - initConfig()
    - initAmazonProxy()
    - handleTokenRegistration()

- public refreshAlexaCookie():
    - initConfig()
    - httpPost("https://api.{baseAmazonPage}/auth/token")
    - getLocalCookies()
    - handleTokenRegistration()

- private handleTokenRegistration():
    - httpPost("https://api.{baseAmazonPage}/auth/register")
    - httpGet("https://alexa.{baseAmazonPage}/api/users/me")
    - getLocalCookies()
    - getCSRFFromCookies()

- private getLocalCookies():
    - httpPost("https://www.{amazonPage}/ap/exchangetoken/cookies")

- private getCSRFFromCookies():
    - for each csrfPathCandidate in csrfPathCandidates until success:
        - httpGet("https://alexa.{amazonPage}/{csrfPathCandidate}")

- private initConfig()

- private initAmazonProxy()
    - userInteractiveHttpGet("https://www.{baseAmazonPage/ap/signin}")







    


