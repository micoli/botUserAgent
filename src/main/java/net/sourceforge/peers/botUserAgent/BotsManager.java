package net.sourceforge.peers.botUserAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.interfaces.ConsoleCommands;
import net.sourceforge.peers.botUserAgent.interfaces.NetworkCommands;

import org.json.simple.parser.ParseException;

public class BotsManager  {
	private HashMap<String, String> loadedScripts;
	private HashMap<String, BotUserAgent> botUserAgents;
	private Iterator<PeerConfig> iterator;
	private ConsoleCommands	consoleCommands;
	private NetworkCommands	oTCPCommandsReader;
	private ScriptEngine engine = null;

	private void loadScript(String sFilename) throws FileNotFoundException, ScriptException{
		if(!loadedScripts.containsKey(sFilename)){
			loadedScripts.put(sFilename,sFilename);
			System.out.println("Script :: "+sFilename);
			engine.eval(new FileReader(sFilename));
		}
	}
	public void run() throws IOException, ParseException {
		File workingDirectory = new File(GlobalConfig.config.getString("scriptPath")).getAbsoluteFile();

		System.setProperty("user.dir", workingDirectory.toString());

		ExecutorService	executorService = Executors.newCachedThreadPool();
		engine = new ScriptEngineManager().getEngineByName("nashorn");
		loadedScripts = new HashMap<String, String> ();
		botUserAgents = new HashMap<String, BotUserAgent> ();
		engine.getBindings(ScriptContext.ENGINE_SCOPE).put("workingDirectory", workingDirectory);
		try{
			Boolean customBindAddr = (!GlobalConfig.config.getInetAddress("bindAddr").equals(GlobalConfig.getOptBindAddr().getDefault()));

			loadScript(workingDirectory.toString() + "/run.js");
			loadScript(workingDirectory.toString() + "/behaviours/_default.js");

			List<PeerConfig> peersList = GlobalConfig.readPeersConf();

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
				System.out.println(config.getId()+" :: "+config.getUserPart()+"@"+config.getDomain()+":"+config.getSipPort()+" ["+config.getPassword()+"] "+config.getBehaviour());
				botUserAgents.put(config.getId(),new BotUserAgent(engine,executorService,config));
			}
			consoleCommands = new ConsoleCommands(this);
			consoleCommands.start();
			oTCPCommandsReader = new NetworkCommands(this);
			oTCPCommandsReader.start();

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}

	}

	public String runCommand(String command) {
		String[] tokens = command.split(" ");
		if(tokens.length<2){
			return "Not enough arguments";
		}

		if(!botUserAgents.containsKey(tokens[0])){
			return "Agent ["+tokens[0]+"] not found";
		}

		try{
			botUserAgents.get(tokens[0]).sendCommand(tokens[1],Arrays.copyOfRange(tokens,2,tokens.length));
		}catch(Exception e){
			//e.printStackTrace();
			return "Error : " + e.getMessage();
		}
		return "OK";
	}
}