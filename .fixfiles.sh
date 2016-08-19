#!/bin/bash
find -iname "pom.xml" -exec ./.xmlrewrite.rb '{}' \;

