package org.micoli.api.commandRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.micoli.api.PluginsManager;
import org.micoli.botUserAgent.BotExtension;

public class ExecutorRouter {
	private class ContextMethod{
		Object context;
		Method method;
		public ContextMethod(Object o,Method m){
			context=o;
			method=m;
		}
	}
	//protected String lastCommand = "bot action=call from=6000 to=6001";
	protected String lastCommand = "bot from=6000 action=print text=aaaa";
	protected HashMap<String, ContextMethod> routes;
	//protected CommandRunner commandRunner = null;

	public ExecutorRouter(CommandRunner commandRunner,boolean attachBotExtension){
		//this.commandRunner= commandRunner;
		try {
			System.out.println("new ExecutorRouter " + commandRunner.getClass().getSimpleName());
			this.routes = new HashMap<String, ContextMethod>();
			attachRoutes(commandRunner.getClass(),commandRunner);

			//FIXME : change botExtension to class <T>
			if (attachBotExtension){
				List<BotExtension> botExtensions = PluginsManager.getExtensionsbyClass(BotExtension.class);
				for (BotExtension botExtension : botExtensions) {
					attachRoutes(botExtension.getClass(),botExtension);
				}
			}
			//PluginsManager.bindExtension(BotExtension.class,commandRunner);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasCommand(String route){
		return routes.containsKey(route);
	}

	public ContextMethod getCommand(String route){
		return routes.get(route);
	}

	public String execute(String route,CommandArgs map){
		try {
			ContextMethod cm = getCommand(route);
			System.out.println("Running "+cm.context.getClass().getSimpleName().toString()+"::"+route+" "+cm.method.getName()+" "+cm.context.getClass().getSimpleName()+" "+ map.toString() );
			return (String) cm.method.invoke(cm.context, map);
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

	public void attachRoutes(Class<?> cls,Object context) throws Exception {
		System.out.println("    Attaching routes: "+cls.getSimpleName().toString());
		for (Method method : cls.getMethods()){
			if (method.isAnnotationPresent(CommandRoute.class)) {
				CommandRoute route = method.getAnnotation(CommandRoute.class);
				try {
					System.out.println("        Attach route: "+cls.getSimpleName().toString()+" :: "+route.value()+" :: "+context.getClass().getSimpleName());
					routes.put(route.value(), new ContextMethod(context,method));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}