#botUserAgent

##Installation
* Peers lib (SIP/RTP layer)
  * git clone https://github.com/micoli/peers.git
  * cd peers
  * mvn install; cd ..

* NanoHttpd
  * git clone https://github.com/NanoHttpd/nanohttpd.git
  * cd nanohttpd
  * mvn install;cd..

* Mary TTS
  * git clone https://github.com/micoli/marytts.git
  * cd marytts
  * mvn install;cd..

* Pf4J (plugin framework for java)
  * git clone https://github.com/decebals/pf4j.git
  * cd pf4j
  * mvn install;cd..

* botUserAgent
  * git clone https://github.com/micoli/botUserAgent.git
  * mvn package


##Execution
$> java -jar target/botUserAgent-0.5.1-SNAPSHOT.jar

Usage: java net.sourceforge.peers.botUserAgent.Main
			[-p <peersConfigFile>] [-a <bindAddr>] [-s <scriptPath>] [-o <scriptOverloadPath>] [-d <logDebug>] [-i <logInfo>] [-e <logError>] [-n <logTraceNetwork>]

	[-p <peersConfigFile>]
		(default: src/main/resources/peers.conf.json)

	[-a <bindAddr>]
		(default: 0.0.0.0)

	[-s <scriptPath>]
		(default: src/main/resources/scripts/)

	[-o <scriptOverloadPath>]
		(default: src/main/resources/scripts/overload)

	[-d <logDebug>]
		(default: 0)

	[-i <logInfo>]
		(default: 1)

	[-e <logError>]
		(default: 1)

	[-n <logTraceNetwork>]
		(default: 0)



##Todo
	* server configuration in json
	* TTS methods
	* http server configuration
	* websocket server functionnal
	* external agents without registration
	* extraction of internal scripts/services/behaviours


	* log4j.properties
	* voice RAW
