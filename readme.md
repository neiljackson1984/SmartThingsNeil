This is a repository to contain all the custom SmartApps and Device Handlers that I use, inlcuding both those by me and those by others.

I am using braid to keep parts of this repository in sync with parts of other repositories.

To update to the latest versions of all other repositories, first clone this repository somewhere, then cd into the repository directory and run "braid update".  Then run "git push" to push everything back into the cloud.

To add a smartapp or devicehandler from another repository (and set up tracking with braid), run, for instance,
braid add \
   https://github.com/erocm123/SmartThingsPublic \
   --path=devicetypes/erocm123/inovelli-2-channel-smart-plug-nzw37-w-scene.src \
   devicetypes/erocm123/inovelli-2-channel-smart-plug-nzw37-w-scene.src

   
-Neil