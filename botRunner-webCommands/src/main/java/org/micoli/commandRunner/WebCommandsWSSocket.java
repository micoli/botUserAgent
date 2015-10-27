package org.micoli.commandRunner;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;

public class WebCommandsWSSocket extends WebSocket {
	private final boolean	DEBUG;

	public WebCommandsWSSocket(IHTTPSession handshakeRequest) {
		super(handshakeRequest);
		System.out.println("WebCommandsWSSocket " + handshakeRequest.getUri());
		DEBUG = true;
	}

	@Override
	protected void onPong(WebSocketFrame pongFrame) {
		if (DEBUG) {
			System.out.println("P " + pongFrame);
		}
	}

	@Override
	protected void onMessage(WebSocketFrame messageFrame) {
		try {
			messageFrame.setUnmasked();
			sendFrame(messageFrame);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onClose(WebSocketFrame.CloseCode code, String reason,boolean initiatedByRemote) {
		if (DEBUG) {
			System.out
					.println("C ["
							+ (initiatedByRemote ? "Remote" : "Self")
							+ "] "
							+ (code != null ? code : "UnknownCloseCode[" + code
									+ "]")
							+ (reason != null && !reason.isEmpty() ? ": "
									+ reason : ""));
		}
	}

	@Override
	protected void onException(IOException e) {
		e.printStackTrace();
	}

	@Override
	protected void handleWebsocketFrame(WebSocketFrame frame) throws IOException {
		if (DEBUG) {
			System.out.println("R " + frame);
		}
		super.handleWebsocketFrame(frame);
	}

	@Override
	public synchronized void sendFrame(WebSocketFrame frame) throws IOException {
		if (DEBUG) {
			System.out.println("S " + frame);
		}
		//frame.setTextPayload("{\"toto\":1}");
		super.sendFrame(frame);
	}
}