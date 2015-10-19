package org.micoli.commandRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;


public class CommandArgs {
	protected Map<String, String> args;

	public CommandArgs(){
		args = new HashMap<String, String>();
	}

	public CommandArgs(JSONObject json){
		args = new HashMap<String, String>();
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keySet().iterator();
		while (keys.hasNext()) {
			String k = (String) keys.next();
			args.put(k,json.get(k).toString());
		}
	}

	public CommandArgs(String commandStr){
		args = new HashMap<String, String>();
		StringTokenizer st = new StringTokenizer(commandStr, " ");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			if (sep >= 0) {
				args.put(e.substring(0, sep).trim(),e.substring(sep + 1));
			} else {
				args.put(e.trim(), "");
			}
		}

	}
	public CommandArgs(Map<String, String> parms) {
		args = parms;
	}

	public boolean has(String key){
		return args.containsKey(key);
	}

	public String get(String key){
		return args.get(key);
	}

	public String getDefault(String key,String defolt){
		if(has(key)){
			return args.get(key);
		}else{
			return defolt;
		}
	}

	public int size() {
		return args.size();
	}
}
