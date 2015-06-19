package net.sourceforge.peers.botUserAgent;
//hashmap by callid
//encode/decode siprequest


import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.logger.CliLogger;
import net.sourceforge.peers.botUserAgent.logger.CliLoggerOutput;
import net.sourceforge.peers.javaxsound.JavaxSoundManager;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class BotUserAgent implements SipListener,CliLoggerOutput {
	private ScriptEngine					engine;
	private ExecutorService					executorService;
	private ScheduledExecutorService		scheduledExecutor;
	private UserAgent						userAgent;
	private SipRequest						aaaaa;
	private Logger							logger;
	private PeerConfig						config;
	private HashMap<String, SipRequest>		sipRequests;

	public BotUserAgent(ScriptEngine engine,ExecutorService executorService,PeerConfig config) {
		this.logger			= new CliLogger(this);
		this.executorService= executorService;
		this.config			= config;
		this.engine			= engine;
		this.sipRequests	= new HashMap<String, SipRequest>();
		
		JavaxSoundManager javaxSoundManager = new JavaxSoundManager(false, logger, null);
		
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
			System.out.println("instantiatePeers");
			String peersHome = Utils.DEFAULT_PEERS_HOME;
			final AbstractSoundManager soundManager = new JavaxSoundManager(
					false, //TODO config.isMediaDebug(),
					logger, peersHome);
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e1) {
				System.out.println(e1 + " " + e1.getMessage());
				logger.error("Unknown ipAddress " + ipAddress, e1);
				return;
			}
			this.config.setLocalInetAddress(inetAddress);
			this.config.setMediaMode(MediaMode.captureAndPlayback);

			executorService.submit(new Runnable() {
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
		executorService.submit(new Runnable() {
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
		executorService.submit(new Runnable() {
			public void run() {
				userAgent.close();
			}
		});
	}
	
	private void storeSipRequest(final SipRequest sipRequest){
		String callId = net.sourceforge.peers.botUserAgent.misc.Utils.getCallId(sipRequest);
		if(!this.sipRequests.containsKey(callId)){
			this.sipRequests.put(callId, sipRequest);
		}
	}

	private SipRequest getSipRequest(String callId){
		if(this.sipRequests.containsKey(callId)){
			return this.sipRequests.get(callId);
		}
		return null;
	}
	
	// commands methods
	public void call(final String callee) {
		executorService.submit(new Runnable() {
			public void run() {
				try {
					storeSipRequest(userAgent.invite(callee, null));
				} catch (SipUriSyntaxException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void acceptCall(final SipRequest sipRequest) {
		executorService.submit(new Runnable() {
			public void run() {
				String callId = Utils.getMessageCallId(sipRequest);
				DialogManager dialogManager = userAgent.getDialogManager();
				Dialog dialog = dialogManager.getDialog(callId);
				userAgent.acceptCall(sipRequest, dialog);
			}
		});
	}

	public void acceptCallByCallId(final String callId) {
		final SipRequest oSIPRequest = this.getSipRequest(callId);
		if(oSIPRequest != null){
			executorService.submit(new Runnable() {
				public void run() {
					DialogManager dialogManager = userAgent.getDialogManager();
					Dialog dialog = dialogManager.getDialog(callId);
					userAgent.acceptCall(oSIPRequest, dialog);
				}
			});
		}
	}
	
	public void invite(final String uri) {
		executorService.submit(new Runnable() {
			public void run() {
				String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
				try {
					SipRequest sipRequest = userAgent.invite(uri, callId);
					storeSipRequest(sipRequest);
					setInviteSipRequest(sipRequest);
				} catch (SipUriSyntaxException e) {
					logger.error(e.getMessage());
				}
			}
		});

	}

	public void unregister() {
		executorService.submit(new Runnable() {
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
		executorService.submit(new Runnable() {
			public void run() {
				userAgent.terminate(sipRequest);
			}
		});
	}

	public void terminateByCallId(String callId) {
		final SipRequest oSIPRequest = this.getSipRequest(callId);
		if(oSIPRequest != null){
			terminate(oSIPRequest);
		}
	}
	
	public void pickup(final SipRequest sipRequest) {
		executorService.submit(new Runnable() {
			public void run() {
				String callId = Utils.getMessageCallId(sipRequest);
				DialogManager dialogManager = userAgent.getDialogManager();
				Dialog dialog = dialogManager.getDialog(callId);
				userAgent.acceptCall(sipRequest, dialog);
			}
		});
	}

	public void pickupByCallId(String callId) {
		final SipRequest oSIPRequest = this.getSipRequest(callId);
		if(oSIPRequest != null){
			pickup(oSIPRequest);
		}
	}
	
	public void busy(final SipRequest sipRequest) {
		executorService.submit(new Runnable() {
			public void run() {
				userAgent.rejectCall(sipRequest);
			}
		});
	}

	public void busyByCallId(String callId) {
		final SipRequest oSIPRequest = this.getSipRequest(callId);
		if(oSIPRequest != null){
			busy(oSIPRequest);
		}
	}
	
	public void dtmf(final char digit) {
		executorService.submit(new Runnable() {
			public void run() {
				MediaManager mediaManager = userAgent.getMediaManager();
				mediaManager.sendDtmf(digit);
			}
		});
	}

	private void JSExec(String method,Object[] arguments){
		try {
			((Invocable) engine).invokeFunction(method, arguments);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			System.out.println("JS Error : "+ method + ", args " + arguments+" "+e.getFileName()+"("+e.getLineNumber()+','+e.getColumnNumber() +")");
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
						obj.put("body"			, sipObject.getBody());
						sipHeaders =  sipObject.getSipHeaders();
					}
					if(arguments[i].getClass().getName().endsWith(".SipRequest")){
						SipRequest sipObject = (SipRequest) arguments[i];
						obj.put("method"		, sipObject.getMethod());
						obj.put("requestUri"	, sipObject.getRequestUri().toString());
						obj.put("from"			, net.sourceforge.peers.botUserAgent.misc.Utils.getFrom(sipObject));
						obj.put("sipVersion"	, sipObject.getSipVersion());
						//obj.put("sipHeaders"	, sipObject.getSipHeaders());
						obj.put("body"			, sipObject.getBody());
						sipHeaders =  sipObject.getSipHeaders();
					}
					HashMap<String,String> headers = new HashMap<String,String>();
					for(SipHeaderFieldName hdr :net.sourceforge.peers.botUserAgent.misc.Utils.sipHeaderList){
						if(sipHeaders.contains(hdr)){
							headers.put(hdr.getName(), JSONValue.toJSONString(sipHeaders.get(hdr)));
						}
					}
					obj.put("sipHeaders"	, headers);
					arguments[i]=obj.toJSONString();
				}
			}
			((Invocable) engine).invokeFunction("botCb",config.getId(),method, arguments);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	// SipListener methods
	public void registering(SipRequest sipRequest) {
		JSCallback("registering",new Object[] { sipRequest,config});
	}

	public void registerSuccessful(SipResponse sipResponse) {
		JSCallback("registerSuccessful",new Object[] { sipResponse,config});
	}

	public void registerFailed(SipResponse sipResponse) {
		JSCallback("registerFailed",new Object[] { sipResponse,config});
	}

	public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
		storeSipRequest(sipRequest);
		JSCallback("incomingCall",new Object[] { sipRequest,provResponse,Utils.getMessageCallId(sipRequest)});
	}

	public void remoteHangup(SipRequest sipRequest) {
		JSCallback("remoteHangup",new Object[] { sipRequest,Utils.getMessageCallId(sipRequest)});
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
		executorService.submit(new Runnable() {
			public void run() {
				JSExec("javaLog", new Object[]{message});
			}
		});
	}

	//CliLoggerOutput
	public void javaNetworkLog(final String message) {
		executorService.submit(new Runnable() {
			public void run() {
				JSExec("javaNetworkLog", new Object[]{message});
			}
		});
	}

	public void setInviteSipRequest(final SipRequest sipRequest) {
		executorService.submit(new Runnable() {
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