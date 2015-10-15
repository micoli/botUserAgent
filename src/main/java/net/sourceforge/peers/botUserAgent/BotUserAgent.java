package net.sourceforge.peers.botUserAgent;
//hashmap by callid

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.logger.CliLogger;
import net.sourceforge.peers.botUserAgent.logger.CliLoggerOutput;
import net.sourceforge.peers.botUserAgent.misc.MiscUtils;
import net.sourceforge.peers.javaxsound.BotSoundManager;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class BotUserAgent implements SipListener,CliLoggerOutput {
	private BotsManager					botsManager;
	private ScheduledExecutorService	scheduledExecutor;
	private UserAgent					userAgent;
	private Logger						logger;
	private PeerConfig					config;

	public BotUserAgent(BotsManager botsManager,PeerConfig config) {
		this.botsManager	= botsManager;
		this.logger			= new CliLogger(this);
		this.config			= config;

		BotSoundManager javaxSoundManager = new BotSoundManager(logger);

		try {
			userAgent = new UserAgent(this, this.config, logger, javaxSoundManager);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		JSExec("initBot",new Object[] {this.config.getId(), this.config,this});

		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				JSCallback("tick",new Object[] {});
			}
		}, 20, 20, TimeUnit.SECONDS);

	}

	public void instantiatePeers() {
		try {
			String ipAddress = GlobalConfig.config.getInetAddress("bindAddr").toString().replaceAll("/", "");
			String peersHome = Utils.DEFAULT_PEERS_HOME;
			final BotSoundManager soundManager = new BotSoundManager(logger);
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e1) {
				System.err.println(e1 + " " + e1.getMessage());
				logger.error("Unknown ipAddress " + ipAddress, e1);
				return;
			}
			this.config.setLocalInetAddress(inetAddress);
			this.config.setMediaMode(MediaMode.captureAndPlayback);

			botsManager.getExecutorService().submit(new Runnable() {
				public void run() {
					try {
						userAgent = new UserAgent(BotUserAgent.this, config,logger, soundManager);
					} catch (SocketException e) {
						logger.error(e.getMessage());
					}
				}
			});
		} catch (SecurityException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void register() {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				try {
					userAgent.register();
				} catch (SipUriSyntaxException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void close() {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.close();
			}
		});
	}

	// commands methods
	public void call(final String callee) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				try {
					botsManager.storeSipRequest(userAgent.invite(callee, null));
				} catch (SipUriSyntaxException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void acceptCall(final SipRequest sipRequest) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				String callId = Utils.getMessageCallId(sipRequest);
				DialogManager dialogManager = userAgent.getDialogManager();
				Dialog dialog = dialogManager.getDialog(callId);
				userAgent.acceptCall(sipRequest, dialog);
			}
		});
	}

	public void acceptCallByCallId(final String callId) {
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			botsManager.getExecutorService().submit(new Runnable() {
				public void run() {
					DialogManager dialogManager = userAgent.getDialogManager();
					Dialog dialog = dialogManager.getDialog(callId);
					userAgent.acceptCall(oSIPRequest, dialog);
				}
			});
		}
	}

	public void invite(final String uri) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
				try {
					SipRequest sipRequest = userAgent.invite(uri, callId);
					botsManager.storeSipRequest(sipRequest);
					setInviteSipRequest(sipRequest);
				} catch (SipUriSyntaxException e) {
					logger.error(e.getMessage());
				}
			}
		});

	}

	public void unregister() {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				try {
					userAgent.unregister();
				} catch (SipUriSyntaxException e) {
					logger.error(e.getMessage());
				}
			}
		});
	}

	public void terminate(SipRequest sipRequest) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.terminate(sipRequest);
			}
		});
	}

	public void terminateByCallId(String callId) {
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			terminate(oSIPRequest);
		}
	}

	public void pickup(final SipRequest sipRequest) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				String callId = Utils.getMessageCallId(sipRequest);
				DialogManager dialogManager = userAgent.getDialogManager();
				Dialog dialog = dialogManager.getDialog(callId);
				userAgent.acceptCall(sipRequest, dialog);
			}
		});
	}

	public void pickupByCallId(String callId) {
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			pickup(oSIPRequest);
		}
	}

	public void busy(final SipRequest sipRequest) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.rejectCall(sipRequest);
			}
		});
	}

	public void busyByCallId(String callId) {
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			busy(oSIPRequest);
		}
	}

	public void dtmf(final char digit) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				MediaManager mediaManager = userAgent.getMediaManager();
				mediaManager.sendDtmf(digit);
			}
		});
	}

	private void JSExec(String method,Object[] arguments){
		try {
			botsManager.getInvocableEngine().invokeFunction(method, arguments);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			System.err.println("JS Error : "+ method + ", args " + arguments+" "+e.getFileName()+"("+e.getLineNumber()+','+e.getColumnNumber() +")");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void JSCallback(String method,Object[] arguments){
		try {
			for(int i = 0; i < arguments.length; i++) {
				if(	arguments[i].getClass().getName().endsWith(".SipResponse") ||
					arguments[i].getClass().getName().endsWith(".SipRequest")
				){
					JSONObject obj=new JSONObject();
					SipHeaders sipHeaders = null;
					if(arguments[i].getClass().getName().endsWith(".SipResponse")){
						SipResponse sipObject = (SipResponse) arguments[i];
						obj.put("statusCode"	, sipObject.getStatusCode());
						obj.put("reasonPhrase"	, sipObject.getReasonPhrase());
						obj.put("sipVersion"	, sipObject.getSipVersion());
						//obj.put("sipHeaders"	, sipObject.getSipHeaders());
						if(sipObject.getBody()!=null){
							obj.put("body"			, new String(sipObject.getBody()));
						}
						sipHeaders =  sipObject.getSipHeaders();
					}
					if(arguments[i].getClass().getName().endsWith(".SipRequest")){
						SipRequest sipObject = (SipRequest) arguments[i];
						obj.put("method"		, sipObject.getMethod());
						obj.put("requestUri"	, sipObject.getRequestUri().toString());
						obj.put("from"			, MiscUtils.getFrom(sipObject));
						obj.put("sipVersion"	, sipObject.getSipVersion());
						if(sipObject.getBody()!=null){
							obj.put("body"			, new String(sipObject.getBody()));
						}
						//obj.put("sipHeaders"	, sipObject.getSipHeaders());
						sipHeaders =  sipObject.getSipHeaders();
					}
					HashMap<String,String> headers = new HashMap<String,String>();
					for(SipHeaderFieldName hdr :MiscUtils.sipHeaderList){
						if(sipHeaders.contains(hdr)){
							headers.put(hdr.getName(), JSONValue.toJSONString(sipHeaders.get(hdr)));
						}
					}
					obj.put("sipHeaders"	, headers);
					arguments[i]=obj.toJSONString();
				}
			}
			botsManager.getInvocableEngine().invokeFunction("botCb",config.getId(),method, arguments);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	// SipListener methods
	public void registering(SipRequest sipRequest) {
		JSCallback("registering",new Object[] { sipRequest,config});
		this.botsManager.removeSipRequest(Utils.getMessageCallId(sipRequest));
	}

	public void registerSuccessful(SipResponse sipResponse) {
		JSCallback("registerSuccessful",new Object[] { sipResponse,config});
	}

	public void registerFailed(SipResponse sipResponse) {
		JSCallback("registerFailed",new Object[] { sipResponse,config});
	}

	public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
		botsManager.storeSipRequest(sipRequest);
		JSCallback("incomingCall",new Object[] { sipRequest,provResponse,Utils.getMessageCallId(sipRequest)});
	}

	public void remoteHangup(SipRequest sipRequest) {
		JSCallback("remoteHangup",new Object[] { sipRequest,Utils.getMessageCallId(sipRequest)});
		this.botsManager.removeSipRequest(Utils.getMessageCallId(sipRequest));
	}

	public void ringing(SipResponse sipResponse) {
		JSCallback("ringing",new Object[] {  sipResponse});
	}

	public void calleePickup(SipResponse sipResponse) {
		JSCallback("calleePickup",new Object[] { sipResponse});
	}

	public void error(SipResponse sipResponse) {
		JSCallback("error",new Object[] { sipResponse});
	}

	//CliLoggerOutput
	public void javaLog(final String message) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSExec("javaLog", new Object[]{message});
			}
		});
	}

	//CliLoggerOutput
	public void javaNetworkLog(final String message) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSExec("javaNetworkLog", new Object[]{message});
			}
		});
	}

	public void setInviteSipRequest(final SipRequest sipRequest) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSCallback("setInviteSipRequest", new Object[] { sipRequest });
			}
		});
	}

	public boolean sendCommand(String command, String[] args) {
		JSCallback("externalCommand", new Object[] {command,args});
		return true;
	}
}