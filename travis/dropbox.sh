#!/bin/bash

rm -rfv ~/.dropbox_uploader
echo "OAUTH_ACCESS_TOKEN=$OAUTH_ACCESS_TOKEN" > ~/.dropbox_uploader

curl "https://raw.githubusercontent.com/andreafabrizi/Dropbox-Uploader/master/dropbox_uploader.sh" -o ~/dropbox_uploader.sh
chmod +x ~/dropbox_uploader.sh

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    ~/dropbox_uploader.sh mkdir "branch"
    ~/dropbox_uploader.sh mkdir "branch/$TRAVIS_BRANCH"
    ~/dropbox_uploader.sh upload ./build/libs/*univ*.jar "branch/$TRAVIS_BRANCH"
    ~/dropbox_uploader.sh upload ./build/libs/*deobf*.jar "branch/$TRAVIS_BRANCH"
    ~/dropbox_uploader.sh upload ./build/libs/*sources*.jar "branch/$TRAVIS_BRANCH"
else
    ~/dropbox_uploader.sh mkdir "pr"
    ~/dropbox_uploader.sh mkdir "pr/$TRAVIS_PULL_REQUEST"
    ~/dropbox_uploader.sh upload ./build/libs/*univ*.jar "pr/$TRAVIS_PULL_REQUEST"
    ~/dropbox_uploader.sh upload ./build/libs/*sources*.jar "pr/$TRAVIS_PULL_REQUEST"
    ~/dropbox_uploader.sh upload ./build/libs/*deobf*.jar "pr/$TRAVIS_PULL_REQUEST"
fi

if [ "$TRAVIS_BRANCH" = "develop" ] || [ "$TRAVIS_BRANCH" = "master" ] || [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    curl -s $UPLOADER_GENERATE_URL
fi

exit 0
