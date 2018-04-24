#!/usr/bin/env sh
# to run in a Travis environment, you must make this script executable by running "git update-index --chmod=+x <pathToThisFile>" .

nameOfTag=$(date +%Y-%m-%d-%H%M%S--external-changes)
tagMessage="external changes available from all braids."
testing=true
nameOfBranchToWhichToImportChanges=master

if $testing="true"; then
	git clone https://github.com/neiljackson1984/neil-smartThings
	pushd neil-smartThings
fi


# we assume that we are starting out in the root of my smartThings git repository with some arbitrary branch 
# checked out (probably the special branch that I will use to trigger Travis.)
# we also assume that braid is installed.
git checkout $(git rev-parse $nameOfBranchToWhichToImportChanges) # this puts us into a detached head state, which ensures that we will not muck up the master branch
braid update
git tag --annotate --message="$tagMessage" $nameOfTag
git push --tags

if $testing="true"; then
	popd
	rm -rf neil-smartThings
	git checkout -f $nameOfBranchToWhichToImportChanges;
fi

#we have just created a commit that is a child of the commit that $nameOfBranchToWhichToImportChanges currently points to.  We have tagged this commit with a uniquely-named tag and pushed the tag to github.

