package net.sourceforge.peers.botUserAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sourceforge.peers.botUserAgent.sip.SipUtils;

import org.apache.log4j.PropertyConfigurator;
import org.json.simple.parser.ParseException;
import org.micoli.api.PluginsManager;
import org.micoli.botUserAgent.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPException;

public class Main {
	protected final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws JSAPException, FileNotFoundException, IOException, ParseException {
		logger.debug("Initialisation");

		SipUtils.init();

		if(!GlobalConfig.parseArgs(Main.class.getName(),args)){
			System.exit(1);
		}

		String log4jproperties = GlobalConfig.getConfig().getString(GlobalConfig.optLog4jproperties);
		if(!log4jproperties.isEmpty()){
			logger.info("Log4jproperties: "+log4jproperties);
			PropertyConfigurator.configure(log4jproperties);
		}

		logger.debug("Parse Arguments :",GlobalConfig.getConfigs());

		File pluginDir = new File(GlobalConfig.getConfig().getString(GlobalConfig.optPluginPath));
		System.setProperty("pf4j.pluginsDir", pluginDir.getAbsolutePath());

		checkAndDumpResources("scripts"			,GlobalConfig.getConfig().getString(GlobalConfig.optScriptPath));
		checkAndDumpResources("peers.conf.json"	,GlobalConfig.getConfig().getString(GlobalConfig.optPeersConfigFile));

		BotsManager botsManager = new BotsManager();
		PluginsManager.init(botsManager);
		PluginsManager.start();
		botsManager.run();
	}

	private static void checkAndDumpResources(String jarPath,String destinationPath) {
		final File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());

		try {
			if(jarFile.isFile()) {  // Run with JAR file
				JarFile jar = new JarFile(jarFile);
				final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				while(entries.hasMoreElements()) {
					final String name = entries.nextElement().getName();
					if (name.startsWith(jarPath + "/")) { //filter according to the path
						System.out.println(name +"=>"+destinationPath+name);
					}
				}
				jar.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}