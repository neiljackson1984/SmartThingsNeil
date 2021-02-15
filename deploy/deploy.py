# def doImports():

from requests import NullHandler


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


parser = argparse.ArgumentParser(description="Deploy a Hubitat package.")
# parser.add_argument("--source", action='store', nargs='?', required=True, help="the file to be uploaded to the hubitat.")
# parser.add_argument("--deployInfoFile", "--deploy_info_file", action='store', nargs='?', required=True, help="a json file that looks something like this: " + "\n" + 
#     "{" + "\n" + 
#     "    \"hubitatIdOfDriverOrApp\"                        : \"225\"," + "\n" + 
#     "    \"hubitatIdOfTestInstance\"                       : \"169\"," + "\n" + 
#     "    \"typeOfCode\"                                    : \"driver\"," + "\n" + 
#     "    \"testEndpoint\"                                  : \"runTheTestCode\"," + "\n" + 
#     "    \"urlOfHubitat\"                                  : \"https://toreutic-abyssinian-6502.dataplicity.io\"," + "\n" + 
#     "    \"nameOfEventToContainTestEndpointResponse\"      : \"testEndpointResponse\"" + "\n" + 
#     "}" +
#     ""
# )
parser.add_argument("--package_info_file", "--packageInfoFile", dest="packageInfoFile" ,action='store', nargs='?', required=True, help=" The file from which to read information about this package. " + "\n" + "")
parser.add_argument("--package_manifest_file", "--packageManifestFile", dest="packageManifestFile" ,action='store', nargs='?', required=True, help=" The package manifest file to be created. " + "\n" + "")

parser.add_argument("--repository_file", "--repositoryFile", dest="repositoryFile" ,action='store', nargs='?', required=True, help=" The hubitat package manager repository file in which to insert the details of this package. " + "\n" + "")
parser.add_argument("--url_root", "--urlRoot", dest="urlRoot" ,action='store', nargs='?', required=True, help="  " + "\n" + "")
parser.add_argument("--local_root", "--localRoot", dest="localRoot" ,action='store', nargs='?', required=True, help="  " + "\n" + "")

parser.add_argument("--credentials_directory","--credentialsDirectory", dest="credentialsDirectory",
    action='store', 
    nargs='?', 
    required=False, 
    help=
        "a directory in which to store the cookies and tokens used to authenticate to the Hubitat " 
        + "for uploading and for hitting the test endpoint.  We will make this directory automatically if it does not exist."
)

parser.add_argument("--build_directory", "--buildDirectory",dest="buildDirectory",
    action='store', 
    nargs='?', 
    required=False, 
    help=
        "the directory in which to look for groovy files"
)

args, unknownArgs = parser.parse_known_args()

print("args: " + str(args))
print("args.packageInfoFile: " + str(args.packageInfoFile))
print("os.getcwd(): " + os.getcwd())
# source = pathlib.Path(args.source).resolve()
# deployInfoFile = pathlib.Path(args.deployInfoFile).resolve()
pathOfPackageInfoFile = pathlib.Path(args.packageInfoFile).resolve()
pathOfRepositoryFile = pathlib.Path(args.repositoryFile).resolve()
pathOfPackageManifestFile = pathlib.Path(args.packageManifestFile).resolve()
pathOfBuildDirectory = pathlib.Path(args.buildDirectory).resolve()
pathOfLocalRoot = pathlib.Path(args.localRoot).resolve()
urlRoot = str(args.urlRoot)

credentialStorageFolderPath = (
    pathlib.Path(args.credentialsDirectory) 
    if args.credentialsDirectory
    else pathlib.Path(pathOfBuildDirectory,"credentials")
)


cookieJarFilePath           = pathlib.Path(credentialStorageFolderPath, "cookie-jar.txt")                   #   os.path.join(credentialStorageFolder, "cookie-jar.txt")
accessTokenFilePath         = pathlib.Path(credentialStorageFolderPath, "accessTokenForTestInstance.txt")   #   os.path.join(credentialStorageFolder, "accessTokenForTestInstance.txt")

print("str(cookieJarFilePath.resolve()): " + str(cookieJarFilePath.resolve()))
print("str(pathOfPackageInfoFile.resolve()): " + str(pathOfPackageInfoFile.resolve()))
print("str(pathOfPackageManifestFile.resolve()): " + str(pathOfPackageManifestFile.resolve()))
print("str(pathOfRepositoryFile.resolve()): " + str(pathOfRepositoryFile.resolve()))
print("str(pathOfLocalRoot.resolve()): " + str(pathOfLocalRoot.resolve()))
print("urlRoot: " + urlRoot)



# deployInfo = json.load(open(deployInfoFile, 'r'))
packageInfo = json.load(open(pathOfPackageInfoFile, 'r'))

#repositoryEntry is the entry that will represent this package within a repository.
repositoryEntry = {
    'id'          : packageInfo['packageId'],
    'name'        : packageInfo['packageName'],
    'category'    : packageInfo['category'],
    'location'    : urlRoot + pathOfPackageManifestFile.relative_to(pathOfLocalRoot).as_posix(),
    # location is the publicly-accessible url to the package's manifest file.

    'description' : packageInfo['description'],
    'tags'        : packageInfo['tags']
}

repository = json.load(open(pathOfRepositoryFile, 'r'))
#update repository with the new/updated repositoryEntry
existingMatchingRepositoryEntry = (list(filter(lambda x: x['id'] == packageInfo['packageId'], repository['packages'])) or (None, ))[0]
if existingMatchingRepositoryEntry: 
    existingMatchingRepositoryEntry.clear()
    existingMatchingRepositoryEntry.update(repositoryEntry)
else:
    repository['packages'].append(repositoryEntry)

json.dump(repository, open(pathOfRepositoryFile, 'w'), indent=4)

print("repositoryEntry['location']: " + repositoryEntry['location'])

packageManifest = {
    'packageName'       : packageInfo['packageName'],
	'author'            : packageInfo['author'],
    'minimumHEVersion'  : packageInfo.get('minimumHEVersion'),
	'version'           : packageInfo.get('version'),
	'dateReleased'      : packageInfo.get('dateReleased'),
	'documentationLink' : packageInfo.get('documentationLink'),
	'communityLink'     : packageInfo.get('communityLink'),
	'releaseNotes'      : packageInfo.get('releaseNotes'),
    'licenseFile'       : packageInfo.get('licenseFile'),
    #the publicly-accessible url of the package's license 

	'apps'              : [],
	'drivers'           : [],
    'files'             : []
}




# print("type(packageInfo): " + str(type(packageInfo)))

#construct packageComponents -- a list of the package components (A component, here, is an app or a driver).
packageComponents = (
    list(map(
        lambda x: {**x, 'typeOfComponent': "driver"},
        packageInfo.get("drivers",[]) 
    ))
    + list(map(
        lambda x: {**x, 'typeOfComponent': "app"},
        packageInfo.get("apps",[]) 
    ))
    + list(map(
        lambda x: {**x, 'typeOfComponent': "file"},
        packageInfo.get("files",[]) 
    ))
)
# print("packageComponents: " + str(packageComponents))




session = requests.Session()
cookieJarFilePath.resolve().parent.mkdir(parents=True, exist_ok=True) 
session.cookies = http.cookiejar.MozillaCookieJar(filename=str(cookieJarFilePath.resolve()))


for packageComponent in packageComponents:
    print("Now processing package component " + packageComponent['typeOfComponent'] + " " + packageComponent['name'])    
    pathOfGroovyFile = pathOfPackageInfoFile.parent.joinpath(packageComponent['file'])
    
    # look for the groovy file in the build directory
    
    
    pathOfPreprocessedGroovyFile = pathOfBuildDirectory.joinpath(packageComponent['file'])
    # note: I have not yet implemented uploading/testing in the case packageComponent['typeOfComponent'] == 'file'
    # the idea is that such components should be added to the hubitat's collection of onboard files.
    pathOfUploadIndicatorFile = pathOfPreprocessedGroovyFile.with_name( pathOfPreprocessedGroovyFile.name + ".upload" )


    if  pathOfPreprocessedGroovyFile.is_file() and pathOfPreprocessedGroovyFile.stat().st_mtime >= pathOfGroovyFile.stat().st_mtime  :
        print("The preprocessed groovy file is up-to-date.  No need to make preprocessed groovy file.")
    else:
        #generate the preprocessed groovy file
        # here is a very hacky way to preserve the escaped newlines (i.e. backslash followed by a newline), which cpp otherwise tenaciously removes:
        # replace a backslash at the end of a line by a bogus (but hopefully globally unique) string, then after preprocessing, replace the bogus string at the end of a line by a backslash. (Yuck, but it gets the job done.)
        # The last sed call gets rid of carriage returns at the end of lines, which cpp tends to insert, even if the original file contained no carriage returns (yuck!). (this is a hack to suit a perticular case where the original file did not have carriage returns and 
        # I do not want to modify the orignal file more than is necessary.
        
        terminalBackslashReplacement="c01a360518214dbf968ccbb383e14601"
        
        command = (
            "cat '" + str(pathOfGroovyFile) +  "'"
            + " | " + "sed --regexp-extended 's/\\\\$/" + terminalBackslashReplacement + "/g'"
            + " | " + "cpp -w -P -C -E -traditional "
            + " | " + "sed --regexp-extended 's/"  + terminalBackslashReplacement + "/\\\\/g'"
            + " | " + "sed --regexp-extended 's/\\r$//g'"
            + " > " + "'" + str(pathOfPreprocessedGroovyFile) + "'"
        )
        print("command: " + command)
        # cat $*.groovy \
		# | sed --regexp-extended 's/\\$$/$(terminalBackslashReplacement)/g' \
		# | cpp -w -P -C -E -traditional \
		# | sed --regexp-extended 's/$(terminalBackslashReplacement)/\\/g' \
		# | sed --regexp-extended 's/\r$$//g'  \
		# > "$(buildDirectory)/$*.groovy"

    continue

    manifestEntry={
        **{
            'id'             : packageComponent['id'],
            'name'           : packageComponent['name'],
            'location'       : urlRoot + pathOfGroovyFile.relative_to(pathOfLocalRoot).as_posix(),
        },
        **({
            'version'        : packageComponent['version'],
            'betaVersion'    : packageComponent.get('betaVersion'),
            'betaLocation'   : packageComponent.get('betaLocation'),
            'required'       : packageComponent['required'],
            'alternateNames' : packageComponent.get('alternateNames') ,
            #alternateNames is expected to be null or a (allowed to be empty?) list, each element of which is a dict of the form {'name': 'foo', 'namespace': 'bar'}
            'namespace'      : packageComponent['namespace']
        } if packageComponent['typeOfComponent'] in ('driver','app') else {} ),
        **({
            'oauth'          : packageComponent.get('oauth'),
            'primary'        : packageComponent.get('primary')
        } if packageComponent['typeOfComponent'] == 'app' else {})
    }

    print("manifestEntry: " + str(manifestEntry))



    packageManifest[
        {
            'app':'apps', 
            'driver':'drivers', 
            'file':'files'
        }[
            packageComponent['typeOfComponent']
        ]
    ].append(manifestEntry)

    # all types of components (i.e. files, drivers, and apps) have the properties: id, name, and location.
    # drivers and apps have additionally, the properties: version, betaVersion, betaLocation, required, alternateNames, namespace, and required.
    # apps have, additionally, the properties: oauth and primary

    deployInfo = packageComponent.get('deployInfo')
    
    if deployInfo:
        if packageComponent['typeOfComponent']=="driver" and deployInfo['testEndpoint'] and isinstance(deployInfo['testEndpoint'], str):
            #nameOfEventToContainTestEndpointResponse   = re.search("^.*//////nameOfEventToContainTestEndpointResponse=(.*)\\s*$",                               sourceContents, re.MULTILINE).group(1)
            #ensure that we have a deployInfo.nameOfEventToContainTestEndpointResponse property
            if not (deployInfo.get('nameOfEventToContainTestEndpointResponse') and isinstance(deployInfo['nameOfEventToContainTestEndpointResponse'], str)):
                print(
                    "The driver entry in the packageInfo file says that we are dealing with a driver and specifies a test endpoint, but there is no 'nameOfEventToContainTestEndpointResponse'.  " 
                    + "Please add a 'nameOfEventToContainTestEndpointResponse' property to the driver entry's 'deployInfo' property in the packageInfo file."
                )
                quit(1)
                # we might want to simply continue to next packageComponent rather than quitting altogether, since this is an error that only affects this one particular component.

        
        print("id of the " + packageComponent['typeOfComponent'] + ":" + deployInfo['hubitatIdOfDriverOrApp'])
        print("hubitatIdOfTestInstance:" + deployInfo['hubitatIdOfTestInstance'])
        print("testEndpoint:" + str(deployInfo.get('testEndpoint')))
        print("typeOfCode:" + packageComponent['typeOfComponent'])
        print("urlOfHubitat:" + deployInfo['urlOfHubitat'])
        if packageComponent['typeOfComponent']=="driver":
            print("nameOfEventToContainTestEndpointResponse:" + deployInfo['nameOfEventToContainTestEndpointResponse'])


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


        #determine whether we need to upload by comparing the timestamp of the groovy file with the timestamp of the uploadIndicatorFile
        # we regard the upload as being up-to-date (i.e. no upload required) iff. the upload indicator file exists and has a modified timestamp 
        # greater than or equal to the modified timestamp of the groovy file.

        if  pathOfUploadIndicatorFile.is_file() and pathOfUploadIndicatorFile.stat().st_mtime >= pathOfPreprocessedGroovyFile.stat().st_mtime  :
            print("The code on the hubitat is up-to-date.  No need to perform the upload.")
        else:
            #we have to get the version number of the code currently on the hub, because we will have to submit a related (incremented-by-one) (or maybe just the exact number) version number in our POST to submit the new code
            response = session.get(
                url=deployInfo['urlOfHubitat'] + "/" + packageComponent['typeOfComponent'] + "/ajax/code",
                params={
                    'id': deployInfo['hubitatIdOfDriverOrApp']
                }
            )
            version = response.json()['version']
            print("version: " + str(version))

            # responseJson = response.json()
            # del responseJson['source']
            # print("response to request to /ajax/code: " + str(responseJson))
            # continue 

            #upload the source
            print("uploading the code...")

            with open(pathOfPreprocessedGroovyFile, 'r') as f:
                sourceContents = f.read()

            response = session.post(
                url = deployInfo['urlOfHubitat'] + "/" + packageComponent['typeOfComponent'] + "/ajax/update",
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
                #touch the ".upload" timestamp-indicator file
                pathOfUploadIndicatorFile.touch()

            elif (response.json()['status'] == "error"):
                print("uploading failed with the following error message.  Quitting...")
                print(response.json()['errorMessage'])
                quit(2)
            else:
                print("uploading failed.  Quitting...")
                quit(2)
        continue
        if deployInfo.get('testEndpoint'):
            print("hitting the test endpoint (" + deployInfo['testEndpoint'] +  ") ...")
            #hit the test endpoint
            if packageComponent['typeOfComponent']=="app":  
                #ensure that the accessToken file exists and contains a working access token 
                #for now we will assume that existence of the access token file implies that it contains a working access token
                if os.path.isfile(accessTokenFilePath):
                    with open(accessTokenFilePath, 'r') as f:
                        accessToken = f.read()
                else:
                    def getClientIdAndClientSecretAssignedToTheApp(deployInfo, session):
                        #obtain the client id and client secret assigned to the app (assuming that oauth has been turned on for this app in the hubitat web interface)
                        response = session.get(deployInfo['urlOfHubitat'] + "/" + packageComponent['typeOfComponent'] + "/editor/" + deployInfo['hubitatIdOfDriverOrApp'])
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
                            url=deployInfo['urlOfHubitat'] + "/" + packageComponent['typeOfComponent'] + "/edit/update",
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
                url = deployInfo['urlOfHubitat'] + "/" + packageComponent['typeOfComponent'] + "s" + "/api/" + deployInfo['hubitatIdOfTestInstance'] + "/" + deployInfo['testEndpoint']
                print("attempting to hit: " + url )
                response = session.get(
                    url=url,
                    headers={'Authorization': "Bearer" + " " + accessToken}
                )
                returnValueFromTestEndpoint = response.text
            elif packageComponent['typeOfComponent']=="driver":
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
        

#to do: remove all null-valued and empty-list-valued properties in packageManifest, at all levels.
json.dump(packageManifest, open(pathOfPackageManifestFile, 'w'), indent=4)

exit(0)

