package org.micoli.commandRunner;

import java.io.IOException;


public class WebCommands extends GenericCommands {

	public WebCommands(Executor executor) {
		super(executor);
	}

	public void run() {
		try {
			WebCommandsHttpServer webCommandsHttpServer = new WebCommandsHttpServer(executor,8081);
			webCommandsHttpServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
