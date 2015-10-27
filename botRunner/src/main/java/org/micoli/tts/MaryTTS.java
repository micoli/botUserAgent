package org.micoli.tts;

import java.io.File;
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
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

public class MaryTTS {
	private MaryInterface marytts;

	public MaryTTS(){
		try {
			marytts = new LocalMaryInterface();
		} catch (MaryConfigurationException e) {
			e.printStackTrace();
		}
	}

	public MaryInterface getInterface(){
		return marytts;
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

	private void dumpToFile(AudioInputStream audioInputStream,String outFileName){
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
			System.out.println("Unable to close the Audio stream.");
		}
	}
}