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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.botUserAgent.config.GlobalConfig;

public class CliLogger implements Logger {

	private CliLoggerOutput out;
	private SimpleDateFormat logFormatter;
	private SimpleDateFormat networkFormatter;
	public boolean logDebug=true;
	public boolean logInfo=true;
	public boolean logError=true;
	public boolean logTraceNetwork=false;

	public CliLogger(CliLoggerOutput out) {
		this.out = out;
		logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	}

	public synchronized void debug(String message) {
		if(!GlobalConfig.config.getBoolean("logDebug")) return;
		System.out.println(genericLog(message.toString(), "DEBUG"));
		out.javaLog(genericLog(message.toString(), "DEBUG"));
	}

	public synchronized void info(String message) {
		if(!GlobalConfig.config.getBoolean("logInfo")) return;
		System.out.println(genericLog(message.toString(), "INFO"));
		out.javaLog(genericLog(message.toString(), "INFO"));
	}

	public synchronized void error(String message) {
		if(!GlobalConfig.config.getBoolean("logError")) return;
		System.err.println(genericLog(message.toString(), "ERROR"));
		out.javaLog(genericLog(message.toString(), "ERROR"));
	}

	public synchronized void error(String message, Exception exception) {
		if(!GlobalConfig.config.getBoolean("logError")) return;
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		stringWriter.write(genericLog(message, "ERROR"));
		stringWriter.flush();
		exception.printStackTrace(printWriter);
		printWriter.flush();
		System.err.println(stringWriter.toString());
		out.javaLog(stringWriter.toString());
	}

	public synchronized void traceNetwork(String message, String direction) {
		if(!GlobalConfig.config.getBoolean("logTraceNetwork")) return;
		StringBuffer buf = new StringBuffer();
		buf.append(networkFormatter.format(new Date()));
		buf.append(" ");
		buf.append(direction);
		buf.append(" [");
		buf.append(Thread.currentThread().getName());
		buf.append("]\n\n");
		buf.append(message);
		buf.append("\n");
		System.out.println(buf.toString());
		out.javaNetworkLog(buf.toString());
	}

	private final String genericLog(String message, String level) {
		StringBuffer buf = new StringBuffer();
		buf.append(logFormatter.format(new Date()));
		buf.append(" ");
		buf.append(level);
		buf.append(" [");
		buf.append(Thread.currentThread().getName());
		buf.append("] ");
		buf.append(message);
		buf.append("\n");
		return buf.toString();
	}
}