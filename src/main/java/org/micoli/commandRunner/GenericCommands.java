package org.micoli.commandRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.micoli.threads.ManagedThread;
import org.reflections.Reflections;

public class GenericCommands extends ManagedThread{
	protected CommandRunner	commandRunner;

	public GenericCommands(CommandRunner commandRunner) {
		this.commandRunner = commandRunner;
	}

	public static void startInterfaces(CommandRunner commandRunner) {
		startInterfaces(commandRunner,"*");
	}

	public static void startInterfaces(CommandRunner commandRunner,String mask) {
		mask = mask.toLowerCase();
		Reflections reflections = new Reflections("org.micoli.commandRunner");
		Set<Class<? extends GenericCommands>> classes = reflections.getSubTypesOf(GenericCommands.class);
		for(Class<?> clas:classes){
			if(mask.equalsIgnoreCase("*") || classes.getClass().getName().toLowerCase().contains(mask)){
				try {
					Constructor<?> constructor	= clas.getConstructor(CommandRunner.class);
					GenericCommands instance	= (GenericCommands) constructor.newInstance(commandRunner);
					instance.start();
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
