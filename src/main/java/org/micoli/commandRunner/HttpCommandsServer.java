package org.micoli.commandRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import fi.iki.elonen.NanoHTTPD;



public class HttpCommandsServer extends NanoHTTPD {
	private String cmdPrefix="/cmd/";
	private Executor executor;

	public HttpCommandsServer(Executor executor) throws IOException {
		super(8081);
		this.executor=executor;
	}

	private String readFile(String path, Charset encoding)	 throws IOException{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();
		System.out.println(method + " '" + uri + "' ");
		String html = "";

		if(uri.startsWith(cmdPrefix)){
			String subMethod = uri.replace(cmdPrefix, "");
			if(executor.hasCommand(subMethod)){
				html = executor.execute(subMethod, new CommandArgs(session.getParms()));
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
}