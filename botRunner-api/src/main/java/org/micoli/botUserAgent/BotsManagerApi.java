package org.micoli.botUserAgent;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import net.sourceforge.peers.sip.transport.SipRequest;

public interface BotsManagerApi {
	public ScriptEngine getEngine();
	public Invocable getInvocableEngine();
	public void storeSipRequest(final SipRequest sipRequest);
	public SipRequest getSipRequest(String callId);
	public boolean removeSipRequest(String callId);
}
