package org.micoli.commandRunner;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;



public class HttpCommandsServer extends NanoHTTPD {

	private CommandRunner commandRunner;

	public HttpCommandsServer() throws IOException {
		super(8081);
	}

	@Override
	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();
		Map<String, String> parms = session.getParms();
		System.out.println(method + " '" + uri + "' ");
		String html = "";

		switch(uri){
			case "/list":
				html = commandRunner.getStatus("list");
			break;
			default:
				html += "<html><body><h1>Hello server</h1>\n";
				if (parms.get("username") == null){
					html +="<form action='?' method='get'>\n" +
							"  <p>Your name: <input type='text' name='username'></p>\n" +
							"</form>\n";
				}else{
					html += "<p>Hello, " + parms.get("username") + "!</p>";
				}

				html += "</body></html>\n";

		}
		return new NanoHTTPD.Response(html);
	}


	public void setCommandRunner(CommandRunner commandRunner2) {
		this.commandRunner = commandRunner2;
	}
}
