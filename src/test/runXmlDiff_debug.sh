#! /bin/bash
#
# To run this script you may need to do: chmod u+x /pathTo/runXmlDiff_debug.sh
################################################################################

cd "`dirname "$0"`" 

java -DxmlDiff.isDebugBuild=true -jar ./../../xmlDiff.jar ./input/old.xml ./input/new.xml ./input/mandatoryTags.xml
