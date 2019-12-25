# include ../../../makefile
pathToDeployScript=../../../deploy/deploy.py

groovyFile:=$(firstword $(wildcard *.groovy))
default:
	python "${pathToDeployScript}" --source="${groovyFile}"