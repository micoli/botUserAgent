package org.micoli.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class resourceManager {
	protected final static Logger logger = LoggerFactory.getLogger(resourceManager.class);
	public static void extractResources(final File jarFile,String jarPath,String destinationPath) {
		extractResources(jarFile,jarPath,destinationPath,false);
	}
	public static void extractResources(final File jarFile,String jarPath,String destinationPath,boolean force) {
		try {
			if(jarFile.isFile()) {
				@SuppressWarnings("resource")
				JarFile jar = new JarFile(jarFile);
				final Enumeration<JarEntry> entries = jar.entries();
				while(entries.hasMoreElements()) {
					final JarEntry jarEntry = entries.nextElement();
					String name = jarEntry.getName();
					if (name.startsWith(jarPath)) {
						String DestinationName = name.replaceFirst("^"+Pattern.quote(jarPath),  Matcher.quoteReplacement(destinationPath));
						logger.info(destinationPath + " " + name +" => " + DestinationName + " (" + jarEntry.getSize() + ")");
						File outFile = new File(DestinationName);
						if(outFile.exists()){
							logger.debug("Already exists "+DestinationName+", skip");
							continue;
						}
						if (jarEntry.isDirectory()) {
							logger.debug("Mkdir "+DestinationName);
							outFile.mkdir();
							continue;
						}
						logger.debug("Create and dump "+DestinationName);
						InputStream inputStream = jar.getInputStream(jarEntry);
						FileOutputStream fileOutputStream = new java.io.FileOutputStream(outFile);
						while (inputStream.available() > 0) {
							fileOutputStream.write(inputStream.read());
						}
						fileOutputStream.close();
						inputStream.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}