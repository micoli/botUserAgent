package org.micoli.api;

import java.util.List;

import org.micoli.api.commandRunner.CommandRunner;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.api.commandRunner.GenericCommands;
import org.micoli.botUserAgent.BotsManagerApi;
import org.micoli.botUserAgent.BotsManagerExtension;
import org.micoli.botUserAgent.BotsManagerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.ExtensionPoint;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class PluginsManager {
	protected final static Logger logger = LoggerFactory.getLogger(PluginsManager.class);
	static List<ExtensionPoint> genericCommands = null;;
	static List<BotsManagerExtension> botsManagerExtensions = null;;
	//static List<BotExtension> botExtensions = null;;
	static DefaultPluginManagerExt pluginManager;
	static BotsManagerApi botsManagerApi;

	public static void init(BotsManagerApi _botsManagerApi){
		botsManagerApi=_botsManagerApi;
		pluginManager = new DefaultPluginManagerExt();
		pluginManager.loadPlugins();
	}

	public static void start() {
		/*
		for (PluginWrapper pluginWrapper : pluginManager.getResolvedPlugins()) {
			PluginState pluginState = pluginWrapper.getPluginState();
			if ((PluginState.DISABLED != pluginState) && (PluginState.STARTED != pluginState)) {
				PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
				logger.info("Start plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
			}
		}*/

		pluginManager.startPlugins();

		for(PluginWrapper pluginWrapper: pluginManager.getPlugins()){
			Plugin plugin = pluginWrapper.getPlugin();
			if(BotsManagerPlugin.class.isAssignableFrom(plugin.getClass())){
				logger.info("Found BotsManagerPlugin ::"+plugin.getClass().getSimpleName().toString());
				((BotsManagerPlugin) plugin).setBotsManager(botsManagerApi);
			}
		}
	}

	public static void stop(){
		pluginManager.stopPlugins();
	}

	public static void startGenericCommands(ExecutorRouter executorRouter,CommandRunner commandRunner){
		genericCommands = getExtensionsbyClass(GenericCommands.class);
		for (ExtensionPoint command : genericCommands) {
			logger.info("Start interface "+command.getClass().getName());
			((GenericCommands) command).launch(executorRouter);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<ExtensionPoint> getExtensionsbyClass(@SuppressWarnings("rawtypes") Class extensionPointClass){
		String pluginList="";
		List<ExtensionPoint> extensions = pluginManager.getExtensions(extensionPointClass);
		for (Object extension : extensions) {
			pluginList=pluginList+(pluginList.equals("")?"":",")+ extension.getClass().getSimpleName();
		}
		logger.info(String.format("Found %d extensions for extension point '%s': %s", extensions.size(), extensionPointClass.getSimpleName(),pluginList));
		return extensions;
	}

	/*public static void startBotsManagerExtension(CommandRunner commandRunner){

		botsManagerExtensions = pluginManager.getExtensions(BotsManagerExtension.class);
		String pluginList="";
		for (BotsManagerExtension globalExtension : botsManagerExtensions) {
			pluginList=pluginList+(pluginList.equals("")?"":",")+ globalExtension.getClass().getSimpleName();
		}
		logger.info(String.format("Found %d botsManagerExtensions for extension point '%s': %s", botsManagerExtensions.size(), BotsManagerExtension.class.getSimpleName(),pluginList));

		for (BotsManagerExtension botsManagerExtension : botsManagerExtensions) {
			logger.info("Start BotsManagerExtension "+botsManagerExtension.getClass().getName());
			try {
				new ExecutorRouter(commandRunner,false);
				//executorRouter.attachRoutes(botsManagerExtension.getClass(), commandRunner, commandRunner);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/
}
