package org.micoli.commandRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.config.MaryConfig;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import net.sourceforge.peers.sip.transport.SipRequest;

import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.CommandRoute;
import org.micoli.botUserAgent.AudioPlugin;
import org.micoli.botUserAgent.BotExtension;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class SoundTTSPlugin extends Plugin {
	private static MaryInterface marytts;

	public SoundTTSPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		System.out.println("SoundTTSPlugin.start()");
		try {
			String configFile;
			//configFile = System.getProperty("pf4j.pluginsDir")+""+this.wrapper.getPluginPath()+"/marytts/config/marybase.config";
			//System.out.println(configFile);
			configFile = "../botRunner-soundTTS/conf/marybase.config";
			System.out.println(configFile);

			MaryConfig.addConfig(new TTSConfig(new FileInputStream(new File(configFile))));
			marytts = new LocalMaryInterface();
		} catch (MaryConfigurationException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		System.out.println("SoundTTSPlugin.stop()");
	}

	@Extension
	public static class SoundTTS implements BotExtension{

		@CommandRoute(value="print", args={"text"})
		public String print(CommandArgs args){
			String result = "PRINT: "+args.get("text")+", context: "+args.getContext();
			System.out.println(result);
			return result;
		}

		@CommandRoute(value="sayWords", args={"words","callId"})
		public void sayWord(final CommandArgs args){
			try {
				new java.util.Timer().schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						try {
							String tmpFileName="/tmp/"+args.get("callId")+".wav";
							saveAudio(args.get("words"), tmpFileName);

							args.getContext(AudioPlugin.class).playAudioFile(args.get("callId"), tmpFileName);
						} catch (SynthesisException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				},200);
			} catch (SecurityException| IllegalArgumentException e) {
				e.printStackTrace();
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
			//AudioInputStream
			//AudioFormat format = new AudioFormat(8000, 16,1, false,false);
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
					System.out.println("That file does not already exist, and" + "there were problems creating a new file" + "of that name.  Are you sure the path"+ "to: " + outFileName + "exists?");
				}
			}
			try {
				if (AudioSystem.write(audioInputStream,type,file) == -1) {
					System.out.println("Problems writing to file.  Please " + "try again.");
				}
			}catch (FileNotFoundException e) {
				System.out.println("The file you specified did not already exist " + "so we tried to create a new one, but were unable" + "to do so.  Please try again.  If problems "+ "persit see your TA.");
			}catch (Exception e) {
				System.out.println("Problems writing to file: " + outFileName);
			}
			try {
				audioInputStream.close();
			}
			catch (Exception e) {
				System.out.println("Unable to close the BotExtension stream.");
			}
		}
	}
}