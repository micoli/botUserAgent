package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.micoli.api.commandRunner.Executor;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.threads.ManagedThread;


public class NetworkCommands extends GenericCommands {
	public NetworkCommands(Executor executor) {
		super(executor);
	}
	public void launch() {
		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		ManagedThread thread=new ManagedThread(){
			@Override
			public void run() {
				isRunning = true;
				ServerSocket Soc;
				try {
					Soc = new ServerSocket(5217);
					while(isRunning()){
						Socket CSoc=Soc.accept();
						new NetworkCommandsClient(CSoc,executor);
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