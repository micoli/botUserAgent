package org.micoli.commandRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.config.MaryConfig;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import org.micoli.api.ServiceProviderTools;
import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.CommandRoute;
import org.micoli.botUserAgent.AudioPlugin;
import org.micoli.botUserAgent.BotExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class SoundTTSPlugin extends Plugin {
	protected final static Logger logger = LoggerFactory.getLogger(SoundTTSPlugin.class);
	private static MaryInterface marytts;

	public SoundTTSPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		logger.debug("SoundTTSPlugin.start()");
		final Set<String> MaryConfigClasses = ServiceProviderTools.getProviders(System.getProperty("pf4j.pluginsDir")+""+this.wrapper.getPluginPath()+"/lib","marytts.config.MaryConfig");

		try {
			logger.debug("Init MaryConfig classes");
			for(String configClass : MaryConfigClasses){
				try {
					logger.debug("Load MaryConfig class: "+configClass);
					MaryConfig.addConfig((MaryConfig) Class.forName(configClass).getConstructor().newInstance());
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException
						| ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			marytts = new LocalMaryInterface();
			for(Locale locale : marytts.getAvailableLocales()){
				logger.debug("Locale "+locale.getCountry()+"-"+locale.getDisplayLanguage()+"::"+locale.getDisplayName());
			}
			for(String voice : marytts.getAvailableVoices()){
				logger.debug("Voice "+voice);
			}
			logger.debug("End initialisation");
		} catch (MaryConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		logger.debug("SoundTTSPlugin.stop()");
	}

	@Extension
	public static class SoundTTS implements BotExtension{

		@CommandRoute(value="print", args={"text"})
		public String print(CommandArgs args){
			String result = "PRINT: "+args.get("text")+", context: "+args.getContext();
			logger.info(result);
			return result;
		}

		@CommandRoute(value="sayWords", args={"words","callId"})
		public void sayWord(final CommandArgs args){
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(args.get("callId").getBytes(),0,args.get("callId").length());
				final String tmpFileName="/tmp/"+md5.digest()+".wav";

				new java.util.Timer().schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						try {
							// marytts.setLocale(Locale.FRENCH);
							saveAudio(args.get("words"), tmpFileName);
							args.getContext(AudioPlugin.class).playAudioFile(args.get("callId"), tmpFileName);
							logger.info("... played "+tmpFileName);
						} catch (SynthesisException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				},200);
			} catch (SecurityException| IllegalArgumentException | NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			}
		}

		public void setVoice(String name){
			Set<String> voices = marytts.getAvailableVoices();
			Iterator<String> iterator = voices.iterator();
			while (iterator.hasNext()){
				String voiceName = iterator.next();
				if(voiceName.equals(name)){
					marytts.setVoice(voiceName);
				}
			}
		}
		@CommandRoute(value="saveAudio", args={"words","filename"})
		public void saveAudio(String words,String outFileName) throws SynthesisException, InterruptedException{
			//dumpToFile(marytts.generateAudio(words),outFileName);
			convert(marytts.generateAudio(words),outFileName);
		}

		/*private AudioInputStream getAudioInputStream(AudioFormat.Encoding targ,AudioInputStream ais){
s			Iterator i=ServiceFactory.lookupProviders(FormatConversionProvider.class);
			while (i.hasNext()) {
				FormatConversionProvider prov=(FormatConversionProvider)i.next();
				if (!prov.isConversionSupported(targ,ais.getFormat()))     continue;
				return prov.getAudioInputStream(targ,ais);
			}
			throw new IllegalArgumentException("encoding not supported for stream");
		}*/

		private void convert(AudioInputStream inStream,String outFileName){
			//   - Channels: 1 (Mono)
			//   - Sampling frequency of default: 8000 Hz
			//   - Default Sample Format: 16 bits
			//AudioFormat.Encoding targ = AudioFormat.Encoding.ALAW
			//AudioSystem.write(inStream,AudioFileFormat.Type.WAVE, outFile);

			File outputFile = new File(outFileName);
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

				out.close();

			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}

		public void convertOld(AudioInputStream inStream,String outFileName){
			File outputFile = new File(outFileName);
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

				out.close();

			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}

		public void dumpToFile(AudioInputStream audioInputStream,String outFileName){
			AudioFileFormat.Type type= AudioFileFormat.Type.WAVE;
			File file=new File(outFileName);
			if (!file.exists()) {
				try {
					file.createNewFile();
				}catch (IOException e) {
					logger.error("That file does not already exist, and" + "there were problems creating a new file" + "of that name.  Are you sure the path"+ "to: " + outFileName + "exists?",e);
				}
			}
			try {
				if (AudioSystem.write(audioInputStream,type,file) == -1) {
					logger.error("Problems writing to file.  Please " + "try again.");
				}
			}catch (FileNotFoundException e) {
				logger.error("The file you specified did not already exist " + "so we tried to create a new one, but were unable" + "to do so.  Please try again.  If problems "+ "persit see your TA.",e);
			}catch (Exception e) {
				logger.error("Problems writing to file: " + outFileName,e);
			}
			try {
				audioInputStream.close();
			}
			catch (Exception e) {
				logger.error("Unable to close the BotExtension stream.",e);
			}
		}
	}
}