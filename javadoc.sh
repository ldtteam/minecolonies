
curl -H "Content-Type: application/json" -H "authToken: $GITHUB_TOKEN" \
      "https://api.github.com/repos/Minecolonies/minecolonies/commits/$TRAVIS_COMMIT/statuses"
      

./gradlew javadoc


