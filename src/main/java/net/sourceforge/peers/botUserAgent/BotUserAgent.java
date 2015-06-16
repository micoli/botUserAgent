package net.sourceforge.peers.botUserAgent;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.botUserAgent.config.GlobalConfig;
import net.sourceforge.peers.botUserAgent.config.PeerConfig;
import net.sourceforge.peers.botUserAgent.logger.WebLogger;
import net.sourceforge.peers.botUserAgent.logger.WebLoggerOutput;
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
@SuppressWarnings("restriction")
public class BotUserAgent implements SipListener,WebLoggerOutput {

	private UserAgent		userAgent;
	private SipRequest		sipRequest;
	private CommandsReader	commandsReader;
	private ScriptEngine	engine;
	private ExecutorService executorService;
	private Logger			logger;
	private PeerConfig		config;

	public BotUserAgent(ScriptEngine engine,ExecutorService executorService,PeerConfig config) {
		this.executorService= executorService;
		this.config= config;
		this.engine = engine;
		this.engine.getBindings(ScriptContext.ENGINE_SCOPE).put("botUserAgent", this);

		logger = new WebLogger(this);

		JavaxSoundManager javaxSoundManager = new JavaxSoundManager(false, logger, null);

		try {
			userAgent = new UserAgent(this, this.config, logger, javaxSoundManager);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		commandsReader = new CommandsReader(this);
		commandsReader.start();

		JSCallback("initBot",new Object[] { this.config});
	}

	public void instantiatePeers() {
		try {
			String ipAddress = GlobalConfig.config.getInetAddress("bindAddr").toString().replaceAll("/", "");
			System.out.println("instantiatePeers");
			executorService = Executors.newCachedThreadPool();
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
	// commands methods
	public void call(final String callee) {
		executorService.submit(new Runnable() {
			public void run() {
				try {
					sipRequest = userAgent.invite(callee, null);
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

	public void invite(final String uri) {
		executorService.submit(new Runnable() {

			public void run() {
				String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
				try {
					SipRequest sipRequest = userAgent.invite(uri, callId);
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

	public void terminate() {
		executorService.submit(new Runnable() {
			public void run() {
				userAgent.terminate(sipRequest);
			}
		});
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

	public void busy(final SipRequest sipRequest) {
		executorService.submit(new Runnable() {
			public void run() {
				userAgent.rejectCall(sipRequest);

			}
		});
	}

	public void dtmf(final char digit) {
		executorService.submit(new Runnable() {
			public void run() {
				MediaManager mediaManager = userAgent.getMediaManager();
				mediaManager.sendDtmf(digit);
			}
		});
	}

	private void JSCallback(String method,Object[] arguments){
		try {
			((Invocable) engine).invokeFunction("botCb",new Object[] {config.getId(),method, arguments});
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
		JSCallback("incomingCall",new Object[] { sipRequest,provResponse});
	}

	public void remoteHangup(SipRequest sipRequest) {
		JSCallback("remoteHangup",new Object[] { sipRequest});
	}

	public void ringing(SipResponse sipResponse) {
		JSCallback("ringing",new Object[] { sipResponse});
	}

	public void calleePickup(SipResponse sipResponse) {
		JSCallback("calleePickup",new Object[] { sipResponse});
	}

	public void error(SipResponse sipResponse) {
		JSCallback("error",new Object[] { sipResponse});
	}

	public void javaLog(final String message) {
		executorService.submit(new Runnable() {
			public void run() {
				JSCallback("javaLog", new Object[]{message});
			}
		});
	}

	public void javaNetworkLog(final String message) {
		executorService.submit(new Runnable() {
			public void run() {
				JSCallback("javaNetworkLog", new Object[]{message});
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

	public String getFrom(SipRequest sipRequest) {
		SipHeaders sipHeaders = sipRequest.getSipHeaders();
		SipHeaderFieldName sipHeaderFieldName = new SipHeaderFieldName(RFC3261.HDR_FROM);
		SipHeaderFieldValue from = sipHeaders.get(sipHeaderFieldName);
		return from.getValue();
	}
}