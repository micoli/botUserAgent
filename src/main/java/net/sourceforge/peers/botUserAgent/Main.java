/**
	This file is an extends of Peers, a java SIP softphone.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

	@author o.michaud
	inspired from Yohann Martineau Copyright 2010
 */

//mvn clean package;java -jar target/ccphone-0.1-SNAPSHOT-jar-with-dependencies.jar

package net.sourceforge.peers.botUserAgent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;

import org.json.simple.parser.ParseException;

import com.martiansoftware.jsap.JSAPException;

/**
 * The Class Main.
 */
public class Main {

	@SuppressWarnings("restriction")
	public static void main(String[] args) throws JSAPException, FileNotFoundException, IOException, ParseException {
		HashMap<String, String> loadedBehaviours = new LinkedHashMap<String, String> ();
		Iterator<PeerConfig> iterator;
		ExecutorService	executorService = Executors.newCachedThreadPool();
		try {
			if(!GlobalConfig.parseArgs(args)){
				System.exit(1);
			}

			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
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
				System.out.println(config.getId()+" :: "+config.getUserPart()+"@"+config.getDomain()+":"+config.getSipPort()+" ["+config.getPassword()+"] "+config.getBehaviour());
				new BotUserAgent(engine,executorService,config);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}
}