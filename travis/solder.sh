#!/bin/bash


if [ "$TRAVIS_BRANCH" = "develop" ] || [ "$TRAVIS_BRANCH" = "master" ] || [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    mkdir solder
    mv ./build/libs/*univ*.jar solder
    jarfile=$(ls -1 solder)
    echo $jarfile
fi

exit 0
