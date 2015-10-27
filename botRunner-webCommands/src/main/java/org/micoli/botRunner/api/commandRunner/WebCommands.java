package org.micoli.botRunner.api.commandRunner;

import java.io.IOException;

import org.micoli.api.commandRunner.Executor;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.threads.ManagedThread;

public class WebCommands extends GenericCommands {

	public WebCommands(Executor executor) {
		super(executor);
	}

	public void launch() {
		ManagedThread thread=new ManagedThread(){
			@Override
			public void run() {
				try {
					WebCommandsHttpServer webCommandsHttpServer = new WebCommandsHttpServer(executor,8081);
					webCommandsHttpServer.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
}
