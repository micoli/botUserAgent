package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.config.MaryConfig;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

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
		String sPath = System.getProperty("pf4j.pluginsDir")+""+this.wrapper.getPluginPath()+"/lib";
		File dir = new File(sPath);
		final Set<String> MaryConfigClasses = new HashSet<String>();

		dir.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
				if(name.endsWith(".jar")){
					logger.debug("Globbing "+dir.getAbsolutePath()+"/"+name);
					try {
						JarFile jarFile = new JarFile(dir.getAbsolutePath()+"/"+name);
						ZipEntry jarEntry = jarFile.getJarEntry("META-INF/services/marytts.config.MaryConfig");
						if(jarEntry!=null){
							BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry)));
							String line;
							while ((line = reader.readLine()) != null){
								logger.debug("Found " + line +" in marytts.config.MaryConfig@"+name);
								MaryConfigClasses.add(line);
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

		try {
			//String configFile;
			//configFile = "../botRunner-soundTTS/conf/marybase.config";
			logger.debug("Init MaryConfig classes");
			for(String configClass : MaryConfigClasses){
				try {
					logger.debug("Load MaryConfig classe: "+configClass);
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

		private void convert(AudioInputStream inStream,String outFileName){
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