package net.sourceforge.peers.botUserAgent.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.peers.botUserAgent.Main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

public class GlobalConfig{
	public static JSAPResult config;
	private static JSAP jsap = new JSAP();
	private static FlaggedOption optPeersConfigFile;
	private static FlaggedOption optBindAddr;
	private static FlaggedOption optScriptPath;
	private static FlaggedOption optScriptOverloadPath;
	private static FlaggedOption optLogDebug;
	private static FlaggedOption optLogInfo;
	private static FlaggedOption optLogError;
	private static FlaggedOption optLogTraceNetwork;

	@SuppressWarnings("rawtypes")
	public static void showUsage(){
		System.err.println();
		for (java.util.Iterator errs = config.getErrorMessageIterator();
				errs.hasNext();) {
			System.err.println("Error: " + errs.next());
		}

		System.err.println();
		System.err.println("Usage: java "+ Main.class.getName());
		System.err.println("            "+ jsap.getUsage());
		System.err.println();
		System.err.println(jsap.getHelp());
	}

	@SuppressWarnings("unchecked")
	public static List<PeerConfig> readPeersConf() throws FileNotFoundException, IOException, ParseException{
		ArrayList<PeerConfig> peers= new ArrayList<PeerConfig>();
		JSONParser parser = new JSONParser();
		String peersConfFilename = GlobalConfig.config.getString("peersConfigFile");
		System.out.println("conf "+peersConfFilename);
		Object obj = parser.parse(new FileReader(peersConfFilename));

		JSONObject jsonConf = (JSONObject) obj;

		JSONArray peersList = (JSONArray) jsonConf.get("peers");

		Iterator<JSONObject> iterator = peersList.iterator();
		while (iterator.hasNext()) {
			PeerConfig config = new PeerConfig();
			JSONObject jsonPeer = iterator.next();
			setPeerConfigKey(config,"id"		,jsonPeer,jsonConf);
			setPeerConfigKey(config,"user"		,jsonPeer,jsonConf);
			setPeerConfigKey(config,"password"	,jsonPeer,jsonConf);
			setPeerConfigKey(config,"domain"	,jsonPeer,jsonConf);
			setPeerConfigKey(config,"behaviour"	,jsonPeer,jsonConf);
			peers.add(config);
		}
		return peers;
	}

	private static void setPeerConfigKey(PeerConfig config,String key,JSONObject jsonPeer,JSONObject jsonConf){
		if(jsonPeer.containsKey(key)){
			config.setKey(key,(String)jsonPeer.get(key));
		}
		if(jsonPeer.containsKey(key)){
			config.setKey(key,(String)jsonPeer.get(key));
		}else{
			if(jsonConf.containsKey("default")){
				JSONObject jsonDefault = (JSONObject) jsonConf.get("default");
				if(jsonDefault.containsKey(key)){
					config.setKey(key,(String)jsonDefault.get(key));
				}
			}
		}
	}

	public static boolean parseArgs(String[] args) throws JSAPException{
		optPeersConfigFile = new FlaggedOption("peersConfigFile")
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/peers.conf.json")
			.setShortFlag('p')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optPeersConfigFile);

		optBindAddr = new FlaggedOption("bindAddr")
			.setStringParser(JSAP.INETADDRESS_PARSER)
			.setDefault("0.0.0.0")
			.setShortFlag('a')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optBindAddr);

		optScriptPath = new FlaggedOption("scriptPath")
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/scripts/")
			.setShortFlag('s')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optScriptPath);

		optScriptOverloadPath = new FlaggedOption("scriptOverloadPath")
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/scripts/overload")
			.setShortFlag('o')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optScriptOverloadPath);

		optLogDebug = new FlaggedOption("logDebug")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("0")
			.setShortFlag('d')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optLogDebug);

		optLogInfo = new FlaggedOption("logInfo")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("1")
			.setShortFlag('i')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optLogInfo);

		optLogError = new FlaggedOption("logError")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("1")
			.setShortFlag('e')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optLogError);

		optLogTraceNetwork = new FlaggedOption("logTraceNetwork")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("0")
			.setShortFlag('n')
			.setLongFlag(JSAP.NO_LONGFLAG);
		jsap.registerParameter(optLogTraceNetwork);

		config = jsap.parse(args);

		if (!config.success()) {
			showUsage();
			return false;
		}
		return true;
	}

	public static FlaggedOption getOptPeersConfigFile() {
		return optPeersConfigFile;
	}

	public static FlaggedOption getOptBindAddr() {
		return optBindAddr;
	}

	public static FlaggedOption getOptScriptPath() {
		return optScriptPath;
	}

	public static FlaggedOption getOptScriptOverloadPath() {
		return optScriptOverloadPath;
	}

	public static FlaggedOption getOptLogDebug() {
		return optLogDebug;
	}

	public static FlaggedOption getOptLogInfo() {
		return optLogInfo;
	}

	public static FlaggedOption getOptLogError() {
		return optLogError;
	}

	public static FlaggedOption getOptLogTraceNetwork() {
		return optLogTraceNetwork;
	}


}
