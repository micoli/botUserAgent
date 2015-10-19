package org.micoli.commandRunner;

import java.io.IOException;


public class HttpCommands extends GenericCommands {

	public HttpCommands(CommandRunner commandRunner) {
		super(commandRunner);
	}

	public void run() {
		try {
			HttpCommandsServer httpCommandsServer = new HttpCommandsServer();
<<<<<<< HEAD
			httpCommandsServer.setCommandRunner(commandRunner);
=======
>>>>>>> branch 'master' of ssh://git@github.com/micoli/botUserAgent.git
			httpCommandsServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
