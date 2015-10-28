package net.sourceforge.peers.botUserAgent;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.sip.SipUtils;

import org.json.simple.parser.ParseException;
import org.micoli.api.PluginsManager;

import com.martiansoftware.jsap.JSAPException;

public class Main {

	public static void main(String[] args) throws JSAPException, FileNotFoundException, IOException, ParseException {
		SipUtils.init();

		System.getProperty("pf4j.pluginsDir", "plugins");

		PluginsManager.init();
		PluginsManager.start();

		if(!GlobalConfig.parseArgs(args)){
			System.exit(1);
		}
		BotsManager botsManager= new BotsManager();
		botsManager.run();
	}
}