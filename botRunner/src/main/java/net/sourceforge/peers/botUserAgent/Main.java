package net.sourceforge.peers.botUserAgent;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.sip.SipUtils;

import org.json.simple.parser.ParseException;
import org.micoli.api.PluginsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPException;
//import org.apache.log4j.PropertyConfigurator;

public class Main {
	protected final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws JSAPException, FileNotFoundException, IOException, ParseException {
		logger.debug("Initialisation");
		SipUtils.init();

		//PropertyConfigurator.configure("log4j.properties");
		//PropertyConfigurator.configure(getClass().getResource("/controlador/log4j.properties"));
		//PropertyConfigurator

		if(!GlobalConfig.parseArgs(args)){
			System.exit(1);
		}
		logger.debug("Parse Arguments :",GlobalConfig.getConfigs());

		System.getProperty("pf4j.pluginsDir", "plugins");

		BotsManager botsManager = new BotsManager();
		PluginsManager.init(botsManager);
		PluginsManager.start();
		botsManager.run();
	}
}