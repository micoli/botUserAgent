package net.sourceforge.peers.botUserAgent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.FileReader;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.MediaDestination;
import net.sourceforge.peers.sdp.NoCodecException;
import net.sourceforge.peers.sdp.SDPManager;
import net.sourceforge.peers.sdp.SessionDescription;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transport.SipRequest;

public class BotUserAgent extends UserAgent {
	public BotUserAgent(SipListener sipListener, Config config, Logger logger,AbstractSoundManager soundManager) throws SocketException {
		super(sipListener, config, logger, soundManager);
	}

	protected Object getFieldFromClass(Object context, Class cls,String fieldName){
		try {
			Field field = cls.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(context);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	BotUserAgent.acceptcall
		uas.acceptCall
		initialRequestManager.getInviteHandler().acceptCall(sipRequest,dialog)
		 ***************
			received2xx
				InvoiteHandler.successResponseReceived
					MediaManager.successResponseReceived
						captureAndPlayback
							MediaManager.startRtpSessionOnSuccessResponse
						Filereader
							MediaManager.startRtpSessionOnSuccessResponse
						Echo
	 */
	public void sendAudioFile(SipRequest oSIPRequest,String filename) throws IllegalArgumentException, IllegalAccessException {
		Logger		logger		= (Logger) getFieldFromClass(this,UserAgent.class,"logger");
		SDPManager	sdpManager	= (SDPManager) getFieldFromClass(this,UserAgent.class,"sdpManager");
		RtpSession	rtpSession	= (RtpSession) getFieldFromClass(getMediaManager(),MediaManager.class,"rtpSession");
		try {
			SessionDescription	sessionDescription	= sdpManager.parse(oSIPRequest.getBody());
			MediaDestination mediaDestination = sdpManager.getMediaDestination(sessionDescription);
			FileReader fileReader = new FileReader(filename, logger);

			System.out.println("logger "			+logger);
			System.out.println("sdpManager "		+sdpManager);
			System.out.println("mediaDestination "	+mediaDestination);
			System.out.println("fileReader "		+fileReader);
			System.out.println("rtpSession "		+rtpSession);

			try {
				CaptureRtpSender captureRtpSender = new CaptureRtpSender(rtpSession,fileReader, false, mediaDestination.getCodec(), logger,"");
				captureRtpSender.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (NoCodecException e1) {
			e1.printStackTrace();
		}
	}
}

/*	public void sendAudioPacket1(SipRequest oSIPRequest, String callId) {
		MediaDestination mediaDestination;
		String remoteAddress="";
		int remotePort=0;
		Codec codec = new Codec();
		InetAddress localAddress;

		DatagramSocket datagramSocket = AccessController.doPrivileged(
			new PrivilegedAction<DatagramSocket>() {
				@Override
				public DatagramSocket run() {
					DatagramSocket datagramSocket = null;
					int rtpPort = getConfig().getRtpPort();
					try {
						if (rtpPort == 0) {
							int localPort = -1;
							while (localPort % 2 != 0) {
								datagramSocket = new DatagramSocket();
								localPort = datagramSocket.getLocalPort();
								if (localPort % 2 != 0) {
									datagramSocket.close();
								}
							}
						} else {
							datagramSocket = new DatagramSocket(rtpPort);
						}
					} catch (SocketException e) {
						System.out.println("cannot create datagram socket ");
						e.printStackTrace();
					}

					return datagramSocket;
				}
			}
		);
		System.out.println("new rtp DatagramSocket " + datagramSocket.hashCode());
		try {
			datagramSocket.setSoTimeout(30000);
		} catch (SocketException e) {
			System.out.println("cannot set timeout on datagram socket ");
			e.printStackTrace();
		}
		getMediaManager().setDatagramSocket(datagramSocket);

		Logger logger;
		logger = getLogger();

		SDPManager _sdpManager = getSDPManager();
		//RtpSession rtpSession  = ((BotMediaManager) getMediaManager()).getRtpSession();
		//RtpSession rtpSession  = getMediaManagerRtpSession(getMediaManager());
		//System.out.println("diff "+(((BotMediaManager) getMediaManager()).getRtpSession())+" "+rtpSession);
		RtpSession _rtpSession = null;

		SessionDescription sessionDescription =_sdpManager.parse(oSIPRequest.getBody());
		try {
			mediaDestination = _sdpManager.getMediaDestination(sessionDescription);
			remoteAddress = mediaDestination.getDestination();
			remotePort = mediaDestination.getPort();
			codec = mediaDestination.getCodec();
			localAddress = getConfig().getLocalInetAddress();

			_rtpSession = new RtpSession(localAddress,datagramSocket,false, logger, "");
		} catch (SecurityException | NoCodecException e) {
			e.printStackTrace();
		}

		System.out.println("--- "+ _rtpSession +" "+_rtpSession.toString());

		//Echo echo;
		//InetAddress remoteAddress= getRtpSessionRemoteAddress(rtpSession);
		//int remotePort= getRtpSessionRemotePort(rtpSession);
		//System.out.println("---"+remoteAddress.getHostAddress() );
		//echo = new Echo(getMediaManager().getDatagramSocket(), remoteAddress.getHostAddress(), remotePort,logger);

		//IncomingRtpReader incomingRtpReader;
		//incomingRtpReader = new IncomingRtpReader(rtpSession,null, getCaptureRtpSenderCodec(),logger);
		//incomingRtpReader.start();

		//setEcho(echo);
		//Thread echoThread = new Thread(echo, Echo.class.getSimpleName());
		//echoThread.start();

		FileReader fileReader = new FileReader("/tmp/toto1234.wav.raw", logger);
		CaptureRtpSender captureRtpSender;

		try {
			captureRtpSender = new CaptureRtpSender(_rtpSession,fileReader, false, codec, logger,"");
			captureRtpSender.start();
		} catch (IOException e) {
			System.out.println("a input/output error");
			e.printStackTrace();
			return;
		}

	}
 */
/*Field captureRtpSenderField =UserAgent.class.getDeclaredField("captureRtpSender");
captureRtpSenderField.setAccessible(true);
RtpSender captureRtpSender = (RtpSender) captureRtpSenderField.get(this);

rtpSender.pushPackets(rtpPackets);

try {
	captureRtpSender = new CaptureRtpSender(rtpSession,soundSource, userAgent.isMediaDebug(), codec, logger,userAgent.getPeersHome());
} catch (IOException e) {
	logger.error("input/output error", e);
	return;
}

try {
	captureRtpSender.start();
} catch (IOException e) {
	logger.error("input/output error", e);
}
*/

/*
MediaDestination _mediaDestination;
SessionDescription sessionDescription =_sdpManager.parse(sipResponse.getBody());
try {
	_mediaDestination = _sdpManager.getMediaDestination(sessionDescription);
} catch (NoCodecException e) {
	e.printStackTrace();
}
String remoteAddress = _mediaDestination.getDestination();
int remotePort = _mediaDestination.getPort();
Codec codec = _mediaDestination.getCodec();
String localAddress = getConfig().getLocalInetAddress().getHostAddress();
 */
/*
protected Field getCaptureRtpSenderField() throws NoSuchFieldException, SecurityException{
	Field captureRtpSenderField =UserAgent.class.getDeclaredField("captureRtpSender");
	captureRtpSenderField.setAccessible(true);
	return captureRtpSenderField;
}

private RtpSender getCaptureRtpSender(){
	try {
		return  (RtpSender) getCaptureRtpSenderField().get(this);
	} catch (IllegalArgumentException | IllegalAccessException| NoSuchFieldException | SecurityException e) {
		e.printStackTrace();
	}
	return null;
}

protected Codec getCaptureRtpSenderCodec(){
	try {
		Field codecField;
		codecField = RtpSender.class.getDeclaredField("codec");
		codecField.setAccessible(true);
		return (Codec) codecField.get(getCaptureRtpSender());
	} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		e.printStackTrace();
	}
	return null;
}

protected DatagramSocket getMediaManagerDatagramSocket(){
	try {
		Field datagramSocketField;
		datagramSocketField = MediaManager.class.getDeclaredField("datagramSocket");
		datagramSocketField.setAccessible(true);
		return (DatagramSocket) datagramSocketField.get(getMediaManager());
	} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		e.printStackTrace();
	}
	return null;
}

protected Field getChallengeManagerField() throws NoSuchFieldException, SecurityException{
	Field challengeManagerField =UserAgent.class.getDeclaredField("challengeManager");
	challengeManagerField.setAccessible(true);
	return challengeManagerField;
}

private ChallengeManager getChallengeManager(){
	try {
		return (ChallengeManager) getChallengeManagerField().get(this);
	} catch (IllegalArgumentException | IllegalAccessException| NoSuchFieldException | SecurityException e) {
		e.printStackTrace();
	}
	return null;
}
protected Field getMediaManagerField() throws NoSuchFieldException, SecurityException{
	Field mediaManagerField =UserAgent.class.getDeclaredField("mediaManager");
	mediaManagerField.setAccessible(true);
	return mediaManagerField;
}
protected Field getLoggerField() throws NoSuchFieldException, SecurityException{
	Field loggerField =UserAgent.class.getDeclaredField("logger");
	loggerField.setAccessible(true);
	return loggerField;
}
*/
/*protected InetAddress getRtpSessionRemoteAddress(RtpSession rtpSession){
try {
	Field remoteAddressField =RtpSession.class.getDeclaredField("remoteAddress");
	remoteAddressField.setAccessible(true);
	return (InetAddress) remoteAddressField.get(rtpSession);
} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
	e.printStackTrace();
}
return null;
}

protected int getRtpSessionRemotePort(RtpSession rtpSession){
try {
	Field remotePortField =RtpSession.class.getDeclaredField("remotePort");
	remotePortField.setAccessible(true);
	return (int) remotePortField.get(rtpSession);
} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
	e.printStackTrace();
}
return 0;
}*/
/*private SDPManager getSDPManager(){
try {
	return (SDPManager) getFieldFromClass(UserAgent.class,"sdpManager").get(this);
} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
	e.printStackTrace();
}
return null;
}*/

/*private RtpSession getMediaManagerRtpSession(MediaManager mediaManager){
try {
	return (RtpSession) getFieldFromClass(MediaManager.class,"rtpSession").get(mediaManager);
} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
	e.printStackTrace();
}
return null;
}*/

/*protected Logger getLogger(){
try {
	return (Logger) getFieldFromClass(UserAgent.class,"logger").get(this);
} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
	e.printStackTrace();
}
return null;
}*/

