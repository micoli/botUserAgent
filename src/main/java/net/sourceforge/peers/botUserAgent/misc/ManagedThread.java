package net.sourceforge.peers.botUserAgent.misc;

public class ManagedThread extends Thread {
	protected boolean isRunning;

	public synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

}
