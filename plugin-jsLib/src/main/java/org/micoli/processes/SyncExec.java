package org.micoli.processes;

import java.io.IOException;

import org.slf4j.LoggerFactory;

public class SyncExec {
	private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	private static SyncExec inst = null ;
	public static SyncExec get(){
		if(inst==null){
			inst=new SyncExec();
		}
		return inst;
	}
	public void exec(String bin) {
		try {
			logger.debug("exec: "+bin);
			Process p = Runtime.getRuntime().exec(bin);
			StreamDisplayer fluxSortie = new StreamDisplayer(p.getInputStream());
			StreamDisplayer fluxErreur = new StreamDisplayer(p.getErrorStream());

			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
	}
}