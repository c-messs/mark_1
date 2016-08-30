#!/bin/bash
echo "Reformatting all poms"
find -iname "pom.xml" -exec ./.xmlrewrite.rb '{}' \;


