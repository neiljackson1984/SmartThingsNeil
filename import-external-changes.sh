#!/bin/bash
# to run in a Travis environment, you must make this script executable by running "git update-index --chmod=+x <pathToThisFile>" .

echo "now importing external changes."
nameOfTag=$(date +%Y-%m-%d-%H%M%S--external-changes)
nameOfBranchToContainProposedChanges=$nameOfTag
tagMessage="external changes available from all braids."
# testing=true
nameOfBranchToWhichToImportChanges=master

githubPathOfMainRepository=neiljackson1984/neil-smartThings
urlOfMainRepository=https://github.com/$githubPathOfMainRepository

githubPathOfRepositoryForProposedUpdates=cirattnow/neil-smartThings
urlOfRepositoryForProposedUpdates=https://github.com/$githubPathOfRepositoryForProposedUpdates

git clone $urlOfMainRepository
pushd neil-smartThings

git remote set-url origin $urlOfRepositoryForProposedUpdates


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
git add --all --force
# git commit --message "$(git log $initialCommit..$finalCommit
#the following line will use the output of the git log command as the commit message.
git log $initialCommit..$finalCommit | git commit --file=- 

#git tag --annotate --message="$tagMessage" $nameOfTag
git branch $nameOfBranchToContainProposedChanges
git checkout $nameOfBranchToContainProposedChanges

git diff --name-only HEAD^ HEAD

git config --local user.name "ci@rattnow.com"
git config --local user.email "ci@rattnow.com"
#the above two values are not hugely significant - they only affect the description of the tag - they have no bearing on authentication to github.

#much thanks to https://gist.github.com/willprice/e07efd73fb7f13f917ea for describing some of the steps involved in getting a travis build to push back to git.

git config --local credential.helper store
echo "https://githubOnlyCaresAboutTheTokenSoThisFieldIsJustADummy:"$GITHUB_TOKEN"@github.com" > ~/.git-credentials
#git push --tags
git push --set-upstream origin $nameOfBranchToContainProposedChanges

# get the number of files (other than the .braids.json file) that have changed.  
# The .braids.json file tends to change even when no other file changes.  This happens when there is a new commit in the repository 
# that a braid points to, but the file(s) that we are tracking haven't changed.
numberOfChangedFiles=$(git diff --name-only HEAD^ HEAD | grep --count --invert-match ^.braids.json\$)
braidsFileChanged=$(git diff --name-only HEAD^ HEAD | grep --count ^.braids.json\$)

echo numberOfChangedFiles: $numberOfChangedFiles
echo braidsFileChanged: $braidsFileChanged

if [$numberOfChangedFiles -gt 0] 
	then
		#create a pull request in the github repository
		echo "github.com:" > ~/.config/hub
		echo "- user: cirattnow" >> ~/.config/hub
		echo "  oauth_token: "$GITHUB_TOKEN"" >> ~/.config/hub
		echo "  protocol: https" >>  ~/.config/hub

		git checkout --force $nameOfBranchToWhichToImportChanges #hub complains if we are in detached head state, so we checkout any arbitrary branch to prevent pull-request from complaining (I tested and confirmed that passing the -f flag to hub pull-request does not prevent hub pull-request from complaining about bei8ng in detached head state.)
		# hub pull-request -b $nameOfBranchToWhichToImportChanges -h $nameOfTag -m "this is the message for the pull request 1, generated $(date +%Y-%m-%d-%H%M%S)"
		# hub pull-request -b $nameOfBranchToWhichToImportChanges -h $(git rev-parse $nameOfTag) -m "this is the message for the pull request 2, generated $(date +%Y-%m-%d-%H%M%S)"
		hub pull-request --base=$githubPathOfMainRepository:$nameOfBranchToWhichToImportChanges --head=$githubPathOfRepositoryForProposedUpdates:$nameOfBranchToContainProposedChanges --message="this is the message for the pull request 3, generated $(date +%Y-%m-%d-%H%M%S)"
		# -b specifies the base of the pull request (i.e. the branch (in the repository pointed to by origin) that we are requesting that some commit be merged into)
		# -h specifies the head of the pull request (i.e. the commit that we are requesting be merged into the base.)
		# it is not entirely clear to me whether each of b and h are supposed to be a branch or a specific commit or both.  It seems that the base ought to always be a branch, whereas the head ought to be allowed to be a branch or a specific commit.
		# The -f flag unfortunately does not prevent hub pull-request from complaining that we are in detached head state (hub pull-request would prefer that we be on a named branch, but for our purposes it does not matter).  That is why we 
	else
		if [$braidsFileChanged -neq 0] 
			then
				echo ".braids.json changed, but no other file changed, so we will not create a pull request."
			else
				echo "no files changed, so we will not create a pull request."
		fi
fi




echo "By the way, mySuperDuperSecret is "$mySuperDuperSecret"."

# if test "$testing"='true'; then
	# popd
	# rm -rf neil-smartThings
	# git checkout -f $nameOfBranchToWhichToImportChanges
	# git reset --hard
	# git clean -fxd :/
# fi

#we have just created a commit that is a child of the commit that $nameOfBranchToWhichToImportChanges currently points to.  We have tagged this commit with a uniquely-named tag and pushed the tag to github.

