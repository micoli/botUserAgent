#!/bin/bash

#cd ~/Documents/workspace/marytts/marytts-runtime
#mvn clean install -Dmaven.test.skip -DskipTests

cd ~/Documents/workspace/botUserAgent/botRunner-api
mvn clean install -Dmaven.test.skip -DskipTests

#cd ~/Documents/workspace/botUserAgent/botRunner-soundTTS
#mvn clean package -Dmaven.test.skip -DskipTests

cd ~/Documents/workspace/botUserAgent/plugin-jslib/
mvn clean package -Dmaven.test.skip -DskipTests

cd ~/Documents/workspace/botUserAgent/botRunner
mvn clean package -Dmaven.test.skip -DskipTests
java -jar target/botRunner-0.5.1-SNAPSHOT.jar

cd ..
