package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.micoli.api.commandRunner.ExecutorRouter;
import org.micoli.threads.ManagedThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NetworkCommandsClient extends ManagedThread{
	protected final static Logger logger = LoggerFactory.getLogger(NetworkCommandsClient.class);

	private Socket				ClientSocket;
	private ExecutorRouter			executorRouter;
	private BufferedReader		streamIn;
	private DataOutputStream	streamOut;

	NetworkCommandsClient(Socket clientSocket,ExecutorRouter executorRouter)  throws Exception{
		this.executorRouter = executorRouter;;
		this.ClientSocket = clientSocket;
		this.streamIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		this.streamOut = new DataOutputStream(clientSocket.getOutputStream());
		//DataInputStream  streamIn  = new DataInputStream (ClientSocket.getInputStream ());
		//DataOutputStream streamOut = new DataOutputStream(ClientSocket.getOutputStream());
		//Display("Waiting for UserName And Password");
		//LoginName=streamIn.readUTF();
		//Password=streamIn.readUTF();
		isRunning=true;
		start();
	}

	public void run(){
		try{
			if (!isRunning()){
				streamOut.writeUTF("Quitting>");
			}
			while(isRunning()){
				String command = streamIn.readLine();
				if (command==null || command.equals("quit")){
					isRunning=false;
				}else{
					String exe=executorRouter.executeCommand(command);
					streamOut.writeUTF(exe+"\n>");
					logger.debug("NetworkCommandsClient execute :"+command.trim()+":"+exe);
				}
			}
			ClientSocket.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}