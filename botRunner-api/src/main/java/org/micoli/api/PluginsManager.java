package org.micoli.api;

import java.util.List;

import org.micoli.api.commandRunner.CommandRunner;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.botUserAgent.BotExtension;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.PluginManager;

public class PluginsManager {
	static List<GenericCommands> genericCommands = null;;
	static List<BotExtension> botExtensions = null;;
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
		ExecutorRouter executorRouter = new ExecutorRouter(commandRunner,false);

		genericCommands = pluginManager.getExtensions(GenericCommands.class);
		String pluginList="";
		for (GenericCommands command : genericCommands) {
			pluginList=pluginList+(pluginList.equals("")?"":",")+ command.getClass().getSimpleName();
		}
		System.out.println(String.format("Found %d extensions for extension point '%s': %s", genericCommands.size(), GenericCommands.class.getSimpleName(),pluginList));

		mask = mask.toLowerCase();
		for (GenericCommands command : genericCommands) {
			if(mask.equalsIgnoreCase("*") || command.getClass().getSimpleName().toLowerCase().contains(mask)){
				System.out.println("Start interface "+command.getClass().getName());
				command.launch(executorRouter);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List<BotExtension> getExtensionsbyClass(@SuppressWarnings("rawtypes") Class extensionPointClass){
		if(botExtensions == null){
			botExtensions = pluginManager.getExtensions(extensionPointClass);
			String pluginList="";
			for (BotExtension botExtension : botExtensions) {
				pluginList=pluginList+(pluginList.equals("")?"":",")+ botExtension.getClass().getSimpleName();
			}
			System.out.println(String.format("Found %d extensions for extension point '%s': %s", botExtensions.size(), extensionPointClass.getSimpleName(),pluginList));
		}
		return botExtensions;
	}

	public static void bindExtensionByClass(@SuppressWarnings("rawtypes") Class extensionPointClass ,CommandRunner commandRunner){
		botExtensions = getExtensionsbyClass(extensionPointClass);

		for (BotExtension botExtension : botExtensions) {
			((BotExtension) botExtension).bind(commandRunner);
		}
	}
}