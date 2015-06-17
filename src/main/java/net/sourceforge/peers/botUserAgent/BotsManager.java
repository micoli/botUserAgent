package net.sourceforge.peers.botUserAgent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;

import org.json.simple.parser.ParseException;

public class BotsManager  {
	private HashMap<String, String> loadedBehaviours;
	private HashMap<String, BotUserAgent> botUserAgents;
	private Iterator<PeerConfig> iterator;
	private CommandsReader	commandsReader;
	
	@SuppressWarnings("restriction")
	public void run() throws IOException, ParseException {
		ExecutorService	executorService = Executors.newCachedThreadPool();
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		loadedBehaviours = new HashMap<String, String> ();
		botUserAgents = new HashMap<String, BotUserAgent> ();
		try{
			Boolean customBindAddr = (!GlobalConfig.config.getInetAddress("bindAddr").equals(GlobalConfig.getOptBindAddr().getDefault()));

			engine.eval(new FileReader(GlobalConfig.config.getString("scriptPath") + "/runtime.js"));
			engine.eval(new FileReader(GlobalConfig.config.getString("scriptPath") + "/run.js"));

			List<PeerConfig> peersList = GlobalConfig.readPeersConf();

			iterator = peersList.iterator();
			while (iterator.hasNext()) {
				PeerConfig config = iterator.next();
				if(!loadedBehaviours.containsKey(config.getBehaviour())){
					loadedBehaviours.put(config.getBehaviour(),config.getBehaviour());
					engine.eval(new FileReader(GlobalConfig.config.getString("scriptPath") + "/"+config.getBehaviour()+".js"));
				}
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
			commandsReader = new CommandsReader(this);
			commandsReader.start();

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}

	}

	public boolean runCommand(String command) {
		String[] tokens = command.split(" ");
		if(tokens.length<2){
			System.out.println("Not enough arguments");
			return false;
		}
		if(!botUserAgents.containsKey(tokens[0])){
			System.out.println("Agent not found");
			return false;
		}
		return botUserAgents.get(tokens[0]).sendCommand(tokens[1],Arrays.copyOfRange(tokens,2,tokens.length));
	}
}