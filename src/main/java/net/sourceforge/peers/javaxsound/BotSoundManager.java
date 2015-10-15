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

	Copyright 2010, 2011, 2012 Yohann Martineau
*/

package net.sourceforge.peers.javaxsound;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.AbstractSoundManager;

public class BotSoundManager extends AbstractSoundManager {

	private Logger logger;

	public BotSoundManager(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void init() {
		logger.debug("openAndStartLines");
		/*SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String date = simpleDateFormat.format(new Date());
		StringBuffer buf = new StringBuffer();
		buf.append(date).append("_");
		buf.append(audioFormat.getEncoding()).append("_");
		buf.append(audioFormat.getSampleRate()).append("_");
		buf.append(audioFormat.getSampleSizeInBits()).append("_");
		buf.append(audioFormat.getChannels()).append("_");
		buf.append(audioFormat.isBigEndian() ? "be" : "le");*/
	}

	@Override
	public synchronized void close() {
		logger.debug("closeLines");
	}

	@Override
	public synchronized byte[] readData() {
		return null;
	}

	@Override
	public int writeData(byte[] buffer, int offset, int length) {
		return 0;
	}

}
