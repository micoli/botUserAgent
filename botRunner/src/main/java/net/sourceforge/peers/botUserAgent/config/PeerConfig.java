package net.sourceforge.peers.botUserAgent.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.micoli.botUserAgent.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerConfig implements Config {
	protected final static Logger	logger		= LoggerFactory.getLogger(PeerConfig.class);

	private String 		id				= "-";
	private String 		behaviour		= "_default";
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

	public void setKey(String key,String value){
		switch (key){
			case "id":
				this.setId(value);
			break;
			case "user":
				this.setUserPart(value);
			break;
			case "domain":
				this.setDomain(value);
			break;
			case "password":
				this.setPassword(value);
			break;
			case "behaviour":
				this.setBehaviour(value);
			break;
		}
	}

	public void save() {}

	@SuppressWarnings("unchecked")
	public static List<PeerConfig> readPeersConf() throws FileNotFoundException, IOException, ParseException{
		ArrayList<PeerConfig> peers= new ArrayList<PeerConfig>();
		JSONParser parser = new JSONParser();
		String peersConfFilename = GlobalConfig.getConfig().getString(GlobalConfig.optPeersConfigFile);
		logger.info("Peers conf :: "+peersConfFilename);
		Object obj = parser.parse(new FileReader(peersConfFilename));

		JSONObject jsonConf = (JSONObject) obj;

		JSONArray peersList = (JSONArray) jsonConf.get("peers");

		Iterator<JSONObject> iterator = peersList.iterator();
		while (iterator.hasNext()) {
			PeerConfig config = new PeerConfig();
			JSONObject jsonPeer = iterator.next();
			setPeerConfigKey(config,"id"		,jsonPeer,jsonConf);
			setPeerConfigKey(config,"user"		,jsonPeer,jsonConf);
			setPeerConfigKey(config,"password"	,jsonPeer,jsonConf);
			setPeerConfigKey(config,"domain"	,jsonPeer,jsonConf);
			setPeerConfigKey(config,"behaviour"	,jsonPeer,jsonConf);
			peers.add(config);
		}
		return peers;
	}

	public InetAddress getLocalInetAddress() {
		if(this.localIpAddress== null){
			try {
				//inetAddress = InetAddress.getLocalHost();
				this.localIpAddress = InetAddress.getByName("0.0.0.0");
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			}
		}
		return this.localIpAddress;
	}
	public void setLocalInetAddress(InetAddress inetAddress) {
		this.localIpAddress = inetAddress;
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
	public void setId(String id) {
		this.id = id;
	}

	public String getBehaviour() {
		return this.behaviour;
	}
	public void setBehaviour(String behaviour) {
		this.behaviour = behaviour;
	}

	public String getUserPart() {
		return this.userPart;
	}
	public void setUserPart(String userPart) {
		this.userPart=userPart;
	}

	public String getDomain() {
		return this.domain;
	}
	public void setDomain(String domain) {
		this.domain=domain;
	}

	public String getPassword() {
		return this.password;
	}
	public void setPassword(String password) {
		this.password=password;
	}

	public MediaMode getMediaMode() {
		return this.mediaMode;
	}
	public void setMediaMode(MediaMode mediaMode) {
		this.mediaMode=mediaMode;
	}

	public SipURI getOutboundProxy() {
		return this.outboundProxy;
	}
	public void setOutboundProxy(SipURI outboundProxy) {
		this.outboundProxy=outboundProxy;
	}

	public int getSipPort() {
		return this.sipPort;
	}
	public void setSipPort(int sipPort) {
		this.sipPort = sipPort;
	}

	public boolean isMediaDebug() {
		return this.isMediaDebug;
	}
	public void setMediaDebug(boolean mediaDebug) {
		this.isMediaDebug = mediaDebug;
	}

	public String getMediaFile() {
		return this.mediaFile;
	}
	public void setMediaFile(String mediaFile) {
		this.mediaFile=mediaFile;
	}

	public int getRtpPort() {
		return this.rtpPort;
	}
	public void setRtpPort(int rtpPort) {
		this.rtpPort=rtpPort;
	}

	private static void setPeerConfigKey(PeerConfig config,String key,JSONObject jsonPeer,JSONObject jsonConf){
		if(jsonPeer.containsKey(key)){
			config.setKey(key,(String)jsonPeer.get(key));
		}
		if(jsonPeer.containsKey(key)){
			config.setKey(key,(String)jsonPeer.get(key));
		}else{
			if(jsonConf.containsKey("default")){
				JSONObject jsonDefault = (JSONObject) jsonConf.get("default");
				if(jsonDefault.containsKey(key)){
					config.setKey(key,(String)jsonDefault.get(key));
				}
			}
		}
	}
}