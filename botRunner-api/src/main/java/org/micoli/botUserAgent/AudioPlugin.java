package org.micoli.botUserAgent;

import net.sourceforge.peers.media.SoundSource;

public interface AudioPlugin extends BotPlugin{
	void playAudioSource(String callId, SoundSource source);
	void playAudioFile(String callId, String filename);
}
