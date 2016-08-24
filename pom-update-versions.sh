#!/bin/bash -e
#
# Script to update all the EE poms.
#
# This should actually be done with mvn versions:set -DnewVersion=${NEW}
#

OLD=$1
NEW=$2

function usage {
    echo $1
    echo "Edits all poms and substitutes OLD for NEW version numbers of Parent"
    echo "usage: $0 {OLD} {NEW}"
    exit 1
}

[ -z $OLD ] && usage "Please provide OLD and NEW versions"
[ -z $NEW ] && usage "Please provide NEW and OLD versions"

echo "Updating all POMs from $OLD to $NEW in `pwd`..."
echo "    (includes count of how many pom files exist)"

set -x
find . -name pom.xml -print -exec sed -i "1,12 s/${OLD}/${NEW}/" "{}" \; | wc -l
set +x 

echo  "Updated all POMs from $OLD to $NEW in `pwd`..."

# Count how many files we updated that have the new versions.
# If there are more lines than number of files, it usually means
# that there are disconnected POMs or there are duplicate hard-wired
# lines.
echo "Line count in files with new version $NEW:"
find . -name pom.xml -exec grep -nH "${NEW}" "{}" \; | wc -l

# Lines that still contain the old version. These are usually dependent files that should 
# use "project.version" instead of a hard-wired version.
echo "Remaining lines with old version ${OLD}:"
echo "Any lines here should be fixed to not use specific versions."
find . -name pom.xml -exec grep -nH "${OLD}" "{}" \;

echo "Replacing remaining lines in all files:"
find . -name pom.xml -print -exec sed -i "s/${OLD}/${NEW}/" "{}" \; | wc -l

echo "Final line count in files with new version $NEW:"
find . -name pom.xml -exec grep -nH "${NEW}" "{}" \; | wc -l



