package net.sourceforge.peers.botUserAgent.logger;

public interface WebLoggerOutput {

	public void javaLog(String message);
	public void javaNetworkLog(String message);

}
