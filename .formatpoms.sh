#!/bin/bash
echo "Reformatting all poms"
find -iname "pom.xml" -print -exec ./.xmlrewrite.rb '{}' \;


