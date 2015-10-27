package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.threads.ManagedThread;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class ConsoleCommandsPlugin extends Plugin {
	static ManagedThread thread;
	public ConsoleCommandsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		System.out.println("ConsoleCommandsPlugin.start()");
	}

	@Override
	public void stop() {
		thread.setRunning(false);
		System.out.println("ConsoleCommandsPlugin.stop()");
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
							e.printStackTrace();
							break;
						}
						System.out.println(executorRouter.executeCommand(command.trim()));
					}
				}
			};
			thread.start();
		}
	}
}