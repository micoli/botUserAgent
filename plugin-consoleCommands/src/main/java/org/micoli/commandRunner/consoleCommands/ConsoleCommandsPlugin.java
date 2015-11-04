package org.micoli.commandRunner.consoleCommands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.threads.ManagedThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class ConsoleCommandsPlugin extends Plugin {
	protected final static Logger logger = LoggerFactory.getLogger(ConsoleCommandsPlugin.class);
	static ManagedThread thread;

	public ConsoleCommandsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		logger.debug("ConsoleCommandsPlugin.start()");
	}

	@Override
	public void stop() {
		thread.setRunning(false);
		logger.debug("ConsoleCommandsPlugin.stop()");
	}

	@Extension
	public static class ConsoleCommands implements GenericCommands{
		public void launch(final ExecutorRouter executorRouter) {
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			thread=new ManagedThread(){
				@Override
				public void run() {
					setRunning(true);
					while (isRunning()) {
						String command;
						try {
							command = bufferedReader.readLine();
						} catch (IOException e) {
							logger.error(e.getClass().getSimpleName(), e);
							break;
						}
						String out = executorRouter.executeCommand(command.trim());
						System.out.println(out);
						logger.debug("Console command execute :"+command.trim()+":"+out);
					}
				}
			};
			thread.start();
		}
	}
}