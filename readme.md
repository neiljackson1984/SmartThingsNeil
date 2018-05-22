This is a repository to contain all the custom SmartApps and Device Handlers that I use, inlcuding both those by me and those by others.

I am using braid to keep parts of this repository in sync with parts of other repositories.

To update to the latest versions of all other repositories:
````Shell
git clone https://github.com/neiljackson1984/neil-smartThings; cd neil-smartThings; braid update; git push;
````

To add a smartapp or devicehandler from another repository (and set up tracking with braid), run, for instance,
````Shell
braid add \
   https://github.com/erocm123/SmartThingsPublic \
   --path=devicetypes/erocm123/inovelli-2-channel-smart-plug-nzw37-w-scene.src \
   devicetypes/erocm123/inovelli-2-channel-smart-plug-nzw37-w-scene.src
````
Slightly simpler, run:

````./add-external-thing.sh "https://github.com/erocm123/SmartThingsPublic" "devicetypes/erocm123/inovelli-2-channel-smart-plug-nzw37-w-scene.src"````

-Neil
