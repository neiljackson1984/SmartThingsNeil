#!bin/sh

pathOfHpmExecutable=./hpm.exe
pathOfRepositoryFile=hubitat_package_manager_repository.json
pathOfpackageManifestFile=drivers/stelpro/stelpro-ki-zigbee-thermostat.src/package_manifest.json

$pathOfHpmExecutable manifest-create     "$pathOfpackageManifestFile" --name="StelPro Ki Zigbee Thermostat" --author="Neil Jackson" --version="1.0" --heversion="2.1.9" --datereleased="2021-02-10"
$pathOfHpmExecutable manifest-add-driver "$pathOfpackageManifestFile" --location="https://raw.githubusercontent.com/neiljackson1984/SmartThingsNeil/c89e585b14d3c49a4a8665b4c3326049977f8d97/drivers/stelpro/stelpro-ki-zigbee-thermostat.src/stelpro-ki-zigbee-thermostat.groovy" --required=true

$pathOfHpmExecutable repository-create "$pathOfRepositoryFile" --author="Neil Jackson"  --githuburl="https://github.com/neiljackson1984/SmartThingsNeil"
$pathOfHpmExecutable repository-add-package "$pathOfRepositoryFile" \
    --manifest "https://raw.githubusercontent.com/neiljackson1984/SmartThingsNeil/master/drivers/stelpro/stelpro-ki-zigbee-thermostat.src/package_manifest.json" \
    --category="Control" \
    --description="stelpro ki zigbee thermostat driver" \
    --tags "Climate Control" "Temperature & Humidity" "Zigbee"
    
# TO DO: prevent guids from changing (which probably means using the 'modify' rather than the 'create' versions of the hpm commands.    
    