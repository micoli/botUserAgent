package org.micoli.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceProviderTools {
	protected final static Logger logger = LoggerFactory.getLogger(ServiceProviderTools.class);

	public static Set<String> getProviders(String sPath, final String className){
		final Set<String> classesList = new HashSet<String>();
		File dir = new File(sPath);

		logger.debug("Scanning "+sPath+", looking for "+className+" providers");
		if(dir.exists() && dir.isDirectory()){
			dir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name){
					if(name.endsWith(".jar")){
						logger.debug("Globbing "+dir.getAbsolutePath()+"/"+name);
						try {
							JarFile jarFile = new JarFile(dir.getAbsolutePath()+"/"+name);
							ZipEntry jarEntry = jarFile.getJarEntry("META-INF/services/"+className);
							if(jarEntry!=null){
								BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry)));
								String line;
								while ((line = reader.readLine()) != null){
									logger.debug("Found " + line +" in "+className+"@"+name);
									classesList.add(line);
								}
								reader.close();
							}
							jarFile.close();
							return true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return false;
				}
			});
		}else{
			logger.debug(sPath+" is not a directory or does not exists");
		}
		return classesList;
	}
}
