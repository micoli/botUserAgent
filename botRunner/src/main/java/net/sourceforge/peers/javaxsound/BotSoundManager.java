/*
	Copyright 2010, 2011, 2012 Yohann Martineau
*/

package net.sourceforge.peers.javaxsound;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import net.sourceforge.peers.media.AbstractSoundManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotSoundManager extends AbstractSoundManager {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private AudioFormat audioFormat;
	private TargetDataLine microphoneDataLine;
	private SourceDataLine speakerDataLine;
	private Object speakerDataLineMutex;
	private DataLine.Info microphoneInfo;
	private DataLine.Info speakerInfo;
	private FileOutputStream microphoneOutput;
	private FileOutputStream speakerInput;
	private boolean mediaDebug;
	private boolean withStream;

	public BotSoundManager() {
		this.mediaDebug = false;
		this.withStream = false;
		audioFormat		= new AudioFormat(8000, 16, 1, true, false);
		microphoneInfo	= new DataLine.Info(TargetDataLine.class, audioFormat);
		speakerInfo		= new DataLine.Info(SourceDataLine.class, audioFormat);
		speakerDataLineMutex = new Object();
	}

	@Override
	public void init() {
		logger.debug("openAndStartLines");
		if(mediaDebug){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String			date = simpleDateFormat.format(new Date());
			StringBuffer	buf = new StringBuffer();
			buf.append(date).append("_");
			buf.append(audioFormat.getEncoding()).append("_");
			buf.append(audioFormat.getSampleRate()).append("_");
			buf.append(audioFormat.getSampleSizeInBits()).append("_");
			buf.append(audioFormat.getChannels()).append("_");
			buf.append(audioFormat.isBigEndian() ? "be" : "le");
			try {
				microphoneOutput	= new FileOutputStream(buf.toString() + "_microphone.output");
				speakerInput		= new FileOutputStream(buf.toString() + "_speaker.input");
			} catch (FileNotFoundException e) {
				logger.error("cannot create file", e);
				return;
			}
		}
		if(this.withStream){
			final boolean finalmediaDebug = mediaDebug;
			// AccessController.doPrivileged added for plugin compatibility
			AccessController.doPrivileged(
				new PrivilegedAction<Void>() {
					@Override
					public Void run() {
						try {
							if(finalmediaDebug){
								microphoneDataLine = (TargetDataLine) AudioSystem.getLine(microphoneInfo);
								microphoneDataLine.open(audioFormat);
							}
						} catch (LineUnavailableException e) {
							logger.error("microphone line unavailable", e);
							return null;
						} catch (SecurityException e) {
							logger.error("security exception", e);
							return null;
						} catch (Throwable t) {
							logger.error("throwable --" + t.getStackTrace()[0].getLineNumber() + t.getStackTrace()[0].getFileName() +" "+t.getMessage());
							return null;
						}
						microphoneDataLine.start();
						synchronized (speakerDataLineMutex) {
							try {
								speakerDataLine = (SourceDataLine) AudioSystem.getLine(speakerInfo);
								speakerDataLine.open(audioFormat);
							} catch (LineUnavailableException e) {
								logger.error("speaker line unavailable", e);
								return null;
							}
							speakerDataLine.start();
						}
						return null;
					}
			});
		}
	}

	@Override
	public synchronized void close() {
		logger.debug("closeLines");

		if(this.withStream){
			if (microphoneOutput != null) {
				try {
					microphoneOutput.close();
				} catch (IOException e) {
					logger.error("cannot close file", e);
				}
				microphoneOutput = null;
			}
			if (speakerInput != null) {
				try {
					speakerInput.close();
				} catch (IOException e) {
					logger.error("cannot close file", e);
				}
				speakerInput = null;
			}
			// AccessController.doPrivileged added for plugin compatibility
			AccessController.doPrivileged(new PrivilegedAction<Void>() {

				@Override
				public Void run() {
					if (microphoneDataLine != null) {
						microphoneDataLine.close();
						microphoneDataLine = null;
					}
					synchronized (speakerDataLineMutex) {
						if (speakerDataLine != null) {
							speakerDataLine.drain();
							speakerDataLine.stop();
							speakerDataLine.close();
							speakerDataLine = null;
						}
					}
					return null;
				}
			});
		}
	}

	@Override
	public synchronized byte[] readData() {
		if(!this.withStream){
			return null;
		}
		if (microphoneDataLine == null) {
		}
		int ready = microphoneDataLine.available();
		while (ready == 0) {
			try {
				Thread.sleep(2);
				ready = microphoneDataLine.available();
			} catch (InterruptedException e) {
				return null;
			}
		}
		if (ready <= 0) {
			return null;
		}
		byte[] buffer = new byte[ready];
		microphoneDataLine.read(buffer, 0, buffer.length);
		if (mediaDebug) {
			try {
				microphoneOutput.write(buffer, 0, buffer.length);
			} catch (IOException e) {
				logger.error("cannot write to file", e);
				return null;
			}
		}
		return buffer;
	}

	@Override
	public int writeData(byte[] buffer, int offset, int length) {
		if(!this.withStream){
			return 0;
		}
		int numberOfBytesWritten;
		synchronized (speakerDataLineMutex) {
			if (speakerDataLine == null) {
				return 0;
			}
			numberOfBytesWritten = speakerDataLine.write(buffer, offset, length);
		}
		if (mediaDebug) {
			try {
				speakerInput.write(buffer, offset, numberOfBytesWritten);
			} catch (IOException e) {
				logger.error("cannot write to file", e);
				return -1;
			}
		}
		return numberOfBytesWritten;
	}
}