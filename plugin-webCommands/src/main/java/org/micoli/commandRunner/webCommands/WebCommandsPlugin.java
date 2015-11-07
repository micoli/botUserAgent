package org.micoli.commandRunner.webCommands;

import java.io.IOException;
import java.util.Properties;

import org.micoli.api.DefaultPluginManagerExt;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class WebCommandsPlugin extends Plugin {
	protected final static Logger logger = LoggerFactory.getLogger(WebCommandsPlugin.class);
	private static Properties	config;

	public WebCommandsPlugin(PluginWrapper wrapper) {
		super(wrapper);
		config = ((DefaultPluginManagerExt) wrapper.getPluginManager()).getPluginConfig(wrapper.getPluginId());
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
						WebCommandsHttpServer webCommandsHttpServer = new WebCommandsHttpServer(executor,Integer.parseInt((String)config.getProperty("httpport")));
						webCommandsHttpServer.start();
					} catch (IOException e) {
						logger.error(e.getClass().getSimpleName(), e);
					}
				}
			};
			thread.start();
		}
	}
}