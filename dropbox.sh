cat > ~/.dropbox_uploader <<EOL
APPKEY=${APPKEY}
APPSECRET=${APPSECRET}
ACCESS_LEVEL=${ACCESS_LEVEL}
OAUTH_ACCESS_TOKEN=${OAUTH_ACCESS_TOKEN}
OAUTH_ACCESS_TOKEN_SECRET=${OAUTH_ACCESS_TOKEN_SECRET}
EOL

curl "https://raw.githubusercontent.com/andreafabrizi/Dropbox-Uploader/master/dropbox_uploader.sh" -o ~/dropbox_uploader.sh
chmod +x ~/dropbox_uploader.sh
~/dropbox_uploader.sh mkdir "$TRAVIS_BRANCH"
