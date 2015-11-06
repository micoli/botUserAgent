package org.micoli.api.commandRunner;

import java.lang.reflect.Method;

public class ContextMethod{
	private Object context;
	private Object globalContext;
	private String[] args;

	public ContextMethod(Object context, Method method, Object globalContext, String[] args){
		this.context		= context;
		this.method			= method;
		this.globalContext	= globalContext;
		this.args			= args;
	}

	public Object getContext() {
		return context;
	}
	public Object getGlobalContext() {
		return globalContext;
	}
	public Method getMethod() {
		return method;
	}
	public String[] getArgs() {
		return args;
	}
	private Method method;
}