cookieFile=../../../smartthings-ide-cookie.txt

cookiePreparationInstructionalMessage="prepare smartthings-ide-cookie.txt by logging in to smartthing ide in chrome, use developer mode (Ctrl-Shift-C), click the "Save" button when editing deive handler (probably same coookie i submitted with any request), then go toNetwork tab, right-click the request to ide/comile, and select "copy as curl for bash".  Grab the clipboard contents and pull out the string in single quotes after -H that strts with "Cookie:".  Save this string as smartthings-ide-cookie.txt. "
groovyFile:=$(firstword $(wildcard *.groovy))

smartThingsId:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/smartThingsId=([0123456789abcdef-]+)/\\\1/p" "${groovyFile}")
smartThingsIdOfTestInstance:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/smartThingsIdOfTestInstance=([0123456789abcdef-]+)/\\\1/p" "${groovyFile}")
testEndpoint:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/testEndpoint=(.*)/\\\1/p" "${groovyFile}")
typeOfCode:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/typeOfCode=(.*)/\\\1/p" "${groovyFile}")
urlOfSmartThings:=$(shell sed --silent --regexp-extended "s/.*\\\/\\\/\\\/\\\/\\\/\\\/urlOfSmartThings=(.*)/\\\1/p" "${groovyFile}")
#type of code should be either "device" or "smartapp"
# in order for the above variables to be properly defined, the groovy file should contain lines that look something like the following lines:
#
#  //////smartThingsId=63157b48-4ea8-4dd5-8f2a-d0661acd6b42
#  //////smartThingsIdOfTestInstance=4fdef9a4-4aab-43b8-9b96-2cf69f90e6f8
#  //////testEndpoint=runTheTestCode
#  //////typeOfCode=device
#  //////urlOfSmartThings=https://graph-na04-useast2.api.smartthings.com
#
# it doesn't matter what precedes the six slashes - the important thing is that the line ends with six slashes, followed by a variable name, followed by the equals sign, 
# followed by the value, followed by a newline.
#
#  smartThingsId is the guid of the device handler (or the guid of the smartapp, in the case that the project is a smartapp)
#  smartThingsIdOfTestInstance is the guid of the installed device (or the guid of the installed smartapp, in the case that the project is a smartapp)
#  testEndpoint is the http endpoint that we will send an http GET request to as part of the testing.
#  we will send the GET query to  
#    <urlOfSmartThings>/api/devices/<smartThingsIdOfTestInstance>/<testEndpoint>  (in the case that typeOfCode==device)
# or <urlOfSmartThings>/api/smartapps/installations/<smartThingsIdOfTestInstance>/<testEndpoint>  (in the case that typeOfCode==smartapp)
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

default: ${cookieFile} ${groovyFile}
	echo groovyFile: ${groovyFile}
	echo smartThingsId: "${smartThingsId}"
	echo smartThingsIdOfTestInstance: "${smartThingsIdOfTestInstance}"
	echo testEndpoint: "${testEndpoint}"
	echo urlOfSmartThings: "${urlOfSmartThings}"
	curl "${urlOfSmartThings}/ide/$(subst smartapp,app,${typeOfCode})/compile"  \
	    -H '@${cookieFile}'  \
	    --data "id=${smartThingsId}"  \
	    --data "location="  \
	    --data-urlencode "code@${groovyFile}"  \
	    --data "resourceType=script"  \
	    2>nul
	curl "${urlOfSmartThings}/ide/$(subst smartapp,app,${typeOfCode})/publishAjax"  \
	    -H '@${cookieFile}'  \
	    --data "id=${smartThingsId}"  \
	    --data "scope=me"  \
	    2>nul
	curl "${urlOfSmartThings}/api/$(subst device,devices,$(subst smartapp,smartapps/installations,${typeOfCode}))/${smartThingsIdOfTestInstance}/${testEndpoint}"  \
		-H '@${cookieFile}'  \
		-X GET \
		2>nul


${cookieFile}: 
	echo ${cookiePreparationInstructionalMessage} > ${cookieFile}
	echo ${cookiePreparationInstructionalMessage}

.SILENT: 