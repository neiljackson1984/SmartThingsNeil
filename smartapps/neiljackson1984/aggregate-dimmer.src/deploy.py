import argparse
import os
import re
import subprocess

cookieJarFilePath="cookie-jar.txt"
accessTokenFilePath="accessTokenForTestInstance.txt"

parser = argparse.ArgumentParser(description="Upload app or driver code to the hubitat")
parser.add_argument("--source", action='store', nargs=1, required=True, help="the file to be uploaded to the hubitat.")
args = parser.parse_args()

print("source is " + str(args.source[0]))
print("os.getcwd(): " + os.getcwd())
source = args.source[0]

# read parameters from the magic comment strings in the source file
with open(source, 'r') as f:
    sourceContents = f.read()

hubitatId                   = re.search("^.*//////hubitatId=([0123456789abcdef-]+)\\s*$",                sourceContents, re.MULTILINE).group(1)
hubitatIdOfTestInstance     = re.search("^.*//////hubitatIdOfTestInstance=([0123456789abcdef-]+)\\s*$",  sourceContents, re.MULTILINE).group(1)
testEndpoint                = re.search("^.*//////testEndpoint=(.*)\\s*$",                               sourceContents, re.MULTILINE).group(1)
typeOfCode                  = re.search("^.*//////typeOfCode=(.*)\\s*$",                                 sourceContents, re.MULTILINE).group(1)
urlOfHubitat                = re.search("^.*//////urlOfHubitat=(.*)\\s*$",                               sourceContents, re.MULTILINE).group(1)

print("hubitatId is " + hubitatId)
print("hubitatIdOfTestInstance is " + hubitatIdOfTestInstance)
print("testEndpoint is " + testEndpoint)
print("typeOfCode is " + typeOfCode)
print("urlOfHubitat is " + urlOfHubitat)


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
            "--cookie-jar " + cookieJarFilePath  + " " +
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
if not os.path.isfile(accessTokenFilePath):
    #obtain the client id and client secret assigned to the app (assuming that oauth has been turned on for this app in the hubitat web interface)
    completedProcess = subprocess.run(
        "curl" + " " +
            "\"" + urlOfHubitat + "/" + typeOfCode + "/editor/" + hubitatId + "\"" + " " +
            "--cookie-jar " + cookieJarFilePath  + " " +
            "--get " + 
            "--data-urlencode " + "\"" + "username=" + hubitatUsername + "\""  + " " +
            "--data-urlencode " + "\"" + "password=" + hubitatPassword + "\""  + " " +
            "--data-urlencode " + "\"" + "submit=Login" + "\""  + " " +
            "",
        capture_output = True
    )