#!/bin/bash

#cd ~/Documents/workspace/marytts/marytts-runtime
#mvn install -Dmaven.test.skip -DskipTests

#cd ~/Documents/workspace/botUserAgent/botRunner-soundTTS
#mvn package -Dmaven.test.skip -DskipTests

cd ~/Documents/workspace/botUserAgent/botRunner-api
mvn install -Dmaven.test.skip -DskipTests

cd ~/Documents/workspace/botUserAgent/botRunner
mvn package -Dmaven.test.skip -DskipTests
java -jar target/botRunner-0.5.1-SNAPSHOT.jar

cd ..
