package org.micoli.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

public class StreamDisplayer implements Runnable {
	private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	private final InputStream inputStream;

	StreamDisplayer(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	private BufferedReader getBufferedReader(InputStream is) {
		return new BufferedReader(new InputStreamReader(is));
	}

	@Override
	public void run() {
		BufferedReader br = getBufferedReader(inputStream);
		String ligne = "";
		try {
			while ((ligne = br.readLine()) != null) {
				System.out.println(ligne);
			}
		} catch (IOException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
	}
}