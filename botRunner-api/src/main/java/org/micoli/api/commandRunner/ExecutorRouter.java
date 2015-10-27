package org.micoli.api.commandRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ExecutorRouter {
	protected String lastCommand = "bot action=call from=6000 to=6001";
	protected HashMap<String, Method>	routes;
	protected CommandRunner commandRunner = null;

	public ExecutorRouter(CommandRunner commandRunner){
		this.commandRunner= commandRunner;
		try {
			parseCommandRunner(commandRunner.getClass());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasCommand(String route){
		return routes.containsKey(route);
	}

	public Method getCommand(String route){
		return routes.get(route);
	}

	public String execute(String route,CommandArgs map){
		try {
			return (String) getCommand(route).invoke(commandRunner, map);
		} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String executeCommand(String commandStr) {
		if(commandStr.equalsIgnoreCase("r") && !lastCommand.equalsIgnoreCase("")){
			commandStr = lastCommand;
		}

		CommandArgs args= new CommandArgs(((!commandStr.startsWith("method="))?"method=":"")+commandStr);

		lastCommand = commandStr;
		String subMethod = args.getDefault("method","[none]");
		if(hasCommand(subMethod)){
			return execute(subMethod, args);
		}else{
			return "No such command : "+subMethod;
		}
	}

	public void parseCommandRunner(Class<?> clazz) throws Exception {
		this.routes = new HashMap<String, Method>();
		for (Method method : clazz.getMethods()){
			if (method.isAnnotationPresent(CommandRoute.class)) {
				CommandRoute route = method.getAnnotation(CommandRoute.class);
				try {
					routes.put(route.value(), method);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}