package org.micoli.commandRunner.webCommands;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;

public class WebCommandsWSSocket extends WebSocket {
	protected final static Logger logger = LoggerFactory.getLogger(WebCommandsWSSocket.class);

	public WebCommandsWSSocket(IHTTPSession handshakeRequest) {
		super(handshakeRequest);
		logger.debug("WebCommandsWSSocket " + handshakeRequest.getUri());
	}

	@Override
	protected void onPong(WebSocketFrame pongFrame) {
		logger.debug("P " + pongFrame);
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
		logger.debug("C ["
						+ (initiatedByRemote ? "Remote" : "Self")
						+ "] "
						+ (code != null ? code : "UnknownCloseCode[" + code
								+ "]")
						+ (reason != null && !reason.isEmpty() ? ": "
								+ reason : ""));
	}

	@Override
	protected void onException(IOException e) {
		logger.error(e.getClass().getSimpleName(), e);
	}

	@Override
	protected void handleWebsocketFrame(WebSocketFrame frame) throws IOException {
		logger.debug("R " + frame);
		super.handleWebsocketFrame(frame);
	}

	@Override
	public synchronized void sendFrame(WebSocketFrame frame) throws IOException {
		logger.debug("S " + frame);
		//frame.setTextPayload("{\"toto\":1}");
		super.sendFrame(frame);
	}
}