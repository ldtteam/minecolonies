#!/bin/bash

./gradlew build

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
  #./gradlew setupDecompWorkspace --refresh-dependencies
  #./gradlew build
  ./gradlew test jacocoTestReport sonarqube --stacktrace \
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

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  #./gradlew setupDecompWorkspace
  if [ -n "${GITHUB_TOKEN:-}" ]; then
    ./gradlew test jacocoTestReport sonarqube --stacktrace \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.oauth=$GITHUB_TOKEN \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN \
        -Dsonar.password=$SONAR_PASS \
        -Dsonar.sources=src/main/java \
        -Dsonar.branch=$TRAVIS_BRANCH \
        -Dsonar.java.binaries=build/classes/main
  fi
fi

chmod +x ./dropbox.sh
./dropbox.sh
