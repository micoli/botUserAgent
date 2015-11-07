package net.sourceforge.peers.botUserAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.peers.botUserAgent.sip.SipUtils;

import org.apache.log4j.PropertyConfigurator;
import org.json.simple.parser.ParseException;
import org.micoli.api.PluginsManager;
import org.micoli.api.resourceManager;
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

		String log4jproperties = GlobalConfig.getLog4jproperties();
		if(!log4jproperties.isEmpty()){
			logger.info("Log4jproperties: "+log4jproperties);
			PropertyConfigurator.configure(log4jproperties);
		}

		logger.debug("Parse Arguments :",GlobalConfig.getConfigs());

		final File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		resourceManager.extractResources(jar,"plugins/"			,GlobalConfig.getPluginPath());
		resourceManager.extractResources(jar,"scripts/"			,GlobalConfig.getScriptPath());
		resourceManager.extractResources(jar,"peers.conf.json"	,GlobalConfig.getPeersConfigFile());

		File pluginDir = new File(GlobalConfig.getPluginPath());
		System.setProperty("pf4j.pluginsDir", pluginDir.getAbsolutePath());

		BotsManager botsManager = new BotsManager();
		PluginsManager.init(botsManager);
		PluginsManager.start();
		botsManager.run();
	}
}