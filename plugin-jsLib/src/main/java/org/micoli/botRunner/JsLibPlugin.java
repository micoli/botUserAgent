package org.micoli.botRunner;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.micoli.botUserAgent.BotsManagerApi;
import org.micoli.botUserAgent.BotsManagerPlugin;
import org.micoli.http.Client;
import org.micoli.processes.SyncExec;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
public class JsLibPlugin extends Plugin implements BotsManagerPlugin{
	private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	private BotsManagerApi botsManagerApi;

	public JsLibPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void setBotsManager(BotsManagerApi botsManagerApi) {
		logger.debug("SetBotsManager init");
		this.botsManagerApi = botsManagerApi;
		ScriptEngine engine =this.botsManagerApi.getEngine();
		Bindings bindings=engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("http"		, new Client());
		bindings.put("syncExec"	, SyncExec.get());
		logger.debug("setBotsManager end");
	}

	@Override
	public void start() {
		logger.debug("jsLibPlugin.start()");
	}

	@Override
	public void stop() {
		logger.debug("jsLibPlugin.stop()");
	}

}