package org.micoli.api.commandRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;


public class CommandArgs {
	protected Map<String, String> args;
	protected Object context;

	public CommandArgs(){
		args = new HashMap<String, String>();
	}

	public void setContext(Object context){
		this.context = context;
	}

	public Object getContext(){
		return this.context;
	}

	public <T> T getContext(Class<T> clz){
		return clz.cast(this.context);
	}

	public CommandArgs(JSONObject json){
		args = new HashMap<String, String>();
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keySet().iterator();
		while (keys.hasNext()) {
			String k = (String) keys.next();
			args.put(k,json.get(k).toString());
		}
		setDefault("from","000");
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
		setDefault("from","000");
	}

	public void setDefault(String key,String value){
		if(!has(key)){
			put(key,value);
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

	public String put(String key,String value){
		return args.put(key,value);
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
