#!/bin/bash

SOLDER_DIR="solder"

beginswith() { case $2 in "$1"*) true;; *) false;; esac; }
beginswith() { case $1 in "$2"*) true;; *) false;; esac; }
checkresult() { if [ $? = 0 ]; then echo TRUE; else echo FALSE; fi; }

any() {
    test=$1; shift
    for i in "$@"; do
        $test "$i" && return
    done
}


if [ "$TRAVIS_PULL_REQUEST" == "false" ] && ( any "beginswith $TRAVIS_BRANCH" develop master release version ); then
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
    
    curl -sL -w "Login CODE: %{http_code}\\n" -c cookies.txt \
        -d "password=$SOLDER_WEB_PASS&email=$SOLDER_WEB_USER&login=Log In" $SOLDER_URL/login -o /dev/null
    
    curl -s -w "View Mod CODE: %{http_code}\\n" -b cookies.txt -c cookies.txt $SOLDER_URL/mod/view/1 -o /dev/null
    
    curl -s -w "\\nAdd Mod CODE: %{http_code}\\n" -b cookies.txt -c cookies.txt \
        --data "mod-id=1&add-version=$JAR_VERSION&add-md5=" -H 'X-Requested-With: XMLHttpRequest' \
        $SOLDER_URL/mod/add-version
        
    MOD_VERSION=$(curl -s -b cookies.txt -c cookies.txt $SOLDER_URL/mod/view/1 | grep version-id |head -n 1 | cut -d'"' -f6)
        
    echo Mod Version: $MOD_VERSION
    
    MODPACK_OLD_VERSION=$(curl -s -b cookies.txt -c cookies.txt $SOLDER_URL/modpack/view/1 | grep Manage | tail -n 1 | cut -d'"' -f2 | cut -d'/' -f6)
        
    echo Old Modpack Version: $MODPACK_OLD_VERSION
    
    INSERT_TOKEN=$(curl -sL -b cookies.txt -c cookies.txt $SOLDER_URL/modpack/add-build/1 | grep token | cut -d'"' -f12)
    
    echo Token: $INSERT_TOKEN
    
    MODPACK_VERSION=$(curl -sL -w "%{url_effective}\\n" -b cookies.txt -c cookies.txt --data "_token=$INSERT_TOKEN&version=$JAR_VERSION&minecraft=1.11&clone=$MODPACK_OLD_VERSION&java-version=1.8" -H 'X-Requested-With: XMLHttpRequest' $SOLDER_URL/modpack/add-build/1  -o /dev/null | cut -d'/' -f6)
        
    echo Created Modpack Version: $MODPACK_VERSION
    
    MODVERSION_ID=$(curl -s -b cookies.txt -c cookies.txt $SOLDER_URL/modpack/build/$MODPACK_VERSION | awk '/(minecolonies)/,/Forge/' | grep modversion_id | head -n 1 | cut -d'"' -f8)

    echo Modversion ID: $MODVERSION_ID
    
    curl -sL -w "\\nMod Version Update CODE: %{http_code}\\n" -b cookies.txt -c cookies.txt \
        --data "build_id=$MODPACK_VERSION&modversion_id=$MODVERSION_ID&action=version&version=$MOD_VERSION" -H 'X-Requested-With: XMLHttpRequest' \
        $SOLDER_URL/modpack/modify/version
        
    echo "Publishing new version..."
    
    curl -sL -w "\\nMod Version Publish CODE: %{http_code}\\n" -b cookies.txt -c cookies.txt -H 'X-Requested-With: XMLHttpRequest' \
        "$SOLDER_URL/modpack/modify/published?build=$MODPACK_VERSION&published=1"
    
    if [ "$TRAVIS_BRANCH" = "develop" ] || [ "$TRAVIS_BRANCH" = "master" ]; then
        echo "Setting new version as Latest..."
        curl -sL -w "\\nMod Version Latest CODE: %{http_code}\\n" -b cookies.txt -c cookies.txt -H 'X-Requested-With: XMLHttpRequest' \
            "$SOLDER_URL/modpack/modify/latest?modpack=1&latest=$JAR_VERSION"
            
        if [ "$TRAVIS_BRANCH" = "master" ]; then
            echo "Setting new version as Recommended..."
            curl -sL -w "\\nMod Version Recommend CODE: %{http_code}\\n" -b cookies.txt -c cookies.txt -H 'X-Requested-With: XMLHttpRequest' \
                "$SOLDER_URL/modpack/modify/recommended?modpack=1&recommended=$JAR_VERSION"
        fi
    fi
else
    echo "Branch $TRAVIS_BRANCH with pr $TRAVIS_PULL_REQUEST does not need to be on solder!"
fi

exit 0
