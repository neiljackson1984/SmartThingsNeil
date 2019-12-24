import argparse
import os
import re
import subprocess
import json
import pathlib


parser = argparse.ArgumentParser(description="Upload app or driver code to the hubitat")
parser.add_argument("--source", action='store', nargs=1, required=True, help="the file to be uploaded to the hubitat.")
args = parser.parse_args()

print("source is " + str(args.source[0]))
print("os.getcwd(): " + os.getcwd())
source = args.source[0]


credentialStorageFolderPath = pathlib.Path(pathlib.Path(source).resolve().parent,"credentials")
cookieJarFilePath           = pathlib.Path(credentialStorageFolderPath, "cookie-jar.txt")                   #   os.path.join(credentialStorageFolder, "cookie-jar.txt")
accessTokenFilePath         = pathlib.Path(credentialStorageFolderPath, "accessTokenForTestInstance.txt")   #   os.path.join(credentialStorageFolder, "accessTokenForTestInstance.txt")

#ensure that the parent folder exists for each of cookieJarFilePath and accessTokenFilePath
for x in [cookieJarFilePath, accessTokenFilePath]:
    pathlib.Path(x).resolve().parent.mkdir(parents=True, exist_ok=True) 


# read parameters from the magic comment strings in the source file
with open(source, 'r') as f:
    sourceContents = f.read()

hubitatId                   = re.search("^.*//////hubitatId=([0123456789abcdef-]+)\\s*$",                sourceContents, re.MULTILINE).group(1)
hubitatIdOfTestInstance     = re.search("^.*//////hubitatIdOfTestInstance=([0123456789abcdef-]+)\\s*$",  sourceContents, re.MULTILINE).group(1)
testEndpoint                = re.search("^.*//////testEndpoint=(.*)\\s*$",                               sourceContents, re.MULTILINE).group(1)
typeOfCode                  = re.search("^.*//////typeOfCode=(.*)\\s*$",                                 sourceContents, re.MULTILINE).group(1)
urlOfHubitat                = re.search("^.*//////urlOfHubitat=(.*)\\s*$",                               sourceContents, re.MULTILINE).group(1)

print("id of the " + typeOfCode + ":" + hubitatId)
print("hubitatIdOfTestInstance:" + hubitatIdOfTestInstance)
print("testEndpoint:" + testEndpoint)
print("typeOfCode:" + typeOfCode)
print("urlOfHubitat:" + urlOfHubitat)


#ensure that the cookie jar file exists and contains a working cookie to authenticate into the hubitat administrative web interface.
#for now, we will assume that existence of the cookie jar file implies that it contains a working cookie.
if not os.path.isfile(cookieJarFilePath):
    #collect username and password from the user
    print("please enter your hubitat username: ")
    hubitatUsername = input()
    print("please enter your hubitat password")
    hubitatPassword = input()
    print("you entered " + hubitatUsername + " and " + hubitatPassword + ".  Thank you.")

    completedProcess = subprocess.run(
        "curl" + " " +
            "\"" + urlOfHubitat + "/login" + "\"" + " " +
            "--cookie-jar " + "\"" + str(cookieJarFilePath) + "\""  + " " +
            "--data-urlencode " + "\"" + "username=" + hubitatUsername + "\""  + " " +
            "--data-urlencode " + "\"" + "password=" + hubitatPassword + "\""  + " " +
            "--data-urlencode " + "\"" + "submit=Login" + "\""  + " " +
            "",
        capture_output = True
    )
    if completedProcess.returncode != 0 or not os.path.isfile(cookieJarFilePath):
        print("the call to curl seems to have failed.")
        print(str(completedProcess))
        exit

#ensure that the accessToken file exists and contains a working access token 
#for now we will assume that existence of the access token file implies that it contains a working access token
if os.path.isfile(accessTokenFilePath):
    with open(accessTokenFilePath, 'r') as f:
        accessToken = f.read()
else:
    #obtain the client id and client secret assigned to the app (assuming that oauth has been turned on for this app in the hubitat web interface)
    url = urlOfHubitat + "/" + typeOfCode + "/editor/" + hubitatId
    completedProcess = subprocess.run(
        "curl" + " " +
            "\"" + url + "\"" + " " +
            "-b " + "\"" + str(cookieJarFilePath) + "\""  + " " +
            "-c " + "\"" + str(cookieJarFilePath) + "\""  + " " 
            "",
        capture_output = True,
        text=True
    )
    print("url: " + url)
    print(completedProcess.stdout)
    # print(type(completedProcess.stdout))
    clientId      = re.search("^.*value=\"([0123456789abcdef-]+)\" id=\"clientId\".*$",         completedProcess.stdout, re.MULTILINE).group(1)
    clientSecret  = re.search("^.*name=\"clientSecret\" value=\"([0123456789abcdef-]+)\".*$",   completedProcess.stdout, re.MULTILINE).group(1)
    # The efficacy of the above regular expressions is highly dependent on the html being formatted in a certain way, which could
	# easily change and break this extraction scheme with a later release of hubitat (regular expressions are not a very robust way of parsing html (and even if we were parsing the html in
	# a more robust way -- the html code is not contractually guaranteed to present the client id and the client secret in a particular machine-readable way -- extracting the data
	# from html that is designed to create a human-readable document rather than be a machine readable structure is fragile and prone to break in the future.  However, 
	# at the moment, I don't know of any better way to obtain the client id and client secret programmatically other than using regexes to search through the html code of the web-based editor page.)

    print("clientId: " + clientId)
    print("clientSecret: " + clientSecret)

    #now that we have the clientId and clientSecret, we can obtain the authorization code
    url = urlOfHubitat + "/oauth/confirm_access?" + "client_id=" + clientId + "&" + "redirect_uri=abc" + "&" + "response_type=code" + "&" + "scope=app"
    completedProcess = subprocess.run(
        "curl" + " " +
            "\"" + url  + "\"" + " " +
            "-b " + "\"" + str(cookieJarFilePath) + "\""  + " " +
            "-c " + "\"" + str(cookieJarFilePath) + "\""  + " " 
            "",
        capture_output = True,
        text=True
    )
    authorizationCode  = re.search("^.*name=\"code\" value=\"(\\w+)\".*$",    completedProcess.stdout, re.MULTILINE).group(1)
    appId              = re.search("^.*name=\"appId\" value=\"(\\w+)\".*$",   completedProcess.stdout, re.MULTILINE).group(1)
    print("appId: " + appId)
    print("authorizationCode: " + authorizationCode)
    #now, we can use the authorizationCode to finally obtain the access token
    
    url = urlOfHubitat + "/oauth/token"
    completedProcess = subprocess.run(
        "curl" + " " +
            "\"" + url  + "\"" + " " +
            "-b " + "\"" + str(cookieJarFilePath) + "\""  + " " +
            "-c " + "\"" + str(cookieJarFilePath) + "\""  + " " +
            "--data-urlencode " + "\"" + "grant_type="     + "authorization_code"  + "\""  + " " +
            "--data-urlencode " + "\"" + "client_id="      + clientId              + "\""  + " " +
            "--data-urlencode " + "\"" + "client_secret="  + clientSecret          + "\""  + " " +
            "--data-urlencode " + "\"" + "code="           + authorizationCode     + "\""  + " " +
            "--data-urlencode " + "\"" + "redirect_uri="   + "abc"                 + "\""  + " " + # 'abc' is a dummy value - it could be anything, as long as it matches the previous redirect uri that we submitted whn obtaining the authorization code.
            "--data-urlencode " + "\"" + "scope="          + "app"                 + "\""  + " " +
            "",
        capture_output = True,
        text=True
    )
    accessToken = json.loads(completedProcess.stdout)['access_token']
    with open(accessTokenFilePath, 'w') as f:
        f.write(accessToken)
print("accessToken: " + accessToken)

#we have to get the version number of the code currently on the hub, because we will have to submit a related (incremented-by-one) (or maybe just the exact number) version number in our POST to submit the new code
url = urlOfHubitat + "/" + typeOfCode + "/ajax/code?id=" + hubitatId
completedProcess = subprocess.run(
    "curl" + " " +
        "\"" + url  + "\"" + " " +
        "-b " + "\"" + str(cookieJarFilePath) + "\""  + " " +
        "-c " + "\"" + str(cookieJarFilePath) + "\""  + " " +
        "",
    capture_output = True,
    text=True
)
version = json.loads(completedProcess.stdout)['version']
print("version: " + str(version))

#upload the source
url = urlOfHubitat + "/" + typeOfCode + "/ajax/update"
completedProcess = subprocess.run(
    "curl" + " " +
        "\"" + url  + "\"" + " " +
        "-b " + "\"" + str(cookieJarFilePath) + "\""  + " " +
        "-c " + "\"" + str(cookieJarFilePath) + "\""  + " " +
        "--data "            + "\"" + "id="       + hubitatId  + "\""  + " " +
        "--data "            + "\"" + "version="  + str(version)    + "\""  + " " +
        "--data-urlencode "  + "\"" + "source@"   + source     + "\""  + " " +
        "",
    capture_output = True,
    text=True
)
#to do: report the status of the upload to the user

#hit the test endpoint
url = urlOfHubitat + "/" + typeOfCode + "s" + "/api/" + hubitatIdOfTestInstance + "/" + testEndpoint
completedProcess = subprocess.run(
    "curl" + " " +
        "\"" + url  + "\"" + " " +
        "--header " + "\"" + "Authorization: Bearer " + accessToken +  "\"" + " " +
        "",
    capture_output = True,
    text=True
)
print(completedProcess.stdout)
