package org.micoli.botRunner.api.commandRunner;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.micoli.api.commandRunner.Executor;
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
			System.out.println(StringUtils.upperCase("WelcomePlugin"));
		}
	}

	@Override
	public void stop() {
		System.out.println("WebCommandsPlugin.stop()");
	}

	@Extension
	public static class WebCommands implements GenericCommands {
		@Override
		public String start() {
			return null;
		}

		@Override
		public void launch(final Executor executor){
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