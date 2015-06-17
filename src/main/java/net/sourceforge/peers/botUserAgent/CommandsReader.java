package net.sourceforge.peers.botUserAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandsReader extends Thread {

	public static final String CALL = "call";
	public static final String HANGUP = "hangup";

	private boolean isRunning;
	private BotsManager	botsManager;

	public CommandsReader(BotsManager botsManager) {
		this.botsManager=botsManager;
	}

	@Override
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
			command = command.trim();
			if (botsManager.runCommand(command)) {
				System.out.println("");
			} else {
				System.out.println("Unknown command " + command);
			}
		}
	}

	public synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
}
