pathToDeployScript=../../../deploy/deploy.py

groovyFile:=$(firstword $(wildcard *.groovy))
deployInfoFile:=$(firstword $(wildcard *.deployinfo))

# default:
# 	$(MAKE) deploy || $(MAKE) configureVirtualEnvironment

deploy:
	cd "$(abspath $(dir ${pathToDeployScript}))"; pipenv run python "$(notdir ${pathToDeployScript})" --source="$(shell cygpath --absolute --mixed "${groovyFile}")" --deployInfoFile="$(shell cygpath --absolute --mixed "${deployInfoFile}")"; \
		if [ $$? -eq 99 ]; then python -m pipenv install; pipenv run python "$(notdir ${pathToDeployScript})" --source="$(shell cygpath --absolute --mixed "${groovyFile}")" --deployInfoFile="$(shell cygpath --absolute --mixed "${deployInfoFile}")"; fi;
	# cd "$(abspath $(dir ${pathToDeployScript}))"; python "$(notdir ${pathToDeployScript})" --source="$(shell cygpath --absolute --mixed "${groovyFile}")" --deployInfoFile="$(shell cygpath --absolute --mixed "${deployInfoFile}")"


# configureVirtualEnvironment:
# 	cd "$(abspath $(dir ${pathToDeployScript}))"; python -m pipenv install

.SILENT: