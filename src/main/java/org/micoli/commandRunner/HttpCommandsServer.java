package org.micoli.commandRunner;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;



public class HttpCommandsServer extends NanoHTTPD {

	public HttpCommandsServer() throws IOException {
		super(8081);
	}

	@Override
	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();
		System.out.println(method + " '" + uri + "' ");

		String msg = "<html><body><h1>Hello server</h1>\n";
		Map<String, String> parms = session.getParms();
		if (parms.get("username") == null){
			msg +="<form action='?' method='get'>\n" +
				"  <p>Your name: <input type='text' name='username'></p>\n" +
				"</form>\n";
		}else{
			msg += "<p>Hello, " + parms.get("username") + "!</p>";
		}

		msg += "</body></html>\n";

		return new NanoHTTPD.Response(msg);
	}
}