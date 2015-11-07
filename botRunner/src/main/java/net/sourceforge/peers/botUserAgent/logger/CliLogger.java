/*
	This file is part of Peers, a java SIP softphone.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

	Copyright 2013 Yohann Martineau
*/

package net.sourceforge.peers.botUserAgent.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.micoli.botUserAgent.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliLogger implements net.sourceforge.peers.Logger{
	private SimpleDateFormat networkFormatter;
	private static Map<String,Logger> loggers = new HashMap<String,Logger>();

	public CliLogger() {
		networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	}

	private synchronized Logger getLogger(){
		/*int i =0;
		for(StackTraceElement se: Thread.currentThread().getStackTrace()){
			i++;
			System.out.println(i+"-"+se.getFileName()+'+'+se.getClassName());
		}*/
		String className=Thread.currentThread().getStackTrace()[4].getClassName();
		if (!loggers.containsKey(className)){
			loggers.put(className,LoggerFactory.getLogger(className));
		}
		return loggers.get(className);
	}

	public synchronized void debug(String message) {
		getLogger().debug(message);
	}

	public synchronized void info(String message) {
		getLogger().info(message);
	}

	public synchronized void error(String message) {
		getLogger().error(message);
	}

	public synchronized void error(String message, Exception exception) {
		getLogger().error(message,exception);
	}

	public synchronized void traceNetwork(String message, String direction) {
		if(!GlobalConfig.getLogTraceNetwork()) return;
		StringBuffer buf = new StringBuffer();
		buf.append(networkFormatter.format(new Date()));
		buf.append(" ");
		buf.append(direction);
		buf.append(" [");
		buf.append(Thread.currentThread().getName());
		buf.append("]\n\n");
		buf.append(message);
		getLogger().debug(buf.toString());
	}
}