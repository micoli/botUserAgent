package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.micoli.api.commandRunner.Executor;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.threads.ManagedThread;

public class ConsoleCommands implements GenericCommands {
	@Override
	public String start() {
		return null;
	}
	public void launch(Executor executor) {
		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		ManagedThread thread=new ManagedThread(){
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
					System.out.println(executor.executeCommand(command.trim()));
				}
			}
		};
		thread.start();
	}
}