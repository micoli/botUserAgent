package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.micoli.threads.ManagedThread;

public class ConsoleCommands extends GenericCommands {
	public ConsoleCommands(Executor executor) {
		super(executor);
	}

	public void run() {
		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
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
}