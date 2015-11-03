package org.micoli.api;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassPathHacker {
	protected final static Logger logger = LoggerFactory.getLogger(ClassPathHacker.class);
	/**
	 * Parameters of the method to add an URL to the System classes.
	 */
	private static final Class<?>[] parameters = new Class[]{URL.class};

	/**
	 * Adds a file to the classpath.
	 * @param s a String pointing to the file
	 * @throws IOException
	 */
	public static void addFile(String s) throws IOException {
		addFile(new File(s));
	}

	/**
	 * Adds a file to the classpath
	 * @param f the file to be added
	 * @throws IOException
	 */
	public static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}

	/**
	 * Adds the content pointed by the URL to the classpath.
	 * @param u the URL pointing to the content to be added
	 * @throws IOException
	 */
	public static void addURL(URL url) throws IOException {
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ url });
		} catch (Throwable e) {
			logger.error(e.getClass().getSimpleName(), e);
			throw new IOException("Error, could not add URL to system classloader");
		}
	}

}// end class