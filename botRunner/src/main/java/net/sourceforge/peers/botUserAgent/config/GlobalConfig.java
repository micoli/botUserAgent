package net.sourceforge.peers.botUserAgent.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.peers.botUserAgent.Main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

public class GlobalConfig{
	protected final static Logger logger = LoggerFactory.getLogger(Main.class);
	private static Map<String,FlaggedOption> options = new HashMap<String,FlaggedOption>();
	private static JSAPResult config;
	private static JSAP jsap = new JSAP();

	public static boolean parseArgs(String[] args) throws JSAPException{
		init();
		setConfig(jsap.parse(args));

		if (!getConfig().success()) {
			showUsage();
			return false;
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static void showUsage(){
		for (java.util.Iterator errs = getConfig().getErrorMessageIterator();
				errs.hasNext();) {
			logger.error("Error: " + errs.next());
		}

		logger.error("");
		logger.error("Usage: java "+ Main.class.getName());
		logger.error("            "+ jsap.getUsage());
		logger.error("");
		logger.error(jsap.getHelp());
	}

	private static void init() throws JSAPException{
		options.put("optPeersConfigFile",new FlaggedOption("peersConfigFile")
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/peers.conf.json")
			.setShortFlag('p')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optPeersConfigFile"));

		options.put("optBindAddr", new FlaggedOption("bindAddr")
			.setStringParser(JSAP.INETADDRESS_PARSER)
			.setDefault("0.0.0.0")
			.setShortFlag('a')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optBindAddr"));

		options.put("optScriptPath", new FlaggedOption("scriptPath")
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/scripts/")
			.setShortFlag('s')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optScriptPath"));

		options.put("optScriptOverloadPath", new FlaggedOption("scriptOverloadPath")
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/scripts/overload")
			.setShortFlag('o')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optScriptOverloadPath"));

		options.put("optLogDebug", new FlaggedOption("logDebug")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("0")
			.setShortFlag('d')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optLogDebug"));

		options.put("optLogInfo", new FlaggedOption("logInfo")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("1")
			.setShortFlag('i')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optLogInfo"));

		options.put("optLogError", new FlaggedOption("logError")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("1")
			.setShortFlag('e')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optLogError"));

		options.put("optLogTraceNetwork", new FlaggedOption("logTraceNetwork")
			.setStringParser(JSAP.BOOLEAN_PARSER)
			.setDefault("0")
			.setShortFlag('n')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get("optLogTraceNetwork"));
	}

	@SuppressWarnings("unchecked")
	public static List<PeerConfig> readPeersConf() throws FileNotFoundException, IOException, ParseException{
		ArrayList<PeerConfig> peers= new ArrayList<PeerConfig>();
		JSONParser parser = new JSONParser();
		String peersConfFilename = GlobalConfig.getConfig().getString("peersConfigFile");
		logger.info("Peers conf :: "+peersConfFilename);
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

	public static FlaggedOption getOptPeersConfigFile() {
		return options.get("optPeersConfigFile");
	}

	public static FlaggedOption getOptBindAddr() {
		return options.get("optBindAddr");
	}

	public static FlaggedOption getOptScriptPath() {
		return options.get("optScriptPath");
	}

	public static FlaggedOption getOptScriptOverloadPath() {
		return options.get("optScriptOverloadPath");
	}

	public static FlaggedOption getOptLogDebug() {
		return options.get("optLogDebug");
	}

	public static FlaggedOption getOptLogInfo() {
		return options.get("optLogInfo");
	}

	public static FlaggedOption getOptLogError() {
		return options.get("optLogError");
	}

	public static FlaggedOption getOptLogTraceNetwork() {
		return options.get("optLogTraceNetwork");
	}

	public static Object getConfigs() {
		String configStr = "";
		String sepa = "";
		for(Entry<String,FlaggedOption> option : options.entrySet()) {
			//String key = option.getKey();
			FlaggedOption value = option.getValue();
			configStr=configStr+sepa+value.getShortFlag()+"::"+value.getLongFlag()+" ["+value.toString()+"]";
			sepa=", ";
		}
		return configStr;
	}

	public static JSAPResult getConfig() {
		return GlobalConfig.config;
	}
	public static void setConfig(JSAPResult config) {
		GlobalConfig.config = config;
	}


}
