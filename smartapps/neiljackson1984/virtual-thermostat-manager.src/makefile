#prepare smartthings-ide-cookie.txt by logging in to smartthing ide in chrome, use developer mode (Ctrl-Shift-C), click the "Save" button when editing deive handler (probably same coookie i submitted with any request), then go toNetwork tab, right-click the request to ide/comile, and select "copy as curl for bash".  Grab the clipboard contents and pull out the string in single quotes after -H that strts with "Cookie: ".  Save this string as smartthings-ide-cookie.txt.


default: ../../../smartthings-ide-cookie.txt virtual-thermostat-manager.groovy
	curl "https://graph-na04-useast2.api.smartthings.com/ide/app/compile"  \
	    -H '@../../../smartthings-ide-cookie.txt'  \
	    --data "id=4c6ce2df-3fe0-432b-ab7e-6b4f80f2e207"  \
	    --data "location="  \
	    --data-urlencode "code@virtual-thermostat-manager.groovy"  \
	    --data "resourceType=script"  \
	    2>nul
	curl "https://graph-na04-useast2.api.smartthings.com/ide/app/publishAjax"  \
	    -H '@../../../smartthings-ide-cookie.txt'  \
	    --data "id=4c6ce2df-3fe0-432b-ab7e-6b4f80f2e207"  \
	    --data "scope=me"  \
	    2>nul
	curl "https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/221cbb88-cd20-4e76-ae95-6867551380cb/runTheTestCode" \
	    -H "Authorization: Bearer 90fd8716-8e4c-441d-8bb0-c13c46b58ca6" \
	    2>nul