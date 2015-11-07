#!/bin/bash

#cd ~/Documents/workspace/marytts/marytts-runtime
#mvn clean install -Dmaven.test.skip -DskipTests

#cd ~/Documents/workspace/botUserAgent/botRunner-api
#mvn clean install -Dmaven.test.skip -DskipTests

#cd ~/Documents/workspace/botUserAgent/botRunner-soundTTS
#mvn clean package -Dmaven.test.skip -DskipTests

#cd ~/Documents/workspace/botUserAgent/plugin-jslib/
#mvn clean package -Dmaven.test.skip -DskipTests

#mvn clean install

mvn clean package -Dmaven.test.skip -DskipTests
rm -rf dist
mkdir dist
ln -s plugin-*/target/*.zip dist/plugins/.

java -jar botRunner/target/botRunner-0.5.1-SNAPSHOT.jar

