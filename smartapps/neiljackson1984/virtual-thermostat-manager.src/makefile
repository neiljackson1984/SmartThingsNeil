pathToDeployScript=../../../deploy/deploy.py

groovyFile:=$(firstword $(wildcard *.groovy))
deployInfoFile:=$(firstword $(wildcard *.deployinfo))

default:
	cd "$(abspath $(dir ${pathToDeployScript}))"; pipenv run python "$(notdir ${pathToDeployScript})" --source="$(shell cygpath --absolute --mixed "${groovyFile}")" --deployInfoFile="$(shell cygpath --absolute --mixed "${deployInfoFile}")"
