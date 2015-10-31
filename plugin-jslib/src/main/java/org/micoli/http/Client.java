package org.micoli.http;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

public class Client {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public Client() {
	}

	public void request(String url){
		request(url,"GET");
	}

	public void request(String url,String method){
		request(url,method,null);
	}

	public void request(String url,String method,Callback callback){
		request(url,method,callback,"",new String[] { });
	}

	public void request(String url,String method,Callback callback,String body){
		request(url,method,callback,body,new String[] { });
	}

	private JSONObject getJSONResponse(Response response){
		return getJSONResponse(response,null);
	}

	@SuppressWarnings({ "unchecked" })
	private JSONObject getJSONResponse(Response response,Throwable thr){
		JSONObject obj=new JSONObject();
		try {
			obj.put("statusCode"	, response.getContentType());
			obj.put("cookies"		, response.getCookies());
			obj.put("headers"		, response.getHeaders());
			obj.put("statusCode"	, response.getStatusCode());
			obj.put("statusText"	, response.getStatusText());
			if(thr!=null){
				obj.put("errorFilename"		, thr.getStackTrace()[0].getFileName());
				obj.put("errorLineNumber"	, thr.getStackTrace()[0].getLineNumber());
				obj.put("errorMethod"		, thr.getStackTrace()[0].getMethodName());
				obj.put("errorMessage"		, thr.getMessage().replace("\n", " ").replace("\r", " "));
				thr.printStackTrace();
			}
			obj.put("uri"			, response.getUri().toASCIIString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public void request(JSONObject json){
		String url			= json.containsKey("url"		)?(String	) json.get("url"		):"";
		String method		= json.containsKey("method"		)?(String	) json.get("method"		):"GET";
		Callback callback	= json.containsKey("callback"	)?(Callback	) json.get("callback"	):null;
		String body			= json.containsKey("body"		)?(String	) json.get("body"		):null;
		String[] headers	= json.containsKey("headers"	)?(String[]	) json.get("headers"	):new String[] {};
		request(url,method,callback,body,headers);
	}

	@SuppressWarnings("serial")
	public void request(String url,String method,final Callback callback,String body,String[] headers){
		RequestBuilder builder = new RequestBuilder(method);
		builder.setUrl(url);

		for (String header: headers) {
			String[] headersKV = header.split(":",2);
			builder.addHeader(headersKV[0],headersKV[1]);
		}
		if(method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")){
			builder.setBody(body);
		}
		Request request = builder.build();

		AsyncHandler<Response> asyncHandler = new AsyncHandler<Response>() {
			private final Response.ResponseBuilder responseBuilder = new Response.ResponseBuilder();
			public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
				responseBuilder.accumulate(content);
				return STATE.CONTINUE;
			}

			public STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
				responseBuilder.accumulate(status);
				return STATE.CONTINUE;
			}

			public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
				responseBuilder.accumulate(headers);
				return STATE.CONTINUE;
			}

			public Response onCompleted() throws Exception {
				Response response = responseBuilder.build();
				try{
					if(callback != null){
						callback.onSuccess(response.getResponseBody(),response.getStatusCode(),getJSONResponse(response));
					}
					return response;
				}catch(IOException e){
					if(callback != null){
						callback.onError(response.getStatusCode(),getJSONResponse(response,e));
					}
					return response;
				}
			}

			public void onThrowable(Throwable arg0) {
				Response response = responseBuilder.build();
				if(callback != null){
					callback.onError(400+response.getStatusCode(),getJSONResponse(response,arg0));
				}
			}
		};

		AsyncHttpClient client = new AsyncHttpClient();
		try {
			this.logger.info("Client client "+ url);
			client.executeRequest(request, asyncHandler);
		} catch (final IOException e) {
			if(callback != null){
				callback.onError(-1,new JSONObject(){
					@SuppressWarnings("unused")
					String message	= e.getMessage();
				});
			}
		}
	}
}
