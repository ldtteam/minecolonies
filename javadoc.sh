#!/bin/bash

echo "Javadoc-testing commit $TRAVIS_COMMIT"

curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
      --request POST \
      --data '{"state":"pending",
      "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/$TRAVIS_BUILD_ID",
      "description":"Javadoc build status",
      "context":"javadoc"}' \
      "https://api.github.com/repos/Minecolonies/minecolonies/statuses/$TRAVIS_COMMIT"
      
echo "Start javadoc gradle..."

if ./gradlew javadoc; then
      curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
            --request POST \
            --data '{"state":"failure",
            "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/$TRAVIS_BUILD_ID",
            "description":"Javadoc build status",
            "context":"javadoc"}' \
            "https://api.github.com/repos/Minecolonies/minecolonies/statuses/$TRAVIS_COMMIT"
else
      curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
            --request POST \
            --data '{"state":"error",
            "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/$TRAVIS_BUILD_ID",
            "description":"Javadoc build status",
            "context":"javadoc"}' \
            "https://api.github.com/repos/Minecolonies/minecolonies/statuses/$TRAVIS_COMMIT"
fi

echo "Finished javadoc gradle..."
