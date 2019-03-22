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

#There are some subtle differences between sh and bash when it comes to escaping back slashes.
# the effect is that if make is using sh, I need to have sequences of three backslashes in the regular expressions below,
# whereas for bash I need to have sequences of two backslashes.
#I insist on using bash as the shell so that the escaping scheme will be consistent.
SHELL=bash

sedExpressionToPrintHubitatId=s/.*\\/\\/\\/\\/\\/\\/hubitatId=([0123456789abcdef-]+)/\\1/p
sedExpressionToPrintHubitatIdOfTestInstance=s/.*\\/\\/\\/\\/\\/\\/hubitatIdOfTestInstance=([0123456789abcdef-]+)/\\1/p
sedExpressionToPrintTestEndpoint=s/.*\\/\\/\\/\\/\\/\\/testEndpoint=(.*)/\\1/p
sedExpressionToPrintTypeOfCode=s/.*\\/\\/\\/\\/\\/\\/typeOfCode=(.*)/\\1/p
sedExpressionToPrintUrlOfHubitat=s/.*\\/\\/\\/\\/\\/\\/urlOfHubitat=(.*)/\\1/p

## extract the details related to the uploading process from the magic comments in the groovy file:
hubitatId:=$(shell sed --silent --regexp-extended "${sedExpressionToPrintHubitatId}" "${groovyFile}")
hubitatIdOfTestInstance:=$(shell sed --silent --regexp-extended "${sedExpressionToPrintHubitatIdOfTestInstance}" "${groovyFile}")
testEndpoint:=$(shell sed --silent --regexp-extended "${sedExpressionToPrintTestEndpoint}" "${groovyFile}")
typeOfCode:=$(shell sed --silent --regexp-extended "${sedExpressionToPrintTypeOfCode}" "${groovyFile}")
urlOfHubitat:=$(shell sed --silent --regexp-extended "${sedExpressionToPrintUrlOfHubitat}" "${groovyFile}")

aa: getAuthorizationCode

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

#this only works if oauth has been turned on for this app.
getAuthorizationCode:
	echo getting authorization code
	$(eval combined:=\
	    $(shell \
	        curl --get "${urlOfHubitat}/${typeOfCode}/editor/${hubitatId}" -b ${cookieJar}  -c ${cookieJar}  2>/dev/null \
	            | sed --silent --regexp-extended \
	                -e "s@^.*value=\"([0123456789abcdef-]+)\" id=\"clientId\".*\$$@\\1 @p" \
	                -e "s@^.*name=\"clientSecret\" value=\"([0123456789abcdef-]+)\".*\$$@\\1 @p" \
	    ) \
	)
	#the above command outputs the client id, then a space , then a newline, then the client secret, then a space.  These sed expressions are highly dependent on the html being formatted in a certain way, which could
	# easily change and break this extraction scheme with a later release of hubitat (regular expressions are not a very robust way of parsing html (and even if we were parsing the html in
	# a more robust way -- the html code is not contractually guaranteed to present the client id and the client secret in a particular machine-readable way -- extracting the data
	# from html that is designed to create a human-readable document rather than be a machine readable structure is fragile and prone to break in the future.  However, 
	# at the moment, I don't know of any better source from which to obtain the client id and client secret programmatically than the html code returned by the web-based editor page.))
	$(eval clientId:=$(word 1,${combined}))
	$(eval clientSecret:=$(word 2,${combined}))
	$(eval urlToGetAuthorizationCode:=${urlOfHubitat}/oauth/authorize?client_id=${clientId}\&redirect_uri=abc\&response_type=code\&scope=app)
	echo clientId: ${clientId}		
	echo clientSecret: ${clientSecret}		
	echo urlToGetAuthorizationCode: ${urlToGetAuthorizationCode}		
	$(eval code:=$$(shell bash -c 'read -e -p "paste the code: "; echo "$$$$REPLY"'))
	echo code: ${code}	
	curl "${urlOfHubitat}/oauth/authorize"  \
	    --data-urlencode "grant_type=authorization_code"  \
	    --data-urlencode "client_id=${clientId}"  \
	    --data-urlencode "client_secret=${clientSecret}"  \
	    --data-urlencode "code=${code}"  \
	    --data-urlencode "redirect_uri=abc"  \
	    --data-urlencode "scope=app"  \
	    2>/dev/null	
	

# see  https://docs.python.org/2/library/xml.dom.minidom.html  for some idea on how to extract the clientId and clientSecret from the html returned from the http request to ${urlOfHubitat}/${typeOfCode}/editor/${hubitatId}
# <input class="mdl-textfield__input " type="text" value="3db30561-ee79409b8-798-88b82f01ccb9" id="clientId" disabled="disabled" placeholder="Auto-Generated">
# <input type="text" name="clientSecret" value="5af7ab-5cea0941-c088841-5fde415-2da0" class="mdl-textfield__input" placeholder="Auto-Generated" disabled="disabled" readonly="readonly" id="clientSecret">
# see https://community.hubitat.com/t/oauth-flow-cloud-and-local/3223 for a good summary of the hubitat oauth flow.


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
	$(eval hubitatUsername:=$$(shell bash -c 'read -e -p "Enter your hubitat username: "; echo "$$$$REPLY"'))
	$(eval hubitatPassword:=$$(shell bash -c 'read -e -s -p "Enter your hubitat password: "; echo "$$$$REPLY"'))
	# echo "you entered ${hubitatUsername} ${hubitatPassword}"
	curl --cookie-jar ${cookieJar} "${urlOfHubitat}/login" \
	    --data-urlencode "username=${hubitatUsername}" \
	    --data-urlencode "password=${hubitatPassword}" \
	    --data-urlencode "submit=Login" \
	    2>/dev/null 1>&2

	
.SILENT: 