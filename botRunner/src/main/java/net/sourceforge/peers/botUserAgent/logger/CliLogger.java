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

import org.micoli.botUserAgent.GlobalConfig;
import org.slf4j.LoggerFactory;

public class CliLogger implements net.sourceforge.peers.Logger{
	private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	private SimpleDateFormat networkFormatter;
	//public boolean logDebug=true;
	//public boolean logInfo=true;
	//public boolean logError=true;
	//public boolean logTraceNetwork=false;

	public CliLogger() {
		networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	}

	public synchronized void debug(String message) {
		logger.debug(message);
		//if(!GlobalConfig.config.getBoolean("logDebug")) return;
		//out.javaLog(genericLog(message.toString(), "DEBUG"));
	}

	public synchronized void info(String message) {
		logger.info(message);
		//if(!GlobalConfig.config.getBoolean("logInfo")) return;
		//out.javaLog(genericLog(message.toString(), "INFO"));
	}

	public synchronized void error(String message) {
		logger.error(message);
		//if(!GlobalConfig.config.getBoolean("logError")) return;
		//out.javaLog(genericLog(message.toString(), "ERROR"));
	}

	public synchronized void error(String message, Exception exception) {
		logger.error(message,exception);
		/*
		if(!GlobalConfig.config.getBoolean("logError")) return;
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		stringWriter.write(genericLog(message, "ERROR"));
		stringWriter.flush();
		exception.printStackTrace(printWriter);
		printWriter.flush();
		System.err.println(stringWriter.toString());
		out.javaLog(stringWriter.toString());
		*/
	}

	public synchronized void traceNetwork(String message, String direction) {
		if(!GlobalConfig.getConfig().getBoolean(GlobalConfig.optLogTraceNetwork)) return;
		StringBuffer buf = new StringBuffer();
		buf.append(networkFormatter.format(new Date()));
		buf.append(" ");
		buf.append(direction);
		buf.append(" [");
		buf.append(Thread.currentThread().getName());
		buf.append("]\n\n");
		buf.append(message);
		logger.debug(buf.toString());
	}
}