package net.sourceforge.peers.botUserAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.sip.SipUtils;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transport.SipRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.micoli.api.PluginsManager;
import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.CommandRoute;
import org.micoli.api.commandRunner.CommandRunner;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.botUserAgent.BotExtension;
import org.micoli.botUserAgent.BotsManagerApi;
import org.micoli.botUserAgent.BotsManagerExtension;
import org.micoli.botUserAgent.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotsManager implements CommandRunner,BotsManagerApi{
	protected final Logger				logger = LoggerFactory.getLogger(getClass());

	private static ExecutorService		executorService;
	private HashMap<String, String>		loadedScripts;
	private HashMap<String, BotAgent>	botAgents;
	private HashMap<String, SipRequest>	sipRequests;
	private Iterator<PeerConfig>		iterator;
	private ScriptEngine				engine;
	private Boolean						customBindAddr;
	private Bindings					engineScope;
	private File						workingDirectory;

	private ExecutorRouter	executorRouter;

	public BotsManager() {
		this.sipRequests	= new HashMap<String, SipRequest>();
		workingDirectory	= new File(GlobalConfig.getConfig().getString(GlobalConfig.optScriptPath)).getAbsoluteFile();
		loadedScripts		= new HashMap<String, String> ();
		botAgents			= new HashMap<String, BotAgent> ();
		engine				= new ScriptEngineManager().getEngineByName("nashorn");
		engineScope			= engine.getBindings(ScriptContext.ENGINE_SCOPE);
		executorService		= Executors.newCachedThreadPool();
		customBindAddr		= (!GlobalConfig.getConfig().getInetAddress(GlobalConfig.optBindAddr).equals(GlobalConfig.getOptBindAddr().getDefault()));

		engineScope.put("workingDirectory"	, workingDirectory);
		engineScope.put("global"			, engineScope);
	}

	public static ExecutorService getExecutorService() {
		return executorService;
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	public Invocable getInvocableEngine() {
		return (Invocable) engine;
	}

	public void storeSipRequest(final SipRequest sipRequest){
		String callId = SipUtils.getCallId(sipRequest);
		if(!sipRequests.containsKey(callId)){
			sipRequests.put(callId, sipRequest);
		}
	}

	public SipRequest getSipRequest(String callId){
		if(sipRequests.containsKey(callId)){
			return sipRequests.get(callId);
		}
		return null;
	}

	public boolean removeSipRequest(String callId){
		if(sipRequests.containsKey(callId)){
			sipRequests.remove(callId);
			return true;
		}
		return false;
	}

	private void loadScript(String sFilename) throws FileNotFoundException, ScriptException{
		if(!loadedScripts.containsKey(sFilename)){
			loadedScripts.put(sFilename,sFilename);
			logger.debug("load script :: "+sFilename);
			engine.eval("load('"+sFilename+"');");
		}
	}

	private Thread getCleanUp(){
		return (new Thread() {
			public void run() {
				PluginsManager.stop();
				for (Map.Entry<String, BotAgent> entry : botAgents.entrySet()) {
					String sId = entry.getKey();
					BotAgent botAgent = entry.getValue();
					botAgent.unregister();
					logger.info(sId + " unregistered ");
				}
			}
		});
	}

	public void run() throws IOException, ParseException {
		System.setProperty("user.dir"		, workingDirectory.toString());

		Runtime.getRuntime().addShutdownHook(this.getCleanUp());
		List<PeerConfig> peersList = PeerConfig.readPeersConf();
		try{
			loadScript(workingDirectory.toString() + "/run.js");
			loadScript(workingDirectory.toString() + "/behaviours/_default.js");

			iterator = peersList.iterator();
			while (iterator.hasNext()) {
				PeerConfig config = iterator.next();
				loadScript(workingDirectory.toString() + "/behaviours/" + config.getBehaviour()+".js");
			}

			iterator = peersList.iterator();
			while (iterator.hasNext()) {
				PeerConfig config = iterator.next();
				if(customBindAddr){
					config.setLocalInetAddress(GlobalConfig.getConfig().getInetAddress(GlobalConfig.optBindAddr));
				}
				logger.info(config.getId()+" :: "+config.getUserPart()+"@"+config.getDomain()+":"+config.getSipPort()+" ["+config.getPassword()+"] "+config.getBehaviour());
				botAgents.put(config.getId(),new BotAgent(this,config));
			}

			setExecutorRouter(new ExecutorRouter());

			PluginsManager.startGenericCommands(getExecutorRouter(),this);

			getExecutorRouter().attachRouteForExtension(ExecutorRouter.root, BotsManagerExtension.class, this);
			for (Map.Entry<String, BotAgent> botAgentKV : botAgents.entrySet()) {
				getExecutorRouter().attachRouteForExtension(botAgentKV.getKey(), BotExtension.class, botAgentKV.getValue());
			}
			getExecutorRouter().displayRoute();
			//START global extensions

		} catch (NullPointerException e) {
			logger.error(e.getClass().getSimpleName(), e);
		} catch (FileNotFoundException e) {
			logger.error(e.getClass().getSimpleName(), e);
		} catch (ScriptException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
	}

	/*@CommandRoute(value="bot",args={"from","action"})
	public String runBotCommand(CommandArgs commandArgs) {
		String botId = commandArgs.getDefault("from", "0");
		if(!botAgents.containsKey(botId)){
			return "Agent ["+botId+"] not found";
		}
		try{
			return botAgents.get(botId).execute(commandArgs.get("action"),commandArgs);
		}catch(Exception e){
			return "Error : " + e.getMessage();
		}
	}*/

	/*
	public String runCommand(String command) {
		if(command.equalsIgnoreCase("r") && !lastCommand.equalsIgnoreCase("")){
			command = lastCommand;
		}
		String[] tokens = command.split(" ");
		if(tokens.length<2){
			return "Not enough arguments";
		}
		if(!botAgents.containsKey(tokens[0])){
			return "Agent ["+tokens[0]+"] not found";
		}

		lastCommand = command;

		try{
			botAgents.get(tokens[0]).sendCommand(tokens[1],Arrays.copyOfRange(tokens,2,tokens.length));
		}catch(Exception e){
			return "Error : " + e.getMessage();
		}
		return "OK";
	}*/

	//CliLoggerOutput
	public void javaLog(final String message) {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSExec("javaLog", new Object[]{message});
			}
		});
	}

	//CliLoggerOutput
	public void javaNetworkLog(final String message) {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSExec("javaNetworkLog", new Object[]{message});
			}
		});
	}

	public void JSExec(String method,Object[] arguments){
		try {
			this.getInvocableEngine().invokeFunction(method, arguments);
		} catch (NoSuchMethodException e) {
			logger.error(e.getClass().getSimpleName(), e);
		} catch (ScriptException e) {
			System.err.println("JS Error : "+ method + ", args " + arguments+" "+e.getFileName()+"("+e.getLineNumber()+','+e.getColumnNumber() +")");
			logger.error(e.getClass().getSimpleName(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@CommandRoute(value="list")
	public String list(CommandArgs args) {
		JSONArray list = new JSONArray();
		for (Map.Entry<String, BotAgent> entry : botAgents.entrySet()) {
			String id = entry.getKey();
			BotAgent botAgent = entry.getValue();
			JSONObject bot = new JSONObject();
			bot.put("id", id);
			bot.put("status", botAgent.getLastStatus());
			Dialog activeCall = botAgent.getActiveCall();
			if(activeCall==null){
				bot.put("activeCall",null);
			}else{
				Map activeCallMap = new LinkedHashMap();
				activeCallMap.put("callid", activeCall.getCallId());
				activeCallMap.put("remoteUri", activeCall.getRemoteUri());
				activeCallMap.put("localUri", activeCall.getLocalUri());
				activeCallMap.put("state", activeCall.getState().getClass().getSimpleName());
				bot.put("activeCall",activeCallMap);
			}
			list.add(bot);
		}
		return list.toJSONString();
	}

	public ExecutorRouter getExecutorRouter() {
		return executorRouter;
	}

	private void setExecutorRouter(ExecutorRouter executorRouter) {
		this.executorRouter = executorRouter;
	}

}