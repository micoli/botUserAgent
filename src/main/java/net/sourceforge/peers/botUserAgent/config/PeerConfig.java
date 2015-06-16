package net.sourceforge.peers.botUserAgent.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;

public class PeerConfig implements Config {

	private String 		id				= "-";
	private String 		behaviour		= "behaviour";
	private InetAddress publicIpAddress	= null;
	private InetAddress localIpAddress	= null;
	private String		userPart		= "";
	private String		domain			= "";
	private String		password		= "";
	private MediaMode	mediaMode		= MediaMode.captureAndPlayback;
	private SipURI		outboundProxy	= null;
	private int			sipPort			= 0;
	private boolean		isMediaDebug	= false;
	private String		mediaFile		= null;
	private int			rtpPort			= 0;

	public InetAddress getLocalInetAddress() {
		if(this.localIpAddress== null){
			try {
				//inetAddress = InetAddress.getByName("10.90.0.203");
				//inetAddress = InetAddress.getLocalHost();
				this.localIpAddress = InetAddress.getByName("0.0.0.0");
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			}
		}
		return this.localIpAddress;
	}

	public InetAddress getPublicInetAddress() {
		return publicIpAddress;
	}

	public void setPublicInetAddress(InetAddress inetAddress) {
		publicIpAddress = inetAddress;
	}

	public String getId() {
		return this.id;
	}

	public String getBehaviour() {
		return this.behaviour;
	}

	public String getUserPart() {
		return this.userPart;
	}

	public String getDomain() {
		return this.domain;
	}

	public String getPassword() {
		return this.password;
	}

	public MediaMode getMediaMode() {
		return this.mediaMode;
	}

	public SipURI getOutboundProxy() {
		return this.outboundProxy;
	}

	public int getSipPort() {
		return this.sipPort;
	}

	public boolean isMediaDebug() {
		return this.isMediaDebug;
	}

	public String getMediaFile() {
		return this.mediaFile;
	}

	public int getRtpPort() {
		return this.rtpPort;
	}

	public void setKey(String key,String value){
		switch (key){
			case "id":
				this.id=value;
			break;
			case "user":
				this.userPart=value;
			break;
			case "domain":
				this.domain=value;
			break;
			case "password":
				this.password=value;
			break;
		}
	}
	public void setLocalInetAddress(InetAddress inetAddress) {
		this.localIpAddress = inetAddress;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setBehaviour(String behaviour) {
		this.behaviour = behaviour;
	}
	public void setUserPart(String userPart) {
		this.userPart=userPart;
	}
	public void setDomain(String domain) {
		this.domain=domain;
	}
	public void setPassword(String password) {
		this.password=password;
	}
	public void setOutboundProxy(SipURI outboundProxy) {
		this.outboundProxy=outboundProxy;
	}
	public void setSipPort(int sipPort) {
		this.sipPort = sipPort;
	}
	public void setMediaMode(MediaMode mediaMode) {
		this.mediaMode=mediaMode;
	}
	public void setMediaDebug(boolean mediaDebug) {
		this.isMediaDebug = mediaDebug;
	}
	public void setMediaFile(String mediaFile) {
		this.mediaFile=mediaFile;
	}
	public void setRtpPort(int rtpPort) {
		this.rtpPort=rtpPort;
	}

	public void save() {
	}
}
