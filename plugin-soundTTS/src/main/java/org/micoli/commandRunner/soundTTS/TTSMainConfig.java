package org.micoli.commandRunner.soundTTS;
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
public class TTSMainConfig extends MaryConfig {

	public TTSMainConfig(InputStream stream) throws MaryConfigurationException, FileNotFoundException {
		super(stream);
	}

	@Override
	public boolean isMainConfig() {
		return true;
	}

}
