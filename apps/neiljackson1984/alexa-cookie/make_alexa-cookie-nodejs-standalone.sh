#!/bin/sh

# rm --recursive --force hubitat
cd build
initialWorkingDirectory=$(pwd)
git clone https://github.com/gabriele-v/hubitat
cd hubitat/AlexaCookieNodeJs/AlexaCookieNodeJs
npm install
npm install open

# inject the following into the server.listen() method:
# require('open')(`http://localhost:${port}`, {wait: false});

# The new server.listen() definition should look like this:
#  server.listen(port, function () {
#      console.log(`AlexaCookieNodeJs listening on port ${port}!`);
#      require('open')(`http://localhost:${port}`, {wait: false});
#  });

nexe --input AlexaCookie.js --resource config.json --output $initialWorkingDirectory/AlexaCookieNodeJs-standalone.exe

