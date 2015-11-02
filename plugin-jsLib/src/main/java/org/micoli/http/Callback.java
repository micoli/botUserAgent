package org.micoli.http;

import org.json.simple.JSONObject;

public interface Callback {
	public String onSuccess(String response, Integer code,JSONObject jsonObject);
	public String onError(Integer code,JSONObject jsonObject);
}
