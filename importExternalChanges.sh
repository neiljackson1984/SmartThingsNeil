#!/usr/bin/env sh
# to run in a Travis environment, you must make this script executable by running "git update-index --chmod=+x <pathToThisFile>" .

nameOfTag=$(date +%Y-%m-%d-%H%M%S--external-changes)
tagMessage="external changes available from all braids."
testing=true

if $testing=="true"; then
	git clone https://github.com/neiljackson1984/neil-smartThings
	pushd neil-smartThings;
fi


# we assume that we are starting out in the root of my smartThings git repository with some arbitrary branch 
# checked out (probably the special branch that I will use to trigger Travis.)
# we also assume that braid is installed.
git checkout $(git rev-parse master)
braid update
git tag --annotate --message="$tagMessage" $nameOfTag
git push --tags

if $testing=="true"; then
	popd
	rm -rf neil-smartThings
fi

