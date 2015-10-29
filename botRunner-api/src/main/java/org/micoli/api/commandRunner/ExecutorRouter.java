package org.micoli.api.commandRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.micoli.api.PluginsManager;
import org.micoli.botUserAgent.BotExtension;

public class ExecutorRouter {
	private class ContextMethod{
		public Object context;
		public Method method;
		public ContextMethod(Object object,Method method){
			this.context	= object;
			this.method	= method;
		}
	}
	//protected String lastCommand = "bot action=call from=6000 to=6001";
	protected String lastCommand = "bot from=6000 action=print text=aaaa";
	protected HashMap<String, ContextMethod> routes;
	protected CommandRunner commandRunner = null;

	public ExecutorRouter(CommandRunner commandRunner,boolean attachBotExtension){
		this.commandRunner= commandRunner;
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
		ContextMethod contextMethod = getCommand(route);
		return execute(contextMethod,map);
	}

	public String execute(ContextMethod contextMethod,CommandArgs map){
		try {
			/*System.out.println("Running "+
				commandRunner.getClass().getSimpleName()+
				"-"+
				contextMethod.method.getDeclaringClass().getSimpleName().toString()+
				"::"+
				route
				+" "+
				contextMethod.method.getName()+
				" "+
				map.toString()
			);*/
			map.setContext(commandRunner);
			return (String) contextMethod.method.invoke(contextMethod.context, map);
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