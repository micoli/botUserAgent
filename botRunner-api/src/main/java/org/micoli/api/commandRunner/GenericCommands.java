package org.micoli.api.commandRunner;

import ro.fortsoft.pf4j.ExtensionPoint;

public interface GenericCommands extends ExtensionPoint{
	void launch(ExecutorRouter executor);
}
