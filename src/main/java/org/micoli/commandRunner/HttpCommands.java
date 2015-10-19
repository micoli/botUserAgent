package org.micoli.commandRunner;

import java.io.IOException;


public class HttpCommands extends GenericCommands {

	public HttpCommands(Executor executor) {
		super(executor);
	}

	public void run() {
		try {
			HttpCommandsServer httpCommandsServer = new HttpCommandsServer(executor);
			httpCommandsServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
