package org.micoli.commandRunner.soundTTS.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;

public class RBCWrapper implements ReadableByteChannel {
	private RBCWrapperDelegate              delegate;
	private long                            expectedSize;
	private ReadableByteChannel             rbc;
	private long                            readSoFar;
	private String                          url;
	private long                            lastSent;

	RBCWrapper( ReadableByteChannel rbc, long expectedSize,String url, RBCWrapperDelegate delegate ) {
		this.delegate = delegate;
		this.expectedSize = expectedSize;
		this.rbc = rbc;
		this.url = url;
		this.url = url;
		this.lastSent = (new Date().getTime())/1000;
	}

	public void close() throws IOException { rbc.close(); }
	public long getReadSoFar() { return readSoFar; }
	public boolean isOpen() { return rbc.isOpen(); }
	public String getUrl() { return url; }

	public int read( ByteBuffer bb ) throws IOException {
		int                     n;
		double                  progress;

		if ( ( n = rbc.read( bb ) ) > 0 ) {
			readSoFar += n;
			progress = expectedSize > 0 ? (double) readSoFar / (double) expectedSize * 100.0 : -1.0;
			long tick = (new Date().getTime())/1000;
			if( tick> this.lastSent){
				this.lastSent=tick;
				delegate.rbcProgressCallback( this, progress);
			}
		}

		return n;
	}
}