package org.micoli.api;

import java.util.List;

import org.micoli.api.commandRunner.CommandRunner;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.botUserAgent.BotExtension;
import org.micoli.botUserAgent.BotsManagerApi;
import org.micoli.botUserAgent.BotsManagerPlugin;
import org.micoli.botUserAgent.GlobalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

public class PluginsManager {
	protected final static Logger logger = LoggerFactory.getLogger(PluginsManager.class);
	static List<GenericCommands> genericCommands = null;;
	static List<GlobalExtension> globalExtensions = null;;
	static List<BotExtension> botExtensions = null;;
	static PluginManager pluginManager;
	static BotsManagerApi botsManagerApi;

	public static void init(BotsManagerApi _botsManagerApi){
		botsManagerApi=_botsManagerApi;
		pluginManager = new DefaultPluginManager();
		pluginManager.loadPlugins();
	}

	public static void start(){
		pluginManager.startPlugins();

		for(PluginWrapper pluginWrapper: pluginManager.getPlugins()){
			Plugin plugin = pluginWrapper.getPlugin();
			if(BotsManagerPlugin.class.isAssignableFrom(plugin.getClass())){
				logger.info("Found BotsManagerPlugin ::"+plugin.getClass().getSimpleName().toString());
				((BotsManagerPlugin) plugin).setBotsManager(botsManagerApi);
			}
		}
		//startGlobalExtension(botsManagerApi);
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
		logger.info(String.format("Found %d extensions for extension point '%s': %s", genericCommands.size(), GenericCommands.class.getSimpleName(),pluginList));

		mask = mask.toLowerCase();
		for (GenericCommands command : genericCommands) {
			if(mask.equalsIgnoreCase("*") || command.getClass().getSimpleName().toLowerCase().contains(mask)){
				logger.info("Start interface "+command.getClass().getName());
				command.launch(executorRouter);
			}
		}
	}

	/*public static void startGlobalExtension(CommandRunner commandRunner){
		globalExtensions = pluginManager.getExtensions(GlobalExtension.class);
		String pluginList="";
		for (GlobalExtension globalExtension : globalExtensions) {
			pluginList=pluginList+(pluginList.equals("")?"":",")+ globalExtension.getClass().getSimpleName();
		}
		logger.info(String.format("Found %d extensions for extension point '%s': %s", globalExtensions.size(), GlobalExtension.class.getSimpleName(),pluginList));

		for (GlobalExtension globalExtension : globalExtensions) {
			new ExecutorRouter(commandRunner,false);
		}
	}*/

	@SuppressWarnings("unchecked")
	public static List<BotExtension> getExtensionsbyClass(@SuppressWarnings("rawtypes") Class extensionPointClass){
		if(botExtensions == null){
			botExtensions = pluginManager.getExtensions(extensionPointClass);
			String pluginList="";
			for (BotExtension botExtension : botExtensions) {
				pluginList=pluginList+(pluginList.equals("")?"":",")+ botExtension.getClass().getSimpleName();
			}
			logger.info(String.format("Found %d extensions for extension point '%s': %s", botExtensions.size(), extensionPointClass.getSimpleName(),pluginList));
		}
		return botExtensions;
	}

	/*
	public static void bindExtensionByClass(@SuppressWarnings("rawtypes") Class extensionPointClass ,CommandRunner commandRunner){
		botExtensions = getExtensionsbyClass(extensionPointClass);
		for (BotExtension botExtension : botExtensions) {
			//((BotExtension) botExtension).bind(commandRunner);
		}
	}
	*/
}