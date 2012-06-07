#! /bin/bash
#
# To run this script you may need to do: chmod u+x /pathTo/build.sh
################################################################################

clear 

# Build the needed paths  
cd "`dirname "$0"`"
BUILD_DIR="`pwd`"

cd $BUILD_DIR/../class 
CLASS_DIR="`pwd`" 

cd $BUILD_DIR/../../
JAR_DIR="`pwd`" 

cd $BUILD_DIR

# Delete old files
rm -f -r $CLASS_DIR/com/github/alinaioanaflorea/xmldiff/*.class
rm -f -r $JAR_DIR/xmlDiff.jar

# Build the class files
javac -g $BUILD_DIR/../java/com/github/alinaioanaflorea/xmldiff/*.java -Xlint:unchecked -d $CLASS_DIR

# Build the executable jar(java archive/package) file
cd $CLASS_DIR
jar cvmf $BUILD_DIR/mainClass.mf $JAR_DIR/xmlDiff.jar ./com/github/alinaioanaflorea/xmldiff/*.class

# Give your user execution rights
chmod u+x $JAR_DIR/xmlDiff.jar

# List the content of the jar file:
echo 
echo "The content of xmlDiff.jar is:"
jar tvf $JAR_DIR/xmlDiff.jar

