package org.micoli.commandRunner;

import java.io.IOException;

import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.RuntimeMode;

public class WebCommandsPlugin extends Plugin {
	protected final static Logger logger = LoggerFactory.getLogger(WebCommandsPlugin.class);

	public WebCommandsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		logger.debug("WebCommandsPlugin.start()");
	}

	@Override
	public void stop() {
		logger.debug("WebCommandsPlugin.stop()");
	}

	@Extension
	public static class WebCommands implements GenericCommands {
		@Override
		public void launch(final ExecutorRouter executor){
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