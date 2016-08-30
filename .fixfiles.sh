#!/bin/bash
echo "Skipping xml rewrite"
#find -iname "pom.xml" -exec ./.xmlrewrite.rb '{}' \;

echo "Fixing file permissions"
find -type f -exec chmod a-x '{}' \;
find -type f -iname "*.sh" -exec chmod 755 '{}' \;

