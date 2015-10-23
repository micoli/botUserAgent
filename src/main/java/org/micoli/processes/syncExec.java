package org.micoli.processes;

import java.io.IOException;

public class syncExec {

	public static void exec(String bin) {
		try {
			Process p = Runtime.getRuntime().exec(bin);
			streamDisplayer fluxSortie = new streamDisplayer(p.getInputStream());
			streamDisplayer fluxErreur = new streamDisplayer(p.getErrorStream());

			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}