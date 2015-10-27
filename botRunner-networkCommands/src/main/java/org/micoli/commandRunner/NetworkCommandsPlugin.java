package org.micoli.commandRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.threads.ManagedThread;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class NetworkCommandsPlugin extends Plugin {
	static ManagedThread thread;
	public NetworkCommandsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		System.out.println("NetworkCommandsPlugin.start()");
	}

	@Override
	public void stop() {
		thread.setRunning(false);
		System.out.println("NetworkCommandsPlugin.stop()");
	}

	@Extension
	public static class NetworkCommands implements GenericCommands{
		@Override
		public void launch(final ExecutorRouter executorRouter) {
			thread=new ManagedThread(){
				@Override
				public void run() {
					setRunning(true);
					ServerSocket Soc;
					try {
						Soc = new ServerSocket(5217);
						while(isRunning()){
							Socket CSoc=Soc.accept();
							new NetworkCommandsClient(CSoc,executorRouter);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}
}