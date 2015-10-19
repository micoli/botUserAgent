package net.sourceforge.peers.botUserAgent;

import java.io.IOException;
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
import net.sourceforge.peers.botUserAgent.sip.SipUtils;
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
import org.micoli.commandRunner.CommandArgs;
import org.micoli.commandRunner.CommandRoute;
import org.micoli.commandRunner.CommandRunner;
import org.micoli.commandRunner.Executor;

public class BotUserAgent implements SipListener,CommandRunner {
	private BotsManager					botsManager;
	private ScheduledExecutorService	scheduledExecutor;
	private UserAgent					userAgent;
	private Logger						logger;
	private PeerConfig					config;
	private Executor					executor;

	public BotUserAgent(BotsManager botsManager,PeerConfig config,Logger logger) {
		this.botsManager	= botsManager;
		this.logger			= logger;
		this.config			= config;
		this.executor		= new Executor(this);

		BotSoundManager javaxSoundManager = new BotSoundManager(logger);

		try {
			userAgent = new UserAgent(this, this.config, logger, javaxSoundManager);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		botsManager.JSExec("initBot",new Object[] {this.config.getId(), this.config,this});


		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				JSCallback("tick",new Object[] {});
			}
		}, 20, 20, TimeUnit.SECONDS);

	}

	@CommandRoute(value="ping", args={"to"})
	public String ping(CommandArgs commandArgs) {
		System.out.println( commandArgs.getDefault("val1", "_VAL1")+" "+commandArgs.getDefault("val2", "_VAL2"));
		return "eeee";

	}
	public String execute(String route, CommandArgs commandArgs) {
		return this.executor.execute(route, commandArgs);
	}

	public void instantiatePeers() {
		try {
			String ipAddress = GlobalConfig.config.getInetAddress("bindAddr").toString().replaceAll("/", "");
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

	@CommandRoute(value="call", args={"to"})
	public void call(final CommandArgs commandArgs) {
		String callee = ((!commandArgs.get("to").startsWith("sip:"))?"sip:":"")+commandArgs.get("to");
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

	public void setAnswerFile(String filename) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.file);
				config.setMediaFile(filename);
			}
		});
	}

	public void setAnswerNone() {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.none);
			}
		});
	}

	public void setAnswerEcho() {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.echo);
			}
		});
	}

	public void setAnswerCaptureAndPlayback() {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				config.setMediaMode(MediaMode.captureAndPlayback);
			}
		});
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

	public void setInviteSipRequest(final SipRequest sipRequest) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				JSCallback("setInviteSipRequest", new Object[] { sipRequest });
			}
		});
	}

	/*public boolean sendCommand(String command, CommandArgs commandArgs) {
		JSCallback("externalCommand", new Object[] {command,commandArgs});
		return true;
	}*/

	public void exec(String bin) {
		botsManager.getExecutorService().submit(new Runnable() {
			public void run() {
				try {
					Process p = Runtime.getRuntime().exec(bin);
					p.waitFor();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}