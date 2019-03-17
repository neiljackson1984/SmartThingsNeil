cookieJar=cookie-jar.txt
accessTokenFile=accessTokenForTestInstance.txt

groovyFile:=$(firstword $(wildcard *.groovy))

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

# ## extract the details related to the uploading process from the magic comments in the groovy file:
# $(eval hubitatId:=$(shell sed --silent --regexp-extended "s/.*\/\/\/\/\/\/hubitatId=([0123456789abcdef-]+)/\1/p" "${groovyFile}"))
# $(eval hubitatIdOfTestInstance:=$(shell sed --silent --regexp-extended "s/.*\/\/\/\/\/\/hubitatIdOfTestInstance=([0123456789abcdef-]+)/\1/p" "${groovyFile}"))
# $(eval testEndpoint:=$(shell sed --silent --regexp-extended "s/.*\/\/\/\/\/\/testEndpoint=(.*)/\1/p" "${groovyFile}"))
# $(eval typeOfCode:=$(shell sed --silent --regexp-extended "s/.*\/\/\/\/\/\/typeOfCode=(.*)/\1/p" "${groovyFile}"))
# $(eval urlOfHubitat:=$(shell sed --silent --regexp-extended "s/.*\/\/\/\/\/\/urlOfHubitat=(.*)/\1/p" "${groovyFile}"))

## extract the details related to the uploading process from the magic comments in the groovy file:
hubitatId:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/hubitatId=([0123456789abcdef-]+)/\\\1/p" "${groovyFile}")
hubitatIdOfTestInstance:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/hubitatIdOfTestInstance=([0123456789abcdef-]+)/\\\1/p" "${groovyFile}")
testEndpoint:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/testEndpoint=(.*)/\\\1/p" "${groovyFile}")
typeOfCode:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/typeOfCode=(.*)/\\\1/p" "${groovyFile}")
urlOfHubitat:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/urlOfHubitat=(.*)/\\\1/p" "${groovyFile}")



default: ${cookieFile} ${accessTokenFile} ${cookieJar} ${groovyFile} getVersion
	echo groovyFile: ${groovyFile}
	echo hubitatId: "${hubitatId}"
	echo hubitatIdOfTestInstance: "${hubitatIdOfTestInstance}"
	echo testEndpoint: "${testEndpoint}"
	echo urlOfHubitat: "${urlOfHubitat}"
	echo version is ${version}
	curl "${urlOfHubitat}/${typeOfCode}/ajax/update"  \
	    -b ${cookieJar}  -c ${cookieJar}  \
	    --data "id=${hubitatId}"  \
	    --data "version=${version}"  \
	    --data-urlencode "source@${groovyFile}"  \
	    2>/dev/null
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
	# #WORKS: 
	# curl --get "${urlOfHubitat}/$(subst device,devices,$(subst app,apps,${typeOfCode}))/api/${hubitatIdOfTestInstance}/${testEndpoint}" \
	    # --data-urlencode "access_token@${accessTokenFile}"  \
	    # 2>/dev/null
	curl --get "${urlOfHubitat}/$(subst device,devices,$(subst app,apps,${typeOfCode}))/api/${hubitatIdOfTestInstance}/${testEndpoint}" \
	    -b ${cookieJar}  -c ${cookieJar}  \
	    2>/dev/null

#we have to get the version number of the code currently on the hub, because we will have to submit a related (incremented-by-one) version number in our POST to submit the new code
getVersion: ${cookieJar}
	$(eval version:=$(shell curl  -b ${cookieJar}  -c ${cookieJar} "${urlOfHubitat}/${typeOfCode}/ajax/code?id=${hubitatId}"  2>/dev/null | python -c "import sys, json; print(json.load(sys.stdin)['version'])"))

${accessTokenFile}: 
	echo ${accessTokenPreparationInstructionalMessage} > ${accessTokenFile}

# the doubled (and quadrupled) dollar signs in the recipe lines below that prompt the user for username and password are required to handle the case where the user's input contains a pound sign (i.e. "#").  Suppose that the user enters "fooli#cious".
# if the doubles dollar sign below were just a single dollar sign, Make would evaluate the $(shell ...) function call immediately, and then would attempt to evaluate the expression $(eval hubitatPassword:=fooli#cious).
# The eval function is given as input, a string containing a pound sign, (namely, the string "hubitatPassword:=fooli#cious"), and, as expected, the eval function interprest the pound sign as a comment delimeter.
# on the other hand, if we use a double dollar sign, the effect is to delay expansion of the call to the shell function, so that the eval function is passed the string "hubitatPassword:=$(shell read ..." .
# when we include a double dollar sign in front of "(shell ...)", we have to include a *QUADRUPLE* dollar sign in front of reply, because that quadruple dollar sign will be run therough the evaluator twice, 
# finally yielding a single dollar sign in the string that is passed to the shell.  
# the "-s" option passed to the read command for the password has the effect of not echoing the user's typed characters, for security.
${cookieJar}: 
	echo "obtaining a cookie..."
	$(eval hubitatUsername:=$$(shell read -p "Enter your hubitat username: "; echo "$$$$REPLY"))
	$(eval hubitatPassword:=$$(shell read -s -p "Enter your hubitat password: "; echo "$$$$REPLY"))
	# echo "you entered ${hubitatUsername} ${hubitatPassword}"
	curl --cookie-jar ${cookieJar} "${urlOfHubitat}/login" \
	    --data-urlencode "username=${hubitatUsername}" \
	    --data-urlencode "password=${hubitatPassword}" \
	    --data-urlencode "submit=Login" \
	    2>/dev/null 1>&2
	
.SILENT: 