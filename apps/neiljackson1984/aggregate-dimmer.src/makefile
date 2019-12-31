# include ../../../makefile
pathToDeployScript=../../../deploy/deploy.py

groovyFile:=$(firstword $(wildcard *.groovy))
deployInfoFile:=$(firstword $(wildcard *.deployinfo))

default:
	@echo ${groovyFile}
	@echo $(shell cygpath --absolute --windows "${groovyFile}")
	@# cd "$(abspath $(dir ${pathToDeployScript}))"; pipenv run python "$(shell cygpath --absolute --windows "${pathToDeployScript}")" --source="$(shell cygpath --absolute --mixed "${groovyFile}")"
	@which pipenv
	cd "$(abspath $(dir ${pathToDeployScript}))"; pipenv run python "$(notdir ${pathToDeployScript})" --source="$(shell cygpath --absolute --mixed "${groovyFile}")" --deployInfoFile="$(shell cygpath --absolute --mixed "${deployInfoFile}")"
	@# cd "$(abspath $(dir ${pathToDeployScript}))"; pwd