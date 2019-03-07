cookieFile=../../../hubitat-ide-cookie.txt
accessTokenFile=accessTokenForTestInstance.txt

cookiePreparationInstructionalMessage="prepare hubitat-ide-cookie.txt by logging in to that hubitat ide in chrome, use developer mode (Ctrl-Shift-C), click the "Save" button when editing the code in the ide (probably same coookie submitted with any request), then go toNetwork tab, right-click the request to ide/comile, and select "copy as curl for bash".  Grab the clipboard contents and pull out the string in single quotes after -H that strts with "Cookie:".  Save this string as hubitat-ide-cookie.txt. "
accessTokenPreparationInstructionalMessage="retrieve the access token by running createAccessToken() within the app.  then, paste that access token here, overwriting the existing contents of this file."
groovyFile:=$(firstword $(wildcard *.groovy))

hubitatId:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/hubitatId=([0123456789abcdef-]+)/\\\1/p" "${groovyFile}")
hubitatIdOfTestInstance:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/hubitatIdOfTestInstance=([0123456789abcdef-]+)/\\\1/p" "${groovyFile}")
testEndpoint:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/testEndpoint=(.*)/\\\1/p" "${groovyFile}")
typeOfCode:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/typeOfCode=(.*)/\\\1/p" "${groovyFile}")
urlOfHubitat:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/urlOfHubitat=(.*)/\\\1/p" "${groovyFile}")
#typeOfCode should be either "device" or "app"
# in order for the above variables to be properly defined, the groovy file should contain lines that look something like the following lines:
#
#  //////hubitatId=63157b48-4ea8-4dd5-8f2a-d0661acd6b42
#  //////hubitatIdOfTestInstance=4fdef9a4-4aab-43b8-9b96-2cf69f90e6f8
#  //////testEndpoint=runTheTestCode
#  //////typeOfCode=device
#  //////urlOfHubitat=https://toreutic-abyssinian-6502.dataplicity.io
#
# it doesn't matter what precedes the six slashes - the important thing is that the line ends with six slashes, followed by a variable name, followed by the equals sign, 
# followed by the value, followed by a newline.
#
#  hubitatId is the id of the device handler (or the id of the smartapp, in the case that the project is a smartapp)
#  hubitatIdOfTestInstance is the id of the installed device (or the id of the installed smartapp, in the case that the project is a smartapp)
#  testEndpoint is the http endpoint that we will send an http GET request to as part of the testing.
#  we will send the GET query to  
#    <urlOfHubitat>/api/devices/<hubitatIdOfTestInstance>/<testEndpoint>  (in the case that typeOfCode==device)
# or <urlOfHubitat>/api/smartapps/installations/<hubitatIdOfTestInstance>/<testEndpoint>  (in the case that typeOfCode==app)
#
# For the test code to work, (assuming that testEndpoint is "runTheTestCode" and the name of the function that we want to run is "runtheTestCode"), the groovy code should
# contain something like the following 
#
#    mappings {
#        path("/runTheTestCode") { action: [GET:"runTheTestCode"] }
#    }
#   def runTheTestCode(){
#          //do some test stuff here.
#          return  render( contentType: "text/html", data: "this is the message that will be returned from the curl call.\n", status: 200);
#      }
#
#  the function runTheTestCode() does not have to be (although it can be) an officially-declared command of the device handler.
# 
# in order to be able to succesfully submit GET queries to the api for this app (or device driver), it is necessary to enable oauth for the app (or device driver).
# oauth is something that is displayed for the app as a whole (not any one installedApp instance).
# to enable oauth, go to https://<hubAddress>/[app|driver]/editor/<idOfTheApp(OrDriver)> and click the "Oauth" button.
# with hubiutat, it seems that he Oauth button only exists for Apps, not for Drivers, so I am wondering if devices can have http endpoints in the same way that apps can.
# I seem to remember that SmartThings did have Oauth and http endpoint capability for both apps and drivers, but I might be mistaken.


# version:=$(shell curl "${urlOfHubitat}/${typeOfCode}/ajax/code?id=${hubitatId}" -H '@${cookieFile}' 2>nul | python -c "${pythonExpressionToExtractVersion}")
version:=$(shell curl "${urlOfHubitat}/${typeOfCode}/ajax/code?id=${hubitatId}" -H '@${cookieFile}' 2>nul | python -c "import sys, json; print(json.load(sys.stdin)['version'])")
# newVersion:=$$(( $(version) + 1 ))
default: ${cookieFile} ${groovyFile} ${accessTokenFile}
	echo groovyFile: ${groovyFile}
	echo hubitatId: "${hubitatId}"
	echo hubitatIdOfTestInstance: "${hubitatIdOfTestInstance}"
	echo testEndpoint: "${testEndpoint}"
	echo urlOfHubitat: "${urlOfHubitat}"
	#first, we have to get the version number of the code, because we will have to submit a related (incremented-by-one) version number in our POST below.
	
	# echo curl "${urlOfHubitat}/${typeOfCode}/ajax/code?id=${hubitatId}" -H '@${cookieFile}' 
	# echo curl "${urlOfHubitat}/${typeOfCode}/ajax/code?id=${hubitatId}" -H '@${cookieFile}'  | python -c 'import sys, json; print json.load(sys.stdin)'
	echo version is ${version}
	curl "${urlOfHubitat}/${typeOfCode}/ajax/update"  \
	    -H '@${cookieFile}'  \
	    --data "id=${hubitatId}"  \
	    --data "version=${version}"  \
	    --data-urlencode "source@${groovyFile}"  \
	    2>nul
	# #DOES NOT WORK:
	# curl "${urlOfHubitat}/$(subst device,devices,$(subst app,apps,${typeOfCode}))/api/${hubitatIdOfTestInstance}/${testEndpoint}"  \
		# -H '@${cookieFile}' \
		# -X GET \
		# 2>nul
	# #DOES NOT WORK:
	# curl "${urlOfHubitat}/$(subst device,devices,$(subst app,apps,${typeOfCode}))/api/${hubitatIdOfTestInstance}/${testEndpoint}"  \
		# -H "Authorization: Bearer 1c393444-5ef9-4e22-960a-2417654a4c13" \
		# -X GET \
		# 2>nul
	#WORKS: 
	curl --get "${urlOfHubitat}/$(subst device,devices,$(subst app,apps,${typeOfCode}))/api/${hubitatIdOfTestInstance}/${testEndpoint}" \
	    --data-urlencode "access_token@${accessTokenFile}"  \
	    2>nul



${cookieFile}: 
	echo ${cookiePreparationInstructionalMessage} > ${cookieFile}
	echo ${cookiePreparationInstructionalMessage}

${accessTokenFile}: 
	echo ${accessTokenPreparationInstructionalMessage} > ${accessTokenFile}


.SILENT: 