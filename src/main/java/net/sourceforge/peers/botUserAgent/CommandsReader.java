package net.sourceforge.peers.botUserAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandsReader extends Thread {

    public static final String CALL = "call";
    public static final String HANGUP = "hangup";

    private boolean isRunning;
    private BotUserAgent botUserAgent;
    
    public CommandsReader(BotUserAgent botUserAgent) {
        this.botUserAgent = botUserAgent;
    }
    
	@Override
    public void run() {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        setRunning(true);
        while (isRunning()) {
            String command;
            try {
                command = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            command = command.trim();
            if (command.startsWith(CALL)) {
                String callee = command.substring(command.lastIndexOf(' ') + 1);
                botUserAgent.call(callee);
            } else if (command.startsWith(HANGUP)) {
            	botUserAgent.terminate();
            } else {
                System.out.println("unknown command " + command);
            }
        }
    }
    
    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
