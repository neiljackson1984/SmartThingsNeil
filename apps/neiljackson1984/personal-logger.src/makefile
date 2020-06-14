getFullyQualifiedWindowsStylePath=$(shell cygpath --windows --absolute "$(1)")
unslashedDir=$(patsubst %/,%,$(dir $(1)))
pathOfThisMakefile:=$(call unslashedDir,$(lastword $(MAKEFILE_LIST)))
pathOfDeployScript:=../../../deploy/deploy.py
venv:=$(shell cd "$(abspath $(dir ${pathOfDeployScript}))" > /dev/null 2>&1; pipenv --venv || echo initializeVenv)
groovyFile:=$(firstword $(wildcard *.groovy))
deployInfoFile:=$(firstword $(wildcard *.deployinfo))

default: | ${venv}
	# @echo "order-only prerequisites: $|"
	cd "$(abspath $(dir ${pathOfDeployScript}))"; pipenv run python "$(notdir ${pathOfDeployScript})" --source="$(shell cygpath --absolute --mixed "${groovyFile}")" --deployInfoFile="$(shell cygpath --absolute --mixed "${deployInfoFile}")"

${venv}: $(dir ${pathOfDeployScript})Pipfile $(dir ${pathOfDeployScript})Pipfile.lock
	@echo "====== INITIALIZING VIRTUAL ENVIRONMENT ======= "
	@echo "target: $@"
	cd "$(abspath $(dir ${pathOfDeployScript}))"; pipenv install
	touch $(shell cd "$(abspath $(dir ${pathOfDeployScript}))" > /dev/null 2>&1; pipenv --venv)

# .SILENT:
