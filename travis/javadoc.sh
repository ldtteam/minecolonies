#!/bin/bash

echo "Javadoc-testing commit $TRAVIS_COMMIT"
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
      curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
            --request POST \
            --data '{"state":"pending",
            "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
            "description":"Javadoc build in progress...",
            "context":"javadoc"}' \
            "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
            
      echo "Start javadoc gradle..."
      
      if ./gradlew javadoc -DXmx2g; then
            curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
                  --request POST \
                  --data '{"state":"success",
                  "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
                  "description":"Javadoc build passed!",
                  "context":"javadoc"}' \
                  "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
      else
            curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
                  --request POST \
                  --data '{"state":"error",
                  "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
                  "description":"Javadoc build failed, check build log!",
                  "context":"javadoc"}' \
                  "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
      fi
fi
echo "Finished javadoc gradle..."
