package org.micoli.commandRunner;

import java.io.IOException;

import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.RuntimeMode;

public class WebCommandsPlugin extends Plugin {

	public WebCommandsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		System.out.println("WebCommandsPlugin.start()");
		// for testing the development mode
		if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
			System.out.println("WelcomePlugin");
		}
	}

	@Override
	public void stop() {
		System.out.println("WebCommandsPlugin.stop()");
	}

	@Extension
	public static class WebCommands implements GenericCommands {
		@Override
		public void launch(final ExecutorRouter executor){
			System.out.println("WebCommands");
			Thread thread=new Thread(){
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
}