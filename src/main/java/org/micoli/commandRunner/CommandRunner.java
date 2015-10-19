package org.micoli.commandRunner;

public interface CommandRunner {
	public String runCommand(String command);
	public String getStatus(String key);
}
