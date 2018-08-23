#prepare smartthings-ide-cookie.txt by logging in to smartthing ide in chrome, use developer mode (Ctrl-Shift-C), click the "Save" button when editing deive handler (probably same coookie i submitted with any request), then go toNetwork tab, right-click the request to ide/comile, and select "copy as curl for bash".  Grab the clipboard contents and pull out the string in single quotes after -H that strts with "Cookie: ".  Save this string as smartthings-ide-cookie.txt.


default: smartthings-ide-cookie.txt virtual-thermostat.groovy
	curl "https://graph-na04-useast2.api.smartthings.com/ide/device/compile"  \
	    -H '@smartthings-ide-cookie.txt'  \
	    --data "id=221620c6-0d27-445a-96cd-5aa61bdc6814"  \
	    --data "location="  \
	    --data-urlencode "code@virtual-thermostat.groovy"  \
	    --data "resourceType=script"  \
	    2>nul
