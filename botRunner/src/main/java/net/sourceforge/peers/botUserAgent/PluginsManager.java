package net.sourceforge.peers.botUserAgent;

import java.util.List;
import java.util.Set;

import org.micoli.api.commandRunner.CommandRunner;
import org.micoli.api.commandRunner.Executor;
import org.micoli.api.commandRunner.GenericCommands;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

public class PluginsManager {
	static List<GenericCommands> genericCommands;
	static PluginManager pluginManager;

	public static void init(){
		pluginManager = new DefaultPluginManager();
		pluginManager.loadPlugins();
	}

	public static void start(){
		pluginManager.startPlugins();
	}

	public static void stop(){
		pluginManager.stopPlugins();
	}

	public static void startGenericCommands(CommandRunner commandRunner){
		startGenericCommands(commandRunner,"*");
	}

	public static void startGenericCommands(CommandRunner commandRunner,String mask){
		Executor executor = new Executor(commandRunner);

		genericCommands = pluginManager.getExtensions(GenericCommands.class);

		// retrieves the extensions for Greeting extension point
		System.out.println(String.format("###Found %d extensions for extension point '%s'", genericCommands.size(), GenericCommands.class.getName()));
		for (GenericCommands command : genericCommands) {
			System.out.println("###>>> " + command.start());
		}

		System.out.println("###start interfaces");
		mask = mask.toLowerCase();
		for (GenericCommands command : genericCommands) {
			System.out.println("###start interface "+command.getClass().getName());
			if(mask.equalsIgnoreCase("*") || command.getClass().getName().toLowerCase().contains(mask)){
				System.out.println("###start interface ok "+command.getClass().getName());
				command.launch(executor);
			}
		}



		// print extensions from classpath (non plugin)
		System.out.println(String.format("###Extensions added by classpath:"));
		Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
		for (String extension : extensionClassNames) {
			System.out.println("###>S>S>S>S   " + extension);
		}

		// print extensions for each started plugin
		List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
		for (PluginWrapper plugin : startedPlugins) {
			String pluginId = plugin.getDescriptor().getPluginId();
			System.out.println(String.format("###Extensions added by plugin '%s':", pluginId));
			extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
			for (String extension : extensionClassNames) {
				System.out.println("###AAAAA   " + extension);
			}
		}
	}
}
