package org.micoli.commandRunner;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.micoli.threads.ManagedThread;

class NetworkCommandsClient extends ManagedThread{
	private Socket				ClientSocket;
	private Executor			executor;
	private BufferedReader		streamIn;
	private DataOutputStream	streamOut;

	NetworkCommandsClient(Socket clientSocket,Executor executor)  throws Exception{
		this.executor = executor;;
		this.ClientSocket = clientSocket;
		this.streamIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		this.streamOut = new DataOutputStream(clientSocket.getOutputStream());
		//DataInputStream  streamIn  = new DataInputStream (ClientSocket.getInputStream ());
		//DataOutputStream streamOut = new DataOutputStream(ClientSocket.getOutputStream());
		//System.out.println("Waiting for UserName And Password");
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
					streamOut.writeUTF(executor.executeCommand(command)+"\n>");
				}
			}
			ClientSocket.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}