#!/bin/bash

SOLDER_DIR="solder"

if [ "$TRAVIS_BRANCH" = "develop" ] || [ "$TRAVIS_BRANCH" = "master" ] || [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    mkdir $SOLDER_DIR
    mv ./build/libs/*univ*.jar $SOLDER_DIR
    JAR_FILE=$(ls -1 $SOLDER_DIR)
    echo $JAR_FILE
    mkdir $SOLDER_DIR/modpack
    mkdir $SOLDER_DIR/modpack/mods
    cp $SOLDER_DIR/$JAR_FILE $SOLDER_DIR/modpack/mods/$JAR_FILE
    zip -r $SOLDER_DIR/minecolonies.zip $SOLDER_DIR/modpack
    echo $(unzip -l $SOLDER_DIR/minecolonies.zip)
fi

exit 0
