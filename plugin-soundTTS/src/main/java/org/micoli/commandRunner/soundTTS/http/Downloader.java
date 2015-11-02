package org.micoli.commandRunner.soundTTS.http;

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author http://stackoverflow.com/questions/2263062/how-to-monitor-progress-jprogressbar-with-filechannels-transferfrom-method
 *
 */

public class Downloader implements RBCWrapperDelegate{
	protected final static Logger logger = LoggerFactory.getLogger(Downloader.class);

	@SuppressWarnings("resource")
	public Downloader start( String localPath, String remoteURL ) {
		FileOutputStream        fos;
		ReadableByteChannel     rbc;
		URL                     url;

		try {
			url = new URL( remoteURL );
			rbc = new RBCWrapper( Channels.newChannel( url.openStream() ), contentLength( url ),url.toString(), this );
			fos = new FileOutputStream( localPath );
			fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
		} catch ( Exception e ) {
			logger.error(e.getClass().getSimpleName(), e);
		}
		return this;
	}

	public void rbcProgressCallback(RBCWrapper rbc, double progress) {
		logger.info( String.format( "Download progress %d bytes received, %.02f%%", rbc.getReadSoFar(), progress));
	}

	private int contentLength( URL url ) {
		HttpURLConnection           connection;
		int                         contentLength = -1;

		try {
			HttpURLConnection.setFollowRedirects( false );

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod( "HEAD" );

			contentLength = connection.getContentLength();
		} catch ( Exception e ) {
			logger.error(e.getClass().getSimpleName(), e);
		}

		return contentLength;
	}
}