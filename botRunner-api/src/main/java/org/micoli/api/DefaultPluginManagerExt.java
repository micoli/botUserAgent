package org.micoli.api;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

public class DefaultPluginManagerExt extends DefaultPluginManager{
	protected final static Logger logger = LoggerFactory.getLogger(DefaultPluginManagerExt.class);

	public Properties getPluginConfig(String pluginId){
		PluginWrapper pluginWrapper = this.getPlugin(pluginId);
		Properties properties = new Properties();

		File oPluginConfigFile	= new File(System.getProperty("pf4j.pluginsDir")+"/"+pluginWrapper.getPluginId() +".properties");
		File oDefaultConfigFile	= new File(System.getProperty("pf4j.pluginsDir")+"/"+pluginWrapper.getPluginPath() +"/plugin.properties");

		logger.info("PluginConfig conf :: " + oPluginConfigFile.getAbsolutePath());
		logger.info("oDefaultConfigFile conf :: " + oDefaultConfigFile.getAbsolutePath());

		if(oDefaultConfigFile.exists() && oDefaultConfigFile.canRead() && oDefaultConfigFile.isFile()){
			logger.info("Loading Default " + oDefaultConfigFile.getAbsolutePath());
			try {
				properties.load(new FileReader(oDefaultConfigFile));
			} catch (IOException e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
		}
		if (oPluginConfigFile.exists() && oPluginConfigFile.canRead() && oPluginConfigFile.isFile()){
			logger.info("Loading specific " + oDefaultConfigFile.getAbsolutePath());
			try {
				properties.load(new FileReader(oPluginConfigFile));
			} catch (IOException e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
		}
		String strDebug="";
		for(Object key : properties.keySet()){
			strDebug=strDebug+ (String) key +"="+(String)properties.getProperty((String) key)+", ";
		}
		logger.info("Config " +pluginId+"::"+ strDebug);
		return properties;
	}
}
