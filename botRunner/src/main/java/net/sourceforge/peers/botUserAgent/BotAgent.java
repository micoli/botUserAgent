package net.sourceforge.peers.botUserAgent;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.sip.SipUtils;
import net.sourceforge.peers.javaxsound.BotSoundManager;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.media.SoundSource;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.micoli.api.commandRunner.CommandArgs;
import org.micoli.api.commandRunner.CommandRoute;
import org.micoli.api.commandRunner.CommandRunner;
import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.botUserAgent.AudioPlugin;
import org.micoli.botUserAgent.BotsManagerApi;
import org.micoli.botUserAgent.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotAgent implements SipListener,CommandRunner,AudioPlugin {
	protected final Logger				logger = LoggerFactory.getLogger(getClass());
	private BotsManager					botsManager;
	private ScheduledExecutorService	scheduledExecutor;
	private BotUserAgent				userAgent;
	private PeerConfig					config;
	private ExecutorRouter				executorRouter;
	private String 						lastStatus;
	private String						lastCallId;

	public BotAgent(BotsManager botsManager,PeerConfig config) {
		this.botsManager	= botsManager;
		this.config			= config;
		this.executorRouter	= new ExecutorRouter(this,true);

		BotSoundManager javaxSoundManager = new BotSoundManager();
		setAnswerFile("/tmp/null.raw");
		try {
			userAgent = new BotUserAgent(this, this.config, logger, javaxSoundManager);
		} catch (SocketException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}

		botsManager.JSExec("initBot",new Object[] {this.config.getId(), this.config,this});

		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				JSCallback("tick",new Object[] {});
			}
		}, 20, 20, TimeUnit.SECONDS);
	}

	@CommandRoute(value="getActiveCall")
	public String getActiveCall(CommandArgs commandArgs) {
		Dialog dialog = this.getActiveCall();
		return (dialog==null)?"":dialog.getRemoteUri()+" "+dialog.getCallId()+" "+dialog.getState().toString();
	}

	public Dialog getActiveCall(){
		for (Dialog dialog : this.userAgent.getDialogManager().getDialogCollection()) {
			if (dialog.getRemoteUri() != null && !dialog.getState().equals(dialog.TERMINATED) && !dialog.getState().equals(dialog.EARLY)) {
				return dialog;
			}
		}
		return null;
	}

	public String getLastStatus(){
		return lastStatus;
	}

	private void setLastStatus(String lastStatus) {
		this.lastStatus = lastStatus;
	}

	public String execute(String route, CommandArgs commandArgs) {
		logger.debug("route: "+route);
		return this.executorRouter.execute(route, commandArgs);
	}

	public void instantiatePeers() {
		try {
			String ipAddress = GlobalConfig.getConfig().getInetAddress(GlobalConfig.optBindAddr).toString().replaceAll("/", "");
			final BotSoundManager soundManager = new BotSoundManager();
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

			BotsManager.getExecutorService().submit(new Runnable() {
				public void run() {
					try {
						userAgent = new BotUserAgent(BotAgent.this, config,null, soundManager);
					} catch (SocketException e) {
						logger.error(e.getMessage());
					}
				}
			});
		} catch (SecurityException e) {
			logger.error(e.getMessage());
			logger.error(e.getClass().getSimpleName(), e);
		}
	}

	public void register() {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				try {
					userAgent.register();
				} catch (SipUriSyntaxException e) {
					logger.error(e.getClass().getSimpleName(), e);
				}
			}
		});
	}

	public void close() {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.close();
			}
		});
	}

	@CommandRoute(value="call", args={"to"})
	public void call(final CommandArgs commandArgs) {
		String callee = ((!commandArgs.get("to").startsWith("sip:"))?"sip:":"")+commandArgs.get("to");
		this.setLastStatus("call "+callee);
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				try {
					botsManager.storeSipRequest(userAgent.invite(callee, null));
				} catch (SipUriSyntaxException e) {
					logger.error(e.getClass().getSimpleName(), e);
				}
			}
		});
	}

	public String getLastCallId(){
		return lastCallId;
	}

	public void acceptCall(final SipRequest sipRequest) {
		String callId = Utils.getMessageCallId(sipRequest);
		lastCallId = callId;
		this.setLastStatus("acceptCall "+callId);
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.acceptCall(sipRequest, userAgent.getDialogManager().getDialog(callId));
			}
		});
	}

	public void acceptCallByCallId(final String callId) {
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		lastCallId = callId;
		this.setLastStatus("acceptCallByCallId "+callId);
		if(oSIPRequest != null){
			BotsManager.getExecutorService().submit(new Runnable() {
				public void run() {
					userAgent.acceptCall(oSIPRequest, userAgent.getDialogManager().getDialog(callId));
				}
			});
		}
	}

	public void invite(final String uri) {
		this.setLastStatus("invite "+uri);
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
				try {
					lastCallId = callId;
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
		this.setLastStatus("unregister");
		BotsManager.getExecutorService().submit(new Runnable() {
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
		lastCallId = "";
		this.setLastStatus("terminate "+sipRequest.getRequestUri());
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.terminate(sipRequest);
			}
		});
	}

	public void terminateByCallId(String callId) {
		lastCallId = "";
		this.setLastStatus("terminateByCallId "+callId);
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			terminate(oSIPRequest);
		}
	}

	public void pickup(final SipRequest sipRequest) {
		String callId = Utils.getMessageCallId(sipRequest);
		lastCallId = callId;
		this.setLastStatus("pickup "+callId);
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				DialogManager dialogManager = userAgent.getDialogManager();
				Dialog dialog = dialogManager.getDialog(callId);
				userAgent.acceptCall(sipRequest, dialog);
			}
		});
	}

	public void pickupByCallId(String callId) {
		lastCallId = callId;
		this.setLastStatus("pickupByCallId "+callId);
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			pickup(oSIPRequest);
		}
	}

	public void busy(final SipRequest sipRequest) {
		this.setLastStatus("busy "+sipRequest.getRequestUri());
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				userAgent.rejectCall(sipRequest);
			}
		});
	}

	public void busyByCallId(String callId) {
		this.setLastStatus("busyByCallId "+callId);
		final SipRequest oSIPRequest = botsManager.getSipRequest(callId);
		if(oSIPRequest != null){
			busy(oSIPRequest);
		}
	}

	public void dtmf(final char digit) {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				MediaManager mediaManager = userAgent.getMediaManager();
				mediaManager.sendDtmf(digit);
			}
		});
	}

	public void setAnswerFile(String filename) {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.file);
				config.setMediaFile(filename);
			}
		});
	}

	public void setAnswerNone() {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.none);
			}
		});
	}

	public void setAnswerEcho() {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.echo);
			}
		});
	}

	public void setAnswerCaptureAndPlayback() {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.captureAndPlayback);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static Object[] arrayToJson(Object[] args){
		for(int i = 0; i < args.length; i++) {
			if(	args[i].getClass().getName().endsWith(".SipResponse") ||
					args[i].getClass().getName().endsWith(".SipRequest")
			){
				JSONObject obj=new JSONObject();
				SipHeaders sipHeaders = null;
				if(args[i].getClass().getName().endsWith(".SipResponse")){
					SipResponse sipObject = (SipResponse) args[i];
					obj.put("statusCode"	, sipObject.getStatusCode());
					obj.put("reasonPhrase"	, sipObject.getReasonPhrase());
					obj.put("sipVersion"	, sipObject.getSipVersion());
					//obj.put("sipHeaders"	, sipObject.getSipHeaders());
					if(sipObject.getBody()!=null){
						obj.put("body"			, new String(sipObject.getBody()));
					}
					sipHeaders =  sipObject.getSipHeaders();
				}
				if(args[i].getClass().getName().endsWith(".SipRequest")){
					SipRequest sipObject = (SipRequest) args[i];
					obj.put("method"		, sipObject.getMethod());
					obj.put("requestUri"	, sipObject.getRequestUri().toString());
					obj.put("from"			, SipUtils.getFrom(sipObject));
					obj.put("sipVersion"	, sipObject.getSipVersion());
					if(sipObject.getBody()!=null){
						obj.put("body"			, new String(sipObject.getBody()));
					}
					//obj.put("sipHeaders"	, sipObject.getSipHeaders());
					sipHeaders =  sipObject.getSipHeaders();
				}
				HashMap<String,String> headers = new HashMap<String,String>();
				for(SipHeaderFieldName hdr :SipUtils.sipHeaderList){
					if(sipHeaders.contains(hdr)){
						headers.put(hdr.getName(), JSONValue.toJSONString(sipHeaders.get(hdr)));
					}
				}
				obj.put("sipHeaders"	, headers);
				args[i]=obj.toJSONString();
			}
		}
		return args;
	}

	private void JSCallback(String method,Object[] arguments){
		try {
			botsManager.getInvocableEngine().invokeFunction("botCb",config.getId(),method, arrayToJson(arguments));
		} catch (NoSuchMethodException e) {
			logger.error(e.getClass().getSimpleName(), e);
		} catch (ScriptException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
	}

	// SipListener methods
	public void registering(SipRequest sipRequest) {
		this.setLastStatus("registering");
		JSCallback("registering",new Object[] { sipRequest,config});
		this.botsManager.removeSipRequest(Utils.getMessageCallId(sipRequest));
	}

	public void registerSuccessful(SipResponse sipResponse) {
		this.setLastStatus("registerSuccessful");
		JSCallback("registerSuccessful",new Object[] { sipResponse,config});
	}

	public void registerFailed(SipResponse sipResponse) {
		this.setLastStatus("registerFailed");
		JSCallback("registerFailed",new Object[] { sipResponse,config});
	}

	public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
		//Dialog dialog = this.getActiveCall();
		//return (dialog==null)?"":dialog.getRemoteUri()+" "+dialog.getCallId()+" "+dialog.getState().toString();
		this.setLastStatus("incomingCall");
		botsManager.storeSipRequest(sipRequest);
		JSCallback("incomingCall",new Object[] { sipRequest,provResponse,Utils.getMessageCallId(sipRequest)});
	}

	public void remoteHangup(SipRequest sipRequest) {
		this.setLastStatus("remoteHangup");
		JSCallback("remoteHangup",new Object[] { sipRequest,Utils.getMessageCallId(sipRequest)});
		this.botsManager.removeSipRequest(Utils.getMessageCallId(sipRequest));
	}

	public void ringing(SipResponse sipResponse) {
		this.setLastStatus("ringing");
		JSCallback("ringing",new Object[] {  sipResponse});
	}

	public void calleePickup(SipResponse sipResponse) {
		this.setLastStatus("calleePickup");
		JSCallback("calleePickup",new Object[] { sipResponse});
	}

	public void error(SipResponse sipResponse) {
		this.setLastStatus("error");
		JSCallback("error",new Object[] { sipResponse});
	}

	public void setInviteSipRequest(final SipRequest sipRequest) {
		BotsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSCallback("setInviteSipRequest", new Object[] { sipRequest });
			}
		});
	}

	@CommandRoute(value="ping", args={"to"})
	public String ping(CommandArgs commandArgs) {
		return "pong: "+commandArgs.getDefault("to", "-");
	}

	@Override
	public void playAudioFile(String callId, String filename) {
		try {
			userAgent.sendAudioFile(this.botsManager.getSipRequest(callId), filename);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getClass().getSimpleName(), e);
		}
	}

	@Override
	public void playAudioSource(String callId, SoundSource source) {
	}

	@Override
	public BotsManagerApi getBotsManager() {
		return botsManager;
	}
}