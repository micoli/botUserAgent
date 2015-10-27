package net.sourceforge.peers.botUserAgent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.sip.SipUtils;

import org.json.simple.parser.ParseException;
import org.micoli.api.commandRunner.GenericCommands;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

import com.martiansoftware.jsap.JSAPException;


public class Main {

	public static void main(String[] args) throws JSAPException, FileNotFoundException, IOException, ParseException {
		loadPlugins();

		SipUtils.init();

		if(!GlobalConfig.parseArgs(args)){
			System.exit(1);
		}
		BotsManager botsManager= new BotsManager();
		botsManager.run();
	}

	public static void loadPlugins(){
		// create the plugin manager
		System.getProperty("pf4j.pluginsDir", "plugins");
		final PluginManager pluginManager = new DefaultPluginManager();

		// load the plugins
		pluginManager.loadPlugins();

		// enable a disabled plugin
		// pluginManager.enablePlugin("welcome-plugin");

		// start (active/resolved) the plugins
		pluginManager.startPlugins();

		// retrieves the extensions for Greeting extension point
		List<GenericCommands> genericCommands = pluginManager.getExtensions(GenericCommands.class);
		System.out.println(String.format("Found %d extensions for extension point '%s'", genericCommands.size(), GenericCommands.class.getName()));
		for (GenericCommands command : genericCommands) {
			System.out.println(">>> " + command.start());
		}

		// print extensions from classpath (non plugin)
		System.out.println(String.format("Extensions added by classpath:"));
		Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
		for (String extension : extensionClassNames) {
			System.out.println("   " + extension);
		}

		// print extensions for each started plugin
		List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
		for (PluginWrapper plugin : startedPlugins) {
			String pluginId = plugin.getDescriptor().getPluginId();
			System.out.println(String.format("Extensions added by plugin '%s':", pluginId));
			extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
			for (String extension : extensionClassNames) {
				System.out.println("   " + extension);
			}
		}

		// stop the plugins
		pluginManager.stopPlugins();
		/*
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				pluginManager.stopPlugins();
			}

		});
		*/

	}
}