#!/bin/bash

echo "Build-testing commit $TRAVIS_COMMIT"
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
      curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
            --request POST \
            --data '{"state":"pending",
            "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
            "description": "Build in progress...",
            "context":"build"}' \
            "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
            
      echo "Start Build gradle..."
      
      if ./gradlew build -DXmx2g; then
            curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
                  --request POST \
                  --data '{"state":"success",
                  "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
                  "description":"Build passed!",
                  "context":"build"}' \
                  "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
      else
            curl -H "Content-Type: application/json" -H "Authorization: token $GITHUB_TOKEN" \
                  --request POST \
                  --data '{"state":"error",
                  "target_url":"https://travis-ci.org/Minecolonies/minecolonies/builds/'$TRAVIS_BUILD_ID'",
                  "description":"Build failed, check build log!",
                  "context":"build"}' \
                  "https://api.github.com/repos/Minecolonies/minecolonies/statuses/${TRAVIS_COMMIT}"
      fi
else
      ./gradlew build
fi
echo "Finished Build gradle..."
