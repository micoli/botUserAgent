package net.sourceforge.peers.botUserAgent;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;

import org.json.simple.parser.ParseException;

import com.martiansoftware.jsap.JSAPException;

public class Main {

	public static void main(String[] args) throws JSAPException, FileNotFoundException, IOException, ParseException {

		if(!GlobalConfig.parseArgs(args)){
			System.exit(1);
		}
		BotsManager botsManager= new BotsManager();
		botsManager.run();
	}
}