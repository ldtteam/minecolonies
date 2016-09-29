#!/bin/bash

# filter out # for sonar
BRANCH=$(echo $TRAVIS_BRANCH | tr -d '#')

echo "building for branch $BRANCH"

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
  ./gradlew test jacocoTestReport sonarqube --stacktrace \
      -Dsonar.analysis.mode=issues \
      -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
      -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
      -Dsonar.github.oauth=$GITHUB_TOKEN \
      -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.login=$SONAR_TOKEN \
      -Dsonar.password=$SONAR_PASS \
      -Dsonar.sources=src/main/java \
      -Dsonar.branch=$BRANCH \
      -Dsonar.java.binaries=build/classes/main \
      -DXmx2g
  if [ $? -ne 0 ]; then
    curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
      --request POST \
      --data '{"state":"failure",
      "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
      "description":"Sonarqube build failed, please check logs!",
      "context":"sonarqube"}' \
      "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
  fi
fi

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
  curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
    --request POST \
    --data '{"state":"pending",
    "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
    "description": "Sonarqube report in progress...",
    "context":"sonarqube-report"}' \
    "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
  ./gradlew test jacocoTestReport sonarqube --stacktrace \
    -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
    -Dsonar.github.oauth=$GITHUB_TOKEN \
    -Dsonar.host.url=$SONAR_HOST_URL \
    -Dsonar.login=$SONAR_TOKEN \
    -Dsonar.password=$SONAR_PASS \
    -Dsonar.sources=src/main/java \
    -Dsonar.branch=$BRANCH \
    -Dsonar.java.binaries=build/classes/main \
    -DXmx2g
  if [ $? -ne 0 ]; then
    curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
      --request POST \
      --data '{"state":"failure",
      "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
      "description":"Sonarqube report failed, please check logs!",
      "context":"sonarqube-report"}' \
      "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
  else
    curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
      --request POST \
      --data '{"state":"success",
      "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
      "description":"Sonarqube report passed!",
      "context":"sonarqube-report"}' \
      "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"    
  fi
fi


