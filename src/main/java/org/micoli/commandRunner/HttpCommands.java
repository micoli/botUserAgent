package org.micoli.commandRunner;

import java.io.IOException;


public class HttpCommands extends GenericCommands {

	public HttpCommands(CommandRunner commandRunner) {
		super(commandRunner);
	}

	public void run() {
		try {
			HttpCommandsServer httpCommandsServer = new HttpCommandsServer();
			httpCommandsServer.setCommandRunner(commandRunner);
			httpCommandsServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}