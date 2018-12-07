#!/bin/bash

# takes two arguments, the first is the URL of a git repository.  The second is a path within that repository.
# for example:
#	add-external-thing "https://github.com/SmartThingsCommunity/SmartThingsPublic" "devicetypes/smartthings/virtual-switch.src"
# invokes braid add to add a braid of the specified path within the specified repository, located at the same path within the current repository.  From the example arguments above, we will run:
#  braid add \
#   https://github.com/SmartThingsCommunity/SmartThingsPublic \
#   --path=devicetypes/smartthings/virtual-switch.src \
#   devicetypes/smartthings/virtual-switch.src

# echo \$#: $#

if [ $# -ne 2 ]; then
    echo syntax error. This script expects two arguments - the first is the URL of a git repository.  the second is a path within that repository.
    echo EXAMPLE: ./add-external-thing.sh add-external-thing "https://github.com/SmartThingsCommunity/SmartThingsPublic" "devicetypes/smartthings/virtual-switch.src"
else
    urlOfGitRepository=$1
    pathWithinGitRepository=$2
    braid add "$urlOfGitRepository" --path="$pathWithinGitRepository" $pathWithinGitRepository
fi

