#!/bin/bash


if [ "$TRAVIS_BRANCH" = "develop" ] || [ "$TRAVIS_BRANCH" = "master" ] || [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    mkdir solder
    mv ./build/libs/*univ*.jar solder
    ls -al solder
fi

exit 0
