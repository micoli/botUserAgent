package net.sourceforge.peers.botUserAgent.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sourceforge.peers.botUserAgent.BotsManager;
import net.sourceforge.peers.botUserAgent.misc.ManagedThread;

public class ConsoleCommands extends ManagedThread {
	private BotsManager	botsManager;

	public ConsoleCommands(BotsManager botsManager) {
		this.botsManager = botsManager;
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
			System.out.println(botsManager.runCommand(command));
		}
	}
}