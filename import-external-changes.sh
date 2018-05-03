#!/bin/bash
# to run in a Travis environment, you must make this script executable by running "git update-index --chmod=+x <pathToThisFile>" .

echo "now importing external changes."
nameOfTag=$(date +%Y-%m-%d-%H%M%S--external-changes)
tagMessage="external changes available from all braids."
testing=true
nameOfBranchToWhichToImportChanges=master

if test "$testing"='true'; then
	git clone https://github.com/neiljackson1984/neil-smartThings
	pushd neil-smartThings
fi


# we assume that we are starting out in the root of my smartThings git repository with some arbitrary branch 
# checked out (probably the special branch that I will use to trigger Travis.)
# we also assume that braid is installed.
git checkout $(git rev-parse $nameOfBranchToWhichToImportChanges) # this puts us into a detached head state, which ensures that we will not muck up the master branch

initialCommit=$(git rev-parse $nameOfBranchToWhichToImportChanges)


# bundle exec braid update
braid update 
finalCommit=$(git rev-parse HEAD)




# the above call to braid update may have caused multiple consecutive commits to occur.  We want these to appear in the history as one single commit.
git reset $(git rev-parse $nameOfBranchToWhichToImportChanges)
git add *
git commit --message "$(git log $initialCommit..$finalCommit)"

git tag --annotate --message="$tagMessage" $nameOfTag

git config --local user.name "neil@rattnow.com"
git config --local user.email "neil@rattnow.com"
#the above two values are not hugely significant - they only affect the description of the tag - they have no bearing on authentication to github.

#much thanks to https://gist.github.com/willprice/e07efd73fb7f13f917ea for describing some of the steps involved in getting a travis build to push back to git.

git config --local credential.helper store
echo "https://"githubOnlyCaresAboutTheTokenSoThisFieldIsJustADummy:$GITHUB_TOKEN"@github.com" > ~/.git-credentials
git push --tags

echo "By the way, mySuperDuperSecret is "$mySuperDuperSecret"."

if test "$testing"='true'; then
	popd
	rm -rf neil-smartThings
	git checkout -f $nameOfBranchToWhichToImportChanges
	git reset --hard
	git clean -fxd :/
fi

#we have just created a commit that is a child of the commit that $nameOfBranchToWhichToImportChanges currently points to.  We have tagged this commit with a uniquely-named tag and pushed the tag to github.

