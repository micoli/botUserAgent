package org.micoli.botUserAgent;

import net.sourceforge.peers.media.SoundSource;
import net.sourceforge.peers.sip.transport.SipRequest;

public interface AudioPlugin {
	void streamAudioSource(SipRequest sipRequest, SoundSource source);
	void streamAudioFile(SipRequest sipRequest, String filename);
}
