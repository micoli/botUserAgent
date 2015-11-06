package org.micoli.commandRunner.soundTTS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.micoli.api.ServiceProviderTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundUtils {
	protected final static Logger logger = LoggerFactory.getLogger(SoundUtils.class);

	public static void saveToFile(AudioInputStream inStream,String outFileName){
		File outputFile=new File(outFileName);

		if (!outputFile.exists()) {
			try {
				outputFile.createNewFile();
			}catch (IOException e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
		}
		byte[] buf = new byte[1024];
		try{
			int bytes_read = 0;
			FileOutputStream out = new FileOutputStream(outputFile);

			do {
				bytes_read = inStream.read(buf, 0, buf.length);
				if (bytes_read > 0){
					out.write(buf, 0, bytes_read);
				}
			} while (bytes_read >= 0);

			try {
				out.close();
			}catch (Exception e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
			out.close();

		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	public static AudioInputStream getAudioInputStream(AudioFormat targ,AudioInputStream ais){
		ClassLoader loader = ClassLoader.getSystemClassLoader();

		logger.debug("Looking for FormatConversionProvider supproting : "+targ.toString());
		final Set<String> ProviderClasses=ServiceProviderTools.getProvidersFromClassLoader(loader, "javax.sound.sampled.spi.FormatConversionProvider");
		for(String classe: ProviderClasses) {
			FormatConversionProvider provider;
			try {
				logger.debug("New instance of provider : "+classe+"::");
				provider = (FormatConversionProvider) Class.forName(classe).getConstructor().newInstance();
				if (!provider.isConversionSupported(targ,ais.getFormat())){
					continue;
				}
				logger.info("Found FormatConversionProvider : "+classe);
				return provider.getAudioInputStream(targ,ais);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				logger.error(e.getClass().getSimpleName(), e);
			}
		}
		throw new IllegalArgumentException("Encoding not supported for stream");
	}

}