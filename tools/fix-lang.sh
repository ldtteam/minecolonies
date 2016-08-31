sed -i.bak 's/ = /=/g;s/\\//g;s/^\(.*\)=$/#\1=/g;' ../src/main/resources/assets/minecolonies/lang/*.lang && rm ../src/main/resources/assets/minecolonies/lang/*.bak
