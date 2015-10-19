package org.micoli.commandRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class NetworkCommands extends GenericCommands {
	public NetworkCommands(Executor executor) {
		super(executor);
	}

	@Override
	public void run() {
		isRunning = true;
		ServerSocket Soc;
		try {
			Soc = new ServerSocket(5217);
			while(isRunning()){
				Socket CSoc=Soc.accept();
				new NetworkCommandsClient(CSoc,this.executor);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}