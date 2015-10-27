package org.micoli.api.commandRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.reflections.Reflections;
import ro.fortsoft.pf4j.ExtensionPoint;

public class GenericCommands implements ExtensionPoint{
	protected Executor executor;

	public GenericCommands(Executor executor) {
		this.executor=executor;
	}

	public String start(){
		return "start >>>>>>>>>>>>>>>>>>>>>";
	}

	protected void launch(){}

	public static void startInterfaces(CommandRunner commandRunner) {
		startInterfaces(commandRunner,"*");
	}

	public static void startInterfaces(CommandRunner commandRunner,String mask) {
		Executor executor = new Executor(commandRunner);
		System.out.println("start interfaces");
		mask = mask.toLowerCase();
		Reflections reflections = new Reflections("org.micoli");
		Set<Class<? extends GenericCommands>> classes = reflections.getSubTypesOf(GenericCommands.class);
		for(Class<?> clas:classes){
			System.out.println("start interface "+classes.getClass().getName());
			if(mask.equalsIgnoreCase("*") || classes.getClass().getName().toLowerCase().contains(mask)){
				System.out.println("start interface ok "+classes.getClass().getName());
				try {
					Constructor<?> constructor	= clas.getConstructor(Executor.class);
					GenericCommands instance	= (GenericCommands) constructor.newInstance(executor);
					instance.launch();
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
