#botUserAgent

##Installation
* Peers lib (SIP/RTP layer)
  * git clone https://github.com/micoli/peers.git
  * cd peers
  * mvn install; cd ..

* NanoHttpd
  * git clone https://github.com/NanoHttpd/nanohttpd.git
  * cd nanohttpd
  * mvn install;cd ..

* google-auto
  * git clone https://github.com/google/auto.git
  * cd auto/service
  * mvn install;cd ../..

* Mary TTS
  * git clone https://github.com/micoli/marytts.git
  * cd marytts
  * mvn install;cd ..

* Pf4J (plugin framework for java)
  * git clone https://github.com/decebals/pf4j.git
  * cd pf4j
  * mvn install;cd ..


* botUserAgent
  * git clone https://github.com/micoli/botUserAgent.git
  * mvn package
  * cd botRunner
  * mkdir plugins
  * cd plugins
  * ln -s ../../plugin-*/target/*.zip .
  * cd ..
  * java -jar target/botRunner-0.5.1-SNAPSHOT.jar -a XXX.YYY.ZZZ.AAA
  * (XXX.YYY.ZZZ.AAA = visible IP for sip user agents)

##Execution
$> java -jar target/botRunner-0.5.1-SNAPSHOT.jar

Usage: java net.sourceforge.peers.botUserAgent.Main [-p <peersConfigFile>] [-a <bindAddr>] [-s <scriptPath>] [-o <scriptOverloadPath>] [-l <log4jproperties>] [-h <pluginPath>] [-n <logTraceNetwork>]
[-p <peersConfigFile>]
		(default: src/main/resources/peers.conf.json)

[-a <bindAddr>]
		(default: 0.0.0.0)

[-s <scriptPath>]
		(default: src/main/resources/scripts/)

[-o <scriptOverloadPath>]
		(default: src/main/resources/scripts/overload)

[-l <log4jproperties>]
		(default: )

[-h <pluginPath>]
		(default: plugins)

[-n <logTraceNetwork>]
		(default: 0)


##Todo
	* add a route parameter to execute a route globally or only for a bot
	* server configuration in json
	* TTS methods
	* http server configuration
	* websocket server functionnal
	* external agents without registration
	* extraction of internal scripts/services/behaviours

	* voice RAW
