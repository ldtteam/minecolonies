#!/bin/bash

SOLDER_DIR="solder"

if [ "$TRAVIS_BRANCH" = "develop" ] || [ "$TRAVIS_BRANCH" = "master" ] || [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    mkdir $SOLDER_DIR
    mv ./build/libs/*univ*.jar $SOLDER_DIR
    cd $SOLDER_DIR
    JAR_FILE=$(ls -1 .)
    echo $JAR_FILE
    mkdir mods
    cp $JAR_FILE mods/$JAR_FILE
    zip -r minecolonies.zip mods
    echo $(unzip -l minecolonies.zip)
    
    curl -sL -w "CODE: %{http_code}\\n" -c cookies.txt -d "password=$SOLDER_WEB_PASS&email=$SOLDER_WEB_USER&login=Log In" $SOLDER_URL -o /dev/null
fi

exit 0
