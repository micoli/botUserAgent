package org.micoli.commandRunner.webCommands;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWebSocketServer;
import fi.iki.elonen.WebSocket;


public class WebCommandsHttpServer extends NanoWebSocketServer {
	protected final static Logger logger = LoggerFactory.getLogger(WebCommandsHttpServer.class);

	private String cmdPrefix="/cmd/";
	private ExecutorRouter executorRouter;

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

			Map<String, String> parms = session.getParms();
			if(!parms.containsKey("from")){
				parms.put("from", "000");
			}
			if(executorRouter.hasCommand(parms.get("from"),subMethod)){
				String res = executorRouter.execute(parms.get("from"),subMethod, new CommandArgs(parms));
				html = res;
				logger.debug("Console command execute :"+html+":"+session.getParms()+":"+res);
			}else{
				html = "No such command : "+subMethod;
				logger.error(html);
			}
		}else{
			if(uri.equals("/")){
				uri="/index.html";
			}
			String filename = "../www"+uri;
			File f = new File(filename);
			try {
				html = readFile(f.getAbsoluteFile().getCanonicalPath(), Charset.defaultCharset());
				logger.debug("Web get: "+f.getAbsoluteFile().getCanonicalPath());
			} catch (IOException e) {
				html = "404 "+uri+" "+e.getMessage();
				logger.error(html);
			}
		}
		return new NanoHTTPD.Response(html);
	}

	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new WebCommandsWSSocket(handshake);
	}
}