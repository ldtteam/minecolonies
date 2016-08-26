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
    
    ZIP_NAME=minecolonies-$JAR_VERSION.zip
    
    mv minecolonies.zip $ZIP_NAME
    
    curl -T $ZIP_NAME -u $SOLDER_USER:$SOLDER_PASS $SOLDER_FTP/minecolonies/
    
    curl -sL -w "CODE: %{http_code}\\n" -c cookies.txt \
        -d "password=$SOLDER_WEB_PASS&email=$SOLDER_WEB_USER&login=Log In" $SOLDER_URL -o /dev/null
    
    curl -s -b cookies.txt -c cookies.txt $SOLDER_URL/mod/view/1
    
    curl -s -b cookies.txt -c cookies.txt \
        --data "mod-id=1&add-version=$JAR_VERSION&add-md5=" -H 'X-Requested-With: XMLHttpRequest' \
        $SOLDER_URL/mod/add-version
        
    curl -sL -w "%{url_effective}\\n" -b cookies.txt -c cookies.txt \
        --data "version=$JAR_VERSION&minecraft=1.8.9&clone=33&java-version=1.8" -H 'X-Requested-With: XMLHttpRequest' \
        $SOLDER_URL/modpack/add-build/1 -o /dev/null | cut -d'/' -f6
    
    INSERT_TOKEN=$(curl -sL -b cookies.txt -c cookies.txt $SOLDER_URL/modpack/add-build/1 | grep token | cut -d'"' -f12)
    
    echo $INSERT_TOKEN
    
    MODPACK_VERSION=$(curl -sL -w "%{url_effective}\\n" -b cookies.txt -c cookies.txt --data "_token=$INSERT_TOKEN&version=$JAR_VERSION&minecraft=1.8.9&clone=33&java-version=1.8" -H 'X-Requested-With: XMLHttpRequest' $SOLDER_URL/modpack/add-build/1  -o /dev/null | cut -d'/' -f6)
        
    echo $MODPACK_VERSION
fi

exit 0
