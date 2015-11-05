package org.micoli.api.commandRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import org.micoli.api.PluginsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.ExtensionPoint;

public class ExecutorRouter {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	final public static String root="_";

	private class ContextMethod{
		private Object context;
		private Object globalContext;
		public Object getContext() {
			return context;
		}
		public Object getGlobalContext() {
			return globalContext;
		}
		public Method getMethod() {
			return method;
		}
		private Method method;
		public ContextMethod(Object context, Method method, Object globalContext){
			this.context		= context;
			this.method			= method;
			this.globalContext	= globalContext;
		}
	}

	//protected String lastCommand = "bot action=call from=6000 to=6001";
	protected String lastCommand = "bot from=6000 action=print text=aaaa";
	protected HashMap<String,HashMap<String, ContextMethod>> routes = new HashMap<String, HashMap<String,ContextMethod>>();

	public ExecutorRouter(){
	}

	public void displayRoute(){
		logger.info("Declared Routes");
		for (Entry<String, HashMap<String, ContextMethod>> obj : routes.entrySet()) {
			logger.info(String.format(" - %s",obj.getKey()));
			for (Entry<String, ContextMethod> route : obj.getValue().entrySet()) {
				logger.info(String.format("    %s [%s@%s]",route.getKey(),route.getValue().method.getName(),route.getValue().context.getClass().getSimpleName()));
			}
		}
		logger.info("Declared Routes end.");
	}

	public boolean hasCommand(String Id,String route){
		return routes.containsKey(Id) && routes.get(Id).containsKey(route);
	}

	public ContextMethod getCommand(String Id,String route){
		return routes.get(Id).get(route);
	}

	public String execute(String Id, String route,CommandArgs map){
		if(!hasCommand(Id,route)){
			logger.error("Execute error, unknown route : "+route);
			return "";
		}
		return execute(getCommand(Id,route),map);
	}

	public String execute(ContextMethod contextMethod,CommandArgs map){
		try {
			/*logger.debug("Running "+
				commandRunner.getClass().getSimpleName()+
				"-"+
				contextMethod.method.getDeclaringClass().getSimpleName().toString()+
				"::"+
				contextMethod.method.getName()+
				" "+
				map.toString()
			);*/
			//map.setContext(contextMethod.getGlobalContext());

			logger.info("Execute contextMethod: "+contextMethod.getMethod().getName()+", context: "+contextMethod.getContext().getClass().toString()+", globalContext:"+contextMethod.getGlobalContext().getClass().toString());

			return (String) contextMethod.getMethod().invoke(contextMethod.getContext(), map);
		} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
		return "----";
	}

	public String executeCommand(String commandStr) {
		if(commandStr.equalsIgnoreCase("r") && !lastCommand.equalsIgnoreCase("")){
			commandStr = lastCommand;
		}

		CommandArgs args= new CommandArgs(((!commandStr.startsWith("method="))?"method=":"")+commandStr);

		lastCommand = commandStr;
		String subMethod = args.getDefault("method","[none]");
		if(hasCommand(args.get("from"),subMethod)){
			return execute(args.get("from"),subMethod, args);
		}else{
			return "No such command : "+subMethod;
		}
	}

	private void setRoute(String id,String route,ContextMethod contextMethod){
		if(!routes.containsKey(id)){
			routes.put(id,new HashMap<String, ContextMethod>());
		}
		if(routes.get(id).containsKey(route)){
			logger.error(String.format("Route %s already defined for %s",route,id));
		}else{
			routes.get(id).put(route, contextMethod);
		}
	}

	public void attachRouteForExtension(String Id,@SuppressWarnings("rawtypes") Class extensionClass,CommandRunner commandRunner){
		attachRoutes(Id,commandRunner.getClass(),commandRunner,commandRunner);
		for (ExtensionPoint extension : PluginsManager.getExtensionsbyClass(extensionClass)) {
			attachRoutes(Id,extension.getClass(),extension,commandRunner);
			//logger.info(String.format("attachRouteForExtension %s : %s, %s",extensionClass.getName(),extension.getClass().getName(),commandRunner.getClass().getName()));
			//logger.info(String.format("attachRouteForExtension %s : %s, %s",extensionClass.getName(),commandRunner.getClass().getName(),commandRunner.getClass().getName()));
			//logger.info(String.format("attachRouteForExtension end"));
		}
	}

	public void attachRoutes(String id,Class<?> cls,Object context,Object globalContext) {
		String sRoutes="";
		//logger.info(">--------------->");
		//logger.info("    Attaching routes: "+cls.getSimpleName().toString());
		sRoutes += "Attaching routes for "+cls.getSimpleName().toString()+"::";
		for (Method method : cls.getMethods()){
			//logger.info("    Attaching routes: "+cls.getSimpleName().toString()+" method "+ method.getName());
			if (method.isAnnotationPresent(CommandRoute.class)) {
				//logger.info("    Attaching routes: "+cls.getSimpleName().toString()+" method "+ method.getName()+" isPresent");
				CommandRoute route = method.getAnnotation(CommandRoute.class);
				try {
					//logger.debug(" Attach route: "+cls.getSimpleName().toString()+" :: "+route.value()+" :: "+(route.global()?globalContext:context).getClass().getSimpleName());
					sRoutes += route.value()+"@"+context.getClass().getSimpleName()+"/"+globalContext.getClass().getSimpleName()+", ";
					setRoute(id,route.value(), new ContextMethod(context,method,globalContext));
				} catch (Exception e) {
					logger.error(e.getClass().getSimpleName(), e);
				}
			}
		}
		logger.info(sRoutes);
		//logger.info("<---------------<");
	}
}