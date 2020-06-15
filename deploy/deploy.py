# def doImports():

try:
    # print("attempting first doImports().")
    # doImports()
    import argparse
    import os
    import re
    # import subprocess
    import json
    import pathlib
    # import urllib.parse
    import time
    import requests
    import http.cookiejar
    import sys
    # print("sys.executable: " + sys.executable)
except ModuleNotFoundError as e:
    print("encountered ModuleNotFoundError exception while attempting to import the needed modules: " + str(e))
    exit(99) # we will, externally, respond to an error code 99 by attempting to install the needed modules (by running pipenv install)
    # import os
    # import subprocess
    # subprocess.check_call(["python", "-m", "pipenv", "install"], cwd=os.path.dirname(os.path.realpath(__file__)))
    # # subprocess.check_call will throw an exception (which we intentionally do not handle) in the case where the command called returns a nonzero exit code.
    # try:
    #     print("attempting second doImports().")
    #     doImports()
    # except Exception as ee:
    #     print("encountered exception while importing after running pipenv install: " + str(e))
    #     exit




# from urllib.parse import urlparse


parser = argparse.ArgumentParser(description="Upload app or driver code to the hubitat")
parser.add_argument("--source", action='store', nargs='?', required=True, help="the file to be uploaded to the hubitat.")
parser.add_argument("--deployInfoFile", "--deploy_info_file", action='store', nargs='?', required=True, help="a json file that looks something like this: " + "\n" + 
    "{" + "\n" + 
    "    \"hubitatIdOfDriverOrApp\"                        : \"225\"," + "\n" + 
    "    \"hubitatIdOfTestInstance\"                       : \"169\"," + "\n" + 
    "    \"typeOfCode\"                                    : \"driver\"," + "\n" + 
    "    \"testEndpoint\"                                  : \"runTheTestCode\"," + "\n" + 
    "    \"urlOfHubitat\"                                  : \"https://toreutic-abyssinian-6502.dataplicity.io\"," + "\n" + 
    "    \"nameOfEventToContainTestEndpointResponse\"      : \"testEndpointResponse\"" + "\n" + 
    "}" +
    ""
)
parser.add_argument("--credentialsDirectory", "--credentials_directory", 
    action='store', 
    nargs='?', 
    required=False, 
    help=
        "a directory in which to store the cookies and tokens used to authenticate to the Hubitat " 
        + "for uploading and for hitting the test endpoint.  We will make this directory automatically if it does not exist."
)

args, unknownArgs = parser.parse_known_args()

print("args: " + str(args))
print("args.source is " + str(args.source))
print("os.getcwd(): " + os.getcwd())
source = pathlib.Path(args.source).resolve()
deployInfoFile = pathlib.Path(args.deployInfoFile).resolve()

credentialStorageFolderPath = (
    pathlib.Path(args.credentialsDirectory) 
    if args.credentialsDirectory
    else pathlib.Path(source.parent,"credentials")
)


cookieJarFilePath           = pathlib.Path(credentialStorageFolderPath, "cookie-jar.txt")                   #   os.path.join(credentialStorageFolder, "cookie-jar.txt")
accessTokenFilePath         = pathlib.Path(credentialStorageFolderPath, "accessTokenForTestInstance.txt")   #   os.path.join(credentialStorageFolder, "accessTokenForTestInstance.txt")

print("str(cookieJarFilePath.resolve()): " + str(cookieJarFilePath.resolve()))


deployInfo = json.load(open(deployInfoFile, 'r'))



if deployInfo['typeOfCode']=="device": deployInfo['typeOfCode']="driver"

if deployInfo['typeOfCode']=="driver" and deployInfo.get('testEndpoint') and isinstance(deployInfo.get('testEndpoint'), str):
    #nameOfEventToContainTestEndpointResponse   = re.search("^.*//////nameOfEventToContainTestEndpointResponse=(.*)\\s*$",                               sourceContents, re.MULTILINE).group(1)
    #to do: ensure that we have a deployInfo.nameOfEventToContainTestEndpointResponse property
    if not ('nameOfEventToContainTestEndpointResponse' in deployInfo and isinstance(deployInfo['nameOfEventToContainTestEndpointResponse'], str)):
        print("The deployInfo says that we are dealing with a driver, and a test endpoint is specified, but there is no 'nameOfEventToContainTestEndpointResponse'.  Please add a 'nameOfEventToContainTestEndpointResponse' property to the deployinfo.")
        quit(1)


print("id of the " + deployInfo['typeOfCode'] + ":" + deployInfo['hubitatIdOfDriverOrApp'])
print("hubitatIdOfTestInstance:" + deployInfo['hubitatIdOfTestInstance'])
print("testEndpoint:" + str(deployInfo.get('testEndpoint')))
print("typeOfCode:" + deployInfo['typeOfCode'])
print("urlOfHubitat:" + deployInfo['urlOfHubitat'])
if deployInfo['typeOfCode']=="driver":
    print("nameOfEventToContainTestEndpointResponse:" + deployInfo['nameOfEventToContainTestEndpointResponse'])


session = requests.Session()
cookieJarFilePath.resolve().parent.mkdir(parents=True, exist_ok=True) 
session.cookies = http.cookiejar.MozillaCookieJar(filename=str(cookieJarFilePath.resolve()))

#ensure that the cookie jar file exists and contains a working cookie to authenticate into the hubitat administrative web interface.
#for now, we will assume that existence of the cookie jar file implies that it contains a working cookie.
if os.path.isfile(session.cookies.filename):
    session.cookies.load(ignore_discard=True)
else:  #we ought to use some more direct and correct test of whether the cookie jar will gain us access rather than simply assuming that if the cookie jar file exists, then it must contain a valid, working cookie.
    #collect username and password from the user
    print("please enter your hubitat username: ")
    hubitatUsername = input()
    print("please enter your hubitat password")
    hubitatPassword = input()
    print("you entered " + hubitatUsername + " and " + hubitatPassword + ".  Thank you.")

    response = session.post(
        deployInfo['urlOfHubitat'] + "/login",
        data={
            'username':hubitatUsername,
            'password':hubitatPassword,
            'submit':'Login'
        }
    )
    # print("cookies: " + str(response.cookies.get_dict()))
    session.cookies.save(ignore_discard=True)



    # completedProcess = subprocess.run(
    #     "curl" + " " +
    #         "\"" + deployInfo['urlOfHubitat'] + "/login" + "\"" + " " +
    #         "--cookie-jar " + "\"" + str(cookieJarFilePath) + "\""  + " " +
    #         "--data-urlencode " + "\"" + "username=" + hubitatUsername + "\""  + " " +
    #         "--data-urlencode " + "\"" + "password=" + hubitatPassword + "\""  + " " +
    #         "--data-urlencode " + "\"" + "submit=Login" + "\""  + " " +
    #         "",
    #     capture_output = True
    # )
    # if completedProcess.returncode != 0 or not os.path.isfile(cookieJarFilePath):
    #     print("the call to curl seems to have failed.")
    #     print(str(completedProcess))
    #     exit


#we have to get the version number of the code currently on the hub, because we will have to submit a related (incremented-by-one) (or maybe just the exact number) version number in our POST to submit the new code
response = session.get(
    url=deployInfo['urlOfHubitat'] + "/" + deployInfo['typeOfCode'] + "/ajax/code",
     params={
         'id': deployInfo['hubitatIdOfDriverOrApp']
     }
)
version = response.json()['version']
print("version: " + str(version))

#upload the source
print("uploading the code...")

with open(source, 'r') as f:
    sourceContents = f.read()

response = session.post(
    url = deployInfo['urlOfHubitat'] + "/" + deployInfo['typeOfCode'] + "/ajax/update",
    data={
        'id': deployInfo['hubitatIdOfDriverOrApp'] ,
        'version': version,
        'source': sourceContents
    }
)
#to do: report the status of the upload to the user
print(response.text)

if(response.json()['status'] == "success"):
    print("uploading succeeded.")
    version = response.json()['version']
    print("version: " + repr(version))
elif (response.json()['status'] == "error"):
    print("uploading failed with the following error message.  Quitting...")
    print(response.json()['errorMessage'])
    quit(2)
else:
    print("uploading failed.  Quitting...")
    quit(2)

if deployInfo.get('testEndpoint'):
    print("hitting the test endpoint (" + deployInfo['testEndpoint'] +  ") ...")
    #hit the test endpoint
    if deployInfo['typeOfCode']=="app":  
        #ensure that the accessToken file exists and contains a working access token 
        #for now we will assume that existence of the access token file implies that it contains a working access token
        if os.path.isfile(accessTokenFilePath):
            with open(accessTokenFilePath, 'r') as f:
                accessToken = f.read()
        else:
            def getClientIdAndClientSecretAssignedToTheApp(deployInfo, session):
                #obtain the client id and client secret assigned to the app (assuming that oauth has been turned on for this app in the hubitat web interface)
                response = session.get(deployInfo['urlOfHubitat'] + "/" + deployInfo['typeOfCode'] + "/editor/" + deployInfo['hubitatIdOfDriverOrApp'])
                print("url: " + response.request.url)
                # print(response.text)
                clientIdMatch = re.search("^.*value=\"([0123456789abcdef-]+)\" id=\"clientId\".*$",         response.text, re.MULTILINE)
                clientSecretMatch = re.search("^.*name=\"clientSecret\" value=\"([0123456789abcdef-]+)\".*$",   response.text, re.MULTILINE)
                # The efficacy of the above regular expressions is highly
                # dependent on the html being formatted in a certain way, which
                # could easily change and break this extraction scheme with a
                # later release of hubitat (regular expressions are not a very
                # robust way of parsing html (and even if we were parsing the
                # html in a more robust way -- the html code is not
                # contractually guaranteed to present the client id and the
                # client secret in a particular machine-readable way --
                # extracting the data from html that is designed to create a
                # human-readable document rather than be a machine readable
                # structure is fragile and prone to break in the future.
                # However, at the moment, I don't know of any better way to
                # obtain the client id and client secret programmatically other
                # than using regexes to search through the html code of the
                # web-based editor page.)
                return (
                    (clientIdMatch.group(1) if clientIdMatch else None),
                    (clientSecretMatch.group(1) if clientSecretMatch else None),
                )

            (clientId, clientSecret) = getClientIdAndClientSecretAssignedToTheApp(deployInfo, session)
            oAuthIsEnabledForTheApp = clientId and clientSecret
            if not oAuthIsEnabledForTheApp:
                print("oAuth is not enabled for the app.  We will now attempt to enable oAuth for the app so that we will be able to hit the test endpoint.")
                #enable oAuth for the app
                # TODO: allow the user to control whether we automatically enable oauth, rather
                # than doing it without asking.
                response = session.post(
                    url=deployInfo['urlOfHubitat'] + "/" + deployInfo['typeOfCode'] + "/edit/update",
                    data={
                        "id":deployInfo['hubitatIdOfDriverOrApp'] ,
                        "version":version,
                        "oauthEnabled":"true",
                        "webServerRedirectUri":"",
                        "displayName":"",
                        "displayLink":"",
                        "_action_update":"Update"      
                    }
                )
                # print("waiting for the setting to sink in.")
                # time.sleep(2)
                (clientId, clientSecret) = getClientIdAndClientSecretAssignedToTheApp(deployInfo, session)
                oAuthIsEnabledForTheApp = clientId and clientSecret
                if not oAuthIsEnabledForTheApp:
                    print("oAuth is still not enabled for this app, so we will not be able to hit the test endpoint.  Quitting...")
                    quit(2)

            print("clientId: " + clientId)
            print("clientSecret: " + clientSecret)

            #now that we have the clientId and clientSecret, we can obtain the authorization code
            dummyRedirectUri = 'abc' #dummy value - it could be anything, as long as it matches between the request to /oauth/confirm_access and the subsequent request to /oauth/token
            response = session.get(deployInfo['urlOfHubitat'] + "/oauth/confirm_access",
                params={
                    'client_id': clientId,
                    'redirect_uri':dummyRedirectUri,
                    'response_type':'code',
                    'scope':'app'
                }
            )
            print("url: " + response.request.url)

            authorizationCode  = re.search("^.*name=\"code\" value=\"(\\w+)\".*$",    response.text, re.MULTILINE).group(1)
            appId              = re.search("^.*name=\"appId\" value=\"(\\w+)\".*$",   response.text, re.MULTILINE).group(1)
            print("appId: " + appId)
            print("authorizationCode: " + authorizationCode)
            #now, we can use the authorizationCode to finally obtain the access token
            response = session.post(
                url=deployInfo['urlOfHubitat'] + "/oauth/token",
                data={
                    "grant_type"    : "authorization_code",
                    "client_id"     : clientId,          
                    "client_secret" : clientSecret,       
                    "code"          : authorizationCode,   
                    "redirect_uri"  : dummyRedirectUri,               
                    "scope"         : "app"               
                }
            )
            accessToken = response.json()['access_token']
            accessTokenFilePath.resolve().parent.mkdir(parents=True, exist_ok=True) 
            with open(accessTokenFilePath, 'w') as f:
                f.write(accessToken)
        # print("accessToken: " + accessToken)
        url = deployInfo['urlOfHubitat'] + "/" + deployInfo['typeOfCode'] + "s" + "/api/" + deployInfo['hubitatIdOfTestInstance'] + "/" + deployInfo['testEndpoint']
        print("attempting to hit: " + url )
        response = session.get(
            url=url,
            headers={'Authorization': "Bearer" + " " + accessToken}
        )
        returnValueFromTestEndpoint = response.text
    elif deployInfo['typeOfCode']=="driver":
        #first, we issue the command (we do the equivalent of clicking the appropriate button in the hubitat administrative web interface)
        response = session.post(
            url=deployInfo['urlOfHubitat'] + "/device/runmethod",
            data={
                'id':deployInfo['hubitatIdOfTestInstance'],
                'method':deployInfo['testEndpoint']
            }
        )

        # print("http response from hitting the test endpoint: " + response.text)

        #then, we look up the value of the most recent even having name deployInfo['nameOfEventToContainTestEndpointResponse']
        response = session.get(
            url=deployInfo['urlOfHubitat'] + "/device/events/" + deployInfo['hubitatIdOfTestInstance'] + "/dataTablesJson",
            params={
                'draw': '1',

                'columns[0][data]': '0',
                'columns[0][name]': 'ID',
                'columns[0][searchable]': 'false',
                'columns[0][orderable]': 'true',
                'columns[0][search][value]': '',
                'columns[0][search][regex]': 'false',

                'columns[1][data]': '1',
                'columns[1][name]': 'NAME',
                'columns[1][searchable]': 'true',
                'columns[1][orderable]': 'true',
                'columns[1][search][value]': deployInfo['nameOfEventToContainTestEndpointResponse'], #this search seems to have no effect
                'columns[1][search][regex]': 'false',

                'columns[2][data]': '2',
                'columns[2][name]': 'VALUE',
                'columns[2][searchable]': 'false', #'true',
                'columns[2][orderable]': 'true',
                'columns[2][search][value]': '',
                'columns[2][search][regex]': 'false',

                'columns[3][data]': '3',
                'columns[3][name]': 'UNIT',
                'columns[3][searchable]': 'false', #'true',
                'columns[3][orderable]': 'true',
                'columns[3][search][value]': '',
                'columns[3][search][regex]': 'false',

                'columns[4][data]': '4',
                'columns[4][name]': 'DESCRIPTION_TEXT',
                'columns[4][searchable]': 'false', #'true',
                'columns[4][orderable]': 'true',
                'columns[4][search][value]': '',
                'columns[4][search][regex]': 'false',

                'columns[5][data]': '5',
                'columns[5][name]': 'SOURCE',
                'columns[5][searchable]': 'false', #'true',
                'columns[5][orderable]': 'true',
                'columns[5][search][value]': '',
                'columns[5][search][regex]': 'false',

                'columns[6][data]': '6',
                'columns[6][name]': 'EVENT_TYPE',
                'columns[6][searchable]': 'false', #'true',
                'columns[6][orderable]': 'true',
                'columns[6][search][value]': '',
                'columns[6][search][regex]': 'false',

                'columns[7][data]': '7',
                'columns[7][name]': 'DATE',
                'columns[7][searchable]': 'false', #'true',
                'columns[7][orderable]': 'true',
                'columns[7][search][value]': '',
                'columns[7][search][regex]': 'false',

                'order[0][column]': '7',
                'order[0][dir]': 'desc',

                'start': '0',
                'length': '1',
                # 'search[value]': '',
                'search[value]': deployInfo['nameOfEventToContainTestEndpointResponse'], # this search is too broad for my purposes.  I want to query events with the specific name, but this search function searches in all event-related text, I think.
                # by setting all the [searchable] entries above to false, except for 'NAME', we limit our search to only the NAME field, whcih is what we want.
                #unfortunately, we will pick up all events whose names  contain the search string.
                # I tried playing around with setting search[regex] to 'true' and then using start-of-string and end-of-string delimeters, but with no luck.
                'search[regex]': 'false',
                '_': str(time.time() - 10000)
                # this appears to be a unix timestamp, but I suspect that it the default value is now (the most recent available events)
                # Actually, I suspect that the only purpose of this is to prevent caching
            }
        )
        eventNamesInTheResultSet = set(
            map(
                lambda x: x[1],
                response.json()['data']
            )
        )
        # print("eventNamesInTheResultSet: " + str(eventNamesInTheResultSet))
        returnValueFromTestEndpoint = response.json()['data'][0][2]
    print(returnValueFromTestEndpoint)    
session.cookies.save(ignore_discard=True)
  


