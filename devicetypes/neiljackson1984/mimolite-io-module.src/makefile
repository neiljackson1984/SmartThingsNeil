#set smartThingsId to the id of the device handler or smartApp that you are uploading.
smartThingsId=9d91b3d1-23a4-4153-b7ca-120ddf76d22d

#smartthings id of the installed device that you want to test.
smartThingsIdOfTestInstance=4fdef9a4-4aab-43b8-9b96-2cf69f90e6f8

#this should be the id of an installed SmartApp that uses bradymholt's smartthings-rest-api code (https://github.com/bradymholt/smartthings-rest-api) 
# (fork: https://github.com/neiljackson1984/smartthings-rest-api)
# you do not need to establish an oauth bearer token provided you are using the ide cookie, like I do during testing.
smartThingsIdOfRestApiInstalledSmartApp=1874e073-f544-459b-9766-c99997f42867
testCommand=refresh
#type of code should be either "device" or "app"
typeOfCode=device

urlOfSmartThings=https://graph-na04-useast2.api.smartthings.com
cookieFile=../../../smartthings-ide-cookie.txt

cookiePreparationInstructionalMessage="prepare smartthings-ide-cookie.txt by logging in to smartthing ide in chrome, use developer mode (Ctrl-Shift-C), click the "Save" button when editing deive handler (probably same coookie i submitted with any request), then go toNetwork tab, right-click the request to ide/comile, and select "copy as curl for bash".  Grab the clipboard contents and pull out the string in single quotes after -H that strts with "Cookie:".  Save this string as smartthings-ide-cookie.txt. "
groovyFile=$(firstword $(wildcard *.groovy))

default: ${cookieFile} ${groovyFile}
	curl "${urlOfSmartThings}/ide/${typeOfCode}/compile"  \
	    -H '@${cookieFile}'  \
	    --data "id=${smartThingsId}"  \
	    --data "location="  \
	    --data-urlencode "code@${groovyFile}"  \
	    --data "resourceType=script"  \
	    2>nul
	curl "${urlOfSmartThings}/ide/${typeOfCode}/publishAjax"  \
	    -H '@${cookieFile}'  \
	    --data "id=${smartThingsId}"  \
	    --data "scope=me"  \
	    2>nul
	curl "${urlOfSmartThings}/api/smartapps/installations/${smartThingsIdOfRestApiInstalledSmartApp}/device/${smartThingsIdOfTestInstance}/command/${testCommand}"  \
		-H '@${cookieFile}'  \
		-X POST \
		2>nul
	# curl "${urlOfSmartThings}/api/smartapps/installations/${smartThingsIdOfRestApiInstalledSmartApp}/device/${smartThingsIdOfTestInstance}/command/${testCommand}"  \
		# -H "Authorization: Bearer c6ac2762-3181-45d3-8d38-d4914c113223" \
		# -X POST \
		# 2>nul


${cookieFile}: 
	echo ${cookiePreparationInstructionalMessage} > ${cookieFile}
	echo ${cookiePreparationInstructionalMessage}

