package net.sourceforge.peers.botUserAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import net.sourceforge.peers.sip.transport.SipRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.micoli.commandRunner.CommandArgs;
import org.micoli.commandRunner.CommandRoute;
import org.micoli.commandRunner.CommandRunner;
import org.micoli.commandRunner.GenericCommands;
import org.micoli.http.Client;
//http://127.0.0.1:8081/cmd/list
//http://127.0.0.1:8081/cmd/bot?action=call&from=6000&to=6001@192.168.1.26
public class BotsManager implements CliLoggerOutput,CommandRunner  {
	private HashMap<String, String>			loadedScripts;
	private HashMap<String, BotUserAgent>	botUserAgents;
	private Iterator<PeerConfig>			iterator;
	private HashMap<String, SipRequest>		sipRequests;
	private ScriptEngine					engine;
	private ExecutorService					executorService;
	private Object							botsMutex;
	private Logger							logger;
	private Boolean							customBindAddr;

	public ExecutorService getExecutorService() {
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
				for (Map.Entry<String, BotUserAgent> entry : botUserAgents.entrySet()) {
					String sId = entry.getKey();
					BotUserAgent botUserAgent = entry.getValue();
					botUserAgent.unregister();
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
		botUserAgents		= new HashMap<String, BotUserAgent> ();
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
				botUserAgents.put(config.getId(),new BotUserAgent(this,config,logger));
			}

			GenericCommands.startInterfaces(this);

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
		if(!botUserAgents.containsKey(botId)){
			return "Agent ["+botId+"] not found";
		}
		try{
			return botUserAgents.get(botId).execute(commandArgs.get("action"),commandArgs);
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
		if(!botUserAgents.containsKey(tokens[0])){
			return "Agent ["+tokens[0]+"] not found";
		}

		lastCommand = command;

		try{
			botUserAgents.get(tokens[0]).sendCommand(tokens[1],Arrays.copyOfRange(tokens,2,tokens.length));
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
		for (Map.Entry<String, BotUserAgent> entry : botUserAgents.entrySet()) {
			String id = entry.getKey();
			//BotUserAgent botUserAgent = entry.getValue();
			JSONObject o = new JSONObject();
			o.put("id", id);
			list.add(o);
		}

		return list.toJSONString();
	}
}