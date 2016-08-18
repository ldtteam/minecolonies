#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
  ./gradlew sonarqube --stacktrace \
      -Dsonar.analysis.mode=issues \
      -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
      -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
      -Dsonar.github.oauth=$GITHUB_TOKEN \
      -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.login=$SONAR_TOKEN \
      -Dsonar.password=$SONAR_PASS \
      -Dsonar.sources=src/main/java \
      -Dsonar.java.binaries=build/classes/main
fi

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
./gradlew sonarqube --stacktrace \
    -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
    -Dsonar.github.oauth=$GITHUB_TOKEN \
    -Dsonar.host.url=$SONAR_HOST_URL \
    -Dsonar.login=$SONAR_TOKEN \
    -Dsonar.password=$SONAR_PASS \
    -Dsonar.sources=src/main/java \
    -Dsonar.branch=$TRAVIS_BRANCH \
    -Dsonar.java.binaries=build/classes/main
fi
