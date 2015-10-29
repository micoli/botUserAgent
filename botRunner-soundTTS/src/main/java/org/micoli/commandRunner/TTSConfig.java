package org.micoli.commandRunner;
import java.io.FileNotFoundException;
import java.io.InputStream;

import marytts.config.MaryConfig;
import marytts.exceptions.MaryConfigurationException;

import com.google.auto.service.AutoService;

/**
 * @author marc
 *
 */
@AutoService(MaryConfig.class)
public class TTSConfig extends MaryConfig {

	public TTSConfig(InputStream stream) throws MaryConfigurationException, FileNotFoundException {
		super(stream);
	}

	@Override
	public boolean isMainConfig() {
		return true;
	}

}
