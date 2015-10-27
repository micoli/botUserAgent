package org.micoli.commandRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.ExecutorRouter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWebSocketServer;
import fi.iki.elonen.WebSocket;


public class WebCommandsHttpServer extends NanoWebSocketServer {
	private String cmdPrefix="/cmd/";
	private ExecutorRouter executorRouter;
	private boolean	debug = true;

	public WebCommandsHttpServer(ExecutorRouter executorRouter, int port) throws IOException {
		super("0.0.0.0",port);
		this.executorRouter=executorRouter;
	}

	private String readFile(String path, Charset encoding)	 throws IOException{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public Response serve(final IHTTPSession session) {
		if (isWebsocketRequested(session)) {
			return super.serve(session);
		}

		//Map<String, String> headers = session.getHeaders();
		//Method method = session.getMethod();
		String uri = session.getUri();
		String html = "";

		if(uri.startsWith(cmdPrefix)){
			String subMethod = uri.replace(cmdPrefix, "");
			if(executorRouter.hasCommand(subMethod)){
				html = executorRouter.execute(subMethod, new CommandArgs(session.getParms()));
			}else{
				html = "No such command : "+subMethod;
			}
		}else{
			if(uri.equals("/")){
				uri="/index.html";
			}
			String filename = "../www"+uri;
			File f = new File(filename);
			try {
				html = readFile(f.getAbsoluteFile().getCanonicalPath(), Charset.defaultCharset());
			} catch (IOException e) {
				html = "404 "+uri+" "+e.getMessage();
			}
		}
		return new NanoHTTPD.Response(html);
	}

	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new WebCommandsWSSocket(handshake);
	}
}