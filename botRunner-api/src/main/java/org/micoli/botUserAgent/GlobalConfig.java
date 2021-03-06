package org.micoli.botUserAgent;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;

public class GlobalConfig{
	protected final static Logger logger = LoggerFactory.getLogger(GlobalConfig.class);
	private static Map<String,Parameter> options = new HashMap<String,Parameter>();

	private static JSAPResult config;
	private static JSAP jsap = new JSAP();
	private static String mainName = "main";

	public static boolean parseArgs(String _mainName,String[] args) throws JSAPException{
		mainName = _mainName;

		init();
		setConfig(jsap.parse(args));

		if (!getConfig().success() || getShowUsage()) {
			showUsage();
			return false;
		}
		return true;
	}

	private static void init() throws JSAPException{
		setOptShowUsage();
		setOptBindAddr();
		setOptPeersConfigFile();
		setOptScriptPath();
		setOptPluginPath();
		setOptLog4jproperties();
		setOptLogTraceNetwork();
	}

	@SuppressWarnings("rawtypes")
	public static void showUsage(){
		for (java.util.Iterator errs = getConfig().getErrorMessageIterator();
				errs.hasNext();) {
			logger.error("Error: " + errs.next());
		}

		logger.error("");
		logger.error("Usage: java "+ mainName+" "+ jsap.getUsage()+"\n"+jsap.getHelp());
	}

	public static JSAPResult getConfig() {
		return GlobalConfig.config;
	}
	public static void setConfig(JSAPResult config) {
		GlobalConfig.config = config;
	}

	public static Object getConfigs() {
		String configStr = "";
		String sepa = "";
		for(Entry<String,Parameter> option : options.entrySet()) {
			//String key = option.getKey();
			if(option.getClass().isAssignableFrom(FlaggedOption.class)){
				FlaggedOption value = (FlaggedOption) option.getValue();
				configStr=configStr+sepa+value.getShortFlag()+"::"+value.getLongFlag()+" ["+value.toString()+"]";
			}
			sepa=", ";
		}
		return configStr;
	}

	private static String ensureTrailingSlash(String value){
		return value + (value.substring(value.length() - 1).equals("/")?"":"/");
	}

	/**
	 *
	 */
	final public static String optShowUsage = "help";
	private static void setOptShowUsage() throws JSAPException{
		options.put(optShowUsage, new Switch(optShowUsage)
		.setDefault("0")
		.setShortFlag('h')
		.setLongFlag("help"));
		jsap.registerParameter(options.get(optShowUsage));
	}
	public static boolean getShowUsage() {
		return GlobalConfig.getConfig().getBoolean(GlobalConfig.optShowUsage);
	}

	/**
	 *
	 */
	final public static String optPeersConfigFile = "peersConfigFile";
	private static void setOptPeersConfigFile() throws JSAPException{
		options.put(optPeersConfigFile,new FlaggedOption(optPeersConfigFile)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("dist/peers.conf.json")
			.setShortFlag('c')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optPeersConfigFile));
	}
	public static FlaggedOption getOptPeersConfigFile() {
		return (FlaggedOption) options.get(optPeersConfigFile);
	}
	public static String getPeersConfigFile() {
		return GlobalConfig.getConfig().getString(GlobalConfig.optPeersConfigFile);
	}

	/**
	 *
	 */
	final public static String optBindAddr = "bindAddr";
	private static void setOptBindAddr() throws JSAPException{
		options.put(optBindAddr, new FlaggedOption(optBindAddr)
			.setStringParser(JSAP.INETADDRESS_PARSER)
			.setDefault("0.0.0.0")
			.setShortFlag('a')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optBindAddr));
	}
	public static FlaggedOption getOptBindAddr() {
		return (FlaggedOption) options.get(optBindAddr);
	}
	public static InetAddress getBindAddr() {
		return GlobalConfig.getConfig().getInetAddress(GlobalConfig.optBindAddr);
	}

	/**
	 *
	 */
	final public static String optScriptPath = "scriptPath";
	private static void setOptScriptPath() throws JSAPException{
		options.put(optScriptPath, new FlaggedOption(optScriptPath)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("dist/scripts/")
			.setShortFlag('s')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optScriptPath));
	}
	public static FlaggedOption getOptScriptPath() {
		return (FlaggedOption) options.get(optScriptPath);
	}
	public static String getScriptPath() {
		return ensureTrailingSlash(GlobalConfig.getConfig().getString(GlobalConfig.optScriptPath));
	}

	/**
	 *
	 */
	final public static String optLog4jproperties = "log4jproperties";
	private static void setOptLog4jproperties() throws JSAPException{
		options.put(optLog4jproperties, new FlaggedOption(optLog4jproperties)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("")
			.setShortFlag('l')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optLog4jproperties));
	}
	public static FlaggedOption getOptLog4jproperties() {
		return (FlaggedOption) options.get(optLog4jproperties);
	}
	public static String getLog4jproperties() {
		return GlobalConfig.getConfig().getString(GlobalConfig.optLog4jproperties);
	}

	/**
	 *
	 */
	final public static String optPluginPath = "pluginPath";
	private static void setOptPluginPath() throws JSAPException{
		options.put(optPluginPath, new FlaggedOption(optPluginPath)
			.setStringParser(JSAP.STRING_PARSER)
			.setDefault("dist/plugins/")
			.setShortFlag('p')
			.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optPluginPath));
	}
	public static FlaggedOption getOptPluginPath() {
		return (FlaggedOption) options.get(optPluginPath);
	}
	public static String getPluginPath() {
		return ensureTrailingSlash(GlobalConfig.getConfig().getString(GlobalConfig.optPluginPath));
	}

	/**
	 *
	 */
	final public static String optLogTraceNetwork = "logTraceNetwork";
	private static void setOptLogTraceNetwork() throws JSAPException{
		options.put(optLogTraceNetwork, new FlaggedOption(optLogTraceNetwork)
		.setStringParser(JSAP.BOOLEAN_PARSER)
		.setDefault("0")
		.setShortFlag('n')
		.setLongFlag(JSAP.NO_LONGFLAG));
		jsap.registerParameter(options.get(optLogTraceNetwork));
	}
	public static FlaggedOption getOptLogTraceNetwork() {
		return (FlaggedOption) options.get(optLogTraceNetwork);
	}
	public static boolean getLogTraceNetwork() {
		return GlobalConfig.getConfig().getBoolean(GlobalConfig.optLogTraceNetwork);
	}

}