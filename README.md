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
  * git clone https://github.com/micoli/pf4j.git
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
```
$> java -jar target/botRunner-0.5.1-SNAPSHOT.jar
Usage: java net.sourceforge.peers.botUserAgent.Main [-p <peersConfigFile>] [-a <bindAddr>] [-s <scriptPath>] [-l <log4jproperties>] [-h <pluginPath>] [-n <logTraceNetwork>]
[-p <peersConfigFile>]
		(default: resources/peers.conf.json)

[-a <bindAddr>]
		(default: 0.0.0.0)

[-s <scriptPath>]
		(default: resources/scripts/)

[-l <log4jproperties>]
		(default: none/default file is integrated)

[-h <pluginPath>]
		(default: plugins)

[-n <logTraceNetwork>]
		(default: 0)
```

##configuration files
	Each plugin has its own configuration files. default values are in plugins/pluginXXX/plugin.properties and can be overloaded by plugins/pluginXXX.properties
	* botrunner-jslib.conf
		* none

	* botrunner-consoleCommands.conf
		* none

	* botrunner-soundTTS.conf
		* voicespath (/tmp/voices/)
		* tmppath (/tmp/)

	* botrunner-networkCommands.conf
		* tcpport (5217)

	* botrunner-webCommands.conf
		* httpport (8081)

##Todo
	* websocket server functionnal
	* external agents without registration
	* extraction of internal scripts/services/behaviours



