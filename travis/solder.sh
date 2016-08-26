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
    
    JAR_VERSION=$(echo $JAR_FILE | cut -d'-' -f4 | rev | cut -c5- | rev)
    echo $JAR_VERSION
    
    curl -sL -w "CODE: %{http_code}\\n" -c cookies.txt \
        -d "password=$SOLDER_WEB_PASS&email=$SOLDER_WEB_USER&login=Log In" $SOLDER_URL -o /dev/null
    
    curl -s -w "%{http_code}\\n" -b cookies.txt -c cookies.txt \
        --data "mod-id=1&add-version=$JAR_VERSION&add-md5=" -H 'X-Requested-With: XMLHttpRequest' \
        $SOLDER_URL/mod/add-version -o /dev/null
fi

exit 0
