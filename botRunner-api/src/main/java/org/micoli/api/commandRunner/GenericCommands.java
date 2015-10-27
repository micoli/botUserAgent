package org.micoli.api.commandRunner;

import ro.fortsoft.pf4j.ExtensionPoint;

public interface GenericCommands extends ExtensionPoint{
	public String start();
	public void launch(Executor executor);
}
