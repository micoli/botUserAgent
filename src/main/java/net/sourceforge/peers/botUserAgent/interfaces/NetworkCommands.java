package net.sourceforge.peers.botUserAgent.interfaces;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.sourceforge.peers.botUserAgent.BotsManager;
import net.sourceforge.peers.botUserAgent.misc.ManagedThread;

public class NetworkCommands extends ManagedThread {
	private BotsManager	botsManager;

	public NetworkCommands(BotsManager botsManager) {
		this.botsManager=botsManager;
	}

	@Override
	public void run() {
		isRunning = true;
		ServerSocket Soc;
		try {
			Soc = new ServerSocket(5217);
			while(isRunning()){
				Socket CSoc=Soc.accept();
				new NetworkCommandsClient(CSoc,this.botsManager);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}