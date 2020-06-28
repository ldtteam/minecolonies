#!/bin/sh
# Change this folder as needed
find ../src/main/resources/assets/minecolonies/sounds/mob/deliveryman/female -regex ".*[A-Z].*" | while read old; do
    new="$(echo "$old" | tr '[A-Z]' '[a-z]')"
    git mv -v "$old" "$new"
  done
