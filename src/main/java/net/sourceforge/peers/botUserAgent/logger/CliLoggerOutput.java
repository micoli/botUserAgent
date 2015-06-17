package net.sourceforge.peers.botUserAgent.logger;

public interface CliLoggerOutput {

	public void javaLog(String message);
	public void javaNetworkLog(String message);

}
