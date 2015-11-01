package org.micoli.botUserAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

public class GlobalConfig{
	protected final static Logger logger = LoggerFactory.getLogger(GlobalConfig.class);
	private static Map<String,FlaggedOption> options = new HashMap<String,FlaggedOption>();

	private static JSAPResult config;
	private static JSAP jsap = new JSAP();
	private static String mainName = "main";

	public static boolean parseArgs(String _mainName,String[] args) throws JSAPException{
		mainName = _mainName;

		init();
		setConfig(jsap.parse(args));

		if (!getConfig().success()) {
			showUsage();
			return false;
		}
		return true;
	}

	private static void init() throws JSAPException{
		setOptPeersConfigFile();
		setOptBindAddr();
		setOptScriptPath();
		setOptScriptOverloadPath();
		setOptLog4jproperties();
		setOptPluginPath();
		setOptLogTraceNetwork();
	}

	@SuppressWarnings("rawtypes")
	public static void showUsage(){
		for (java.util.Iterator errs = getConfig().getErrorMessageIterator();
				errs.hasNext();) {
			logger.error("Error: " + errs.next());
		}

		logger.error("");
		logger.error("Usage: java "+ mainName);
		logger.error("            "+ jsap.getUsage());
		logger.error("");
		logger.error(jsap.getHelp());
	}

	public static JSAPResult getConfig() {
		return GlobalConfig.config;
	}
	public static void setConfig(JSAPResult config) {
		GlobalConfig.config = config;
	}

	/**
	 *
	 */
	final public static String optPeersConfigFile = "peersConfigFile";
	public static FlaggedOption getOptPeersConfigFile() {
		return options.get(optPeersConfigFile);
	}
	private static void setOptPeersConfigFile() throws JSAPException{
		options.put(optPeersConfigFile,new FlaggedOption(optPeersConfigFile)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/peers.conf.json")
			.setShortFlag('p')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optPeersConfigFile));
	}

	/**
	 *
	 */
	final public static String optBindAddr = "bindAddr";
	public static FlaggedOption getOptBindAddr() {
		return options.get(optBindAddr);
	}
	private static void setOptBindAddr() throws JSAPException{
		options.put(optBindAddr, new FlaggedOption(optBindAddr)
			.setStringParser(JSAP.INETADDRESS_PARSER)
			.setDefault("0.0.0.0")
			.setShortFlag('a')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optBindAddr));
	}

	/**
	 *
	 */
	final public static String optScriptPath = "scriptPath";
	public static FlaggedOption getOptScriptPath() {
		return options.get(optScriptPath);
	}
	private static void setOptScriptPath() throws JSAPException{
		options.put(optScriptPath, new FlaggedOption(optScriptPath)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/scripts/")
			.setShortFlag('s')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optScriptPath));
	}

	/**
	 *
	 */
	final public static String optScriptOverloadPath = "scriptOverloadPath";
	public static FlaggedOption getOptScriptOverloadPath() {
		return options.get(optScriptOverloadPath);
	}
	private static void setOptScriptOverloadPath() throws JSAPException{
		options.put(optScriptOverloadPath, new FlaggedOption(optScriptOverloadPath)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("src/main/resources/scripts/overload")
			.setShortFlag('o')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optScriptOverloadPath));
	}

	/**
	 *
	 */
	final public static String optLog4jproperties = "log4jproperties";
	public static FlaggedOption getOptLog4jproperties() {
		return options.get(optLog4jproperties);
	}
	private static void setOptLog4jproperties() throws JSAPException{
		options.put(optLog4jproperties, new FlaggedOption(optLog4jproperties)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("")
			.setShortFlag('l')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optLog4jproperties));
	}

	/**
	 *
	 */
	final public static String optPluginPath = "pluginPath";
	public static FlaggedOption getOptPluginPath() {
		return options.get(optPluginPath);
	}
	private static void setOptPluginPath() throws JSAPException{
		options.put(optPluginPath, new FlaggedOption(optPluginPath)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("plugins")
			.setShortFlag('h')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optPluginPath));
	}

	/**
	 *
	 */
	final public static String optLogTraceNetwork = "logTraceNetwork";
	public static FlaggedOption getOptLogTraceNetwork() {
		return options.get(optLogTraceNetwork);
	}
	private static void setOptLogTraceNetwork() throws JSAPException{
		options.put(optLogTraceNetwork, new FlaggedOption(optLogTraceNetwork)
		.setStringParser(JSAP.BOOLEAN_PARSER)
		.setDefault("0")
		.setShortFlag('n')
		.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optLogTraceNetwork));
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

}