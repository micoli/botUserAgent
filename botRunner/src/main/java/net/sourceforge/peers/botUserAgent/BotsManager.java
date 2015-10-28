package net.sourceforge.peers.botUserAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.logger.CliLogger;
import net.sourceforge.peers.botUserAgent.logger.CliLoggerOutput;
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
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.http.Client;
public class BotsManager implements CliLoggerOutput,CommandRunner  {
	private HashMap<String, String>		loadedScripts;
	private HashMap<String, BotAgent>	botAgents;
	private Iterator<PeerConfig>		iterator;
	private HashMap<String, SipRequest>	sipRequests;
	private ScriptEngine				engine;
	private static ExecutorService		executorService;
	private Logger						logger;
	private Boolean						customBindAddr;

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
			System.out.println("load script :: "+sFilename);
			engine.eval("load('"+sFilename+"');");
			//nodeServer.run(sFilename);
			//engine.eval(new FileReader(sFilename));
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
		File workingDirectory;
		Bindings engineScope;
		logger = new CliLogger(this);
		this.sipRequests	= new HashMap<String, SipRequest>();
		workingDirectory	= new File(GlobalConfig.config.getString("scriptPath")).getAbsoluteFile();
		loadedScripts		= new HashMap<String, String> ();
		botAgents		= new HashMap<String, BotAgent> ();
		engine				= new ScriptEngineManager().getEngineByName("nashorn");
		engineScope			= engine.getBindings(ScriptContext.ENGINE_SCOPE);
		executorService		= Executors.newCachedThreadPool();
		customBindAddr		= (!GlobalConfig.config.getInetAddress("bindAddr").equals(GlobalConfig.getOptBindAddr().getDefault()));

		System.setProperty("user.dir"		, workingDirectory.toString());
		engineScope.put("window"			, engineScope);
		engineScope.put("workingDirectory"	, workingDirectory);
		engineScope.put("http"				, new Client(this.logger));

		Runtime.getRuntime().addShutdownHook(this.getCleanUp());
		List<PeerConfig> peersList = GlobalConfig.readPeersConf();
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
					config.setLocalInetAddress(GlobalConfig.config.getInetAddress("bindAddr"));
				}
				logger.info(config.getId()+" :: "+config.getUserPart()+"@"+config.getDomain()+":"+config.getSipPort()+" ["+config.getPassword()+"] "+config.getBehaviour());
				botAgents.put(config.getId(),new BotAgent(this,config,logger));
			}

			PluginsManager.startGenericCommands(this);

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	@CommandRoute(value="bot",args={"from","action"})
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
	}

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
		this.getExecutorService().submit(new Runnable() {
			public void run() {
				JSExec("javaLog", new Object[]{message});
			}
		});
	}

	//CliLoggerOutput
	public void javaNetworkLog(final String message) {
		this.getExecutorService().submit(new Runnable() {
			public void run() {
				JSExec("javaNetworkLog", new Object[]{message});
			}
		});
	}

	public void JSExec(String method,Object[] arguments){
		try {
			this.getInvocableEngine().invokeFunction(method, arguments);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			System.err.println("JS Error : "+ method + ", args " + arguments+" "+e.getFileName()+"("+e.getLineNumber()+','+e.getColumnNumber() +")");
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "unused", "rawtypes", "serial" })
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

	private static BufferedReader getOutput(Process p) {
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}

	private static BufferedReader getError(Process p) {
		return new BufferedReader(new InputStreamReader(p.getErrorStream()));
	}

	/*private static void test() {
		MaryTTS mary = new MaryTTS();
		try {
			mary.saveAudio("bonjour, ceci est un test 123 quatre cinq six", "/tmp/toto1234.wav");
		} catch (SynthesisException | InterruptedException e) {
			e.printStackTrace();
		}
	}*/
}