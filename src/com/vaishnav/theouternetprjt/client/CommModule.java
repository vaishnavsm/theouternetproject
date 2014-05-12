package com.vaishnav.theouternetprjt.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommModule {
	
	static final int PORT = 6653;
	static String HOST = "localhost";
	
	Thread heartbeat;
	
	Socket socket;
	ObjectOutputStream output;
	ObjectInputStream input;
	
	public CommModule(){
		HOST = Logger.SERVER_IP;
	}
	
	public void request(String address) {
		
		try{
			socket = new Socket(HOST, PORT);
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(socket.getInputStream());
			output.writeObject("IREQ;"+address);
			output.flush();
			
			while(!socket.isClosed()){
				Object inp = input.readObject();
				if(inp.toString().contains(address)&&inp.toString().contains("SERVER ECHO")){closeSock();}
				String[] loc;
				if(Integer.parseInt(inp.toString())==Constants.SERVER_IID_LOC_LIST){
					Object obj = input.readObject();
					if(obj==null){log("Requested for "+address+", server doesn't have entry.");
					Client.writeStringToContentPane("Sorry, Content Not Available!");}
					loc = (String[])obj;
					closeSock();
					if(loc.length==1&&loc[0].equals("NOTAVAILABLE")){
						log("Requested for "+address+", server doesn't have entry.");
						Client.writeStringToContentPane("Sorry, Content Not Available!");
						return;
					}
					log("Connecting with random user that has said info.");
					List<String> locl = new ArrayList<String>();
					for(String s:loc) locl.add(s);
					boolean done = false;
					while(!done){
					int index=0;
					try{
					index = ((int)(Math.random()))%locl.size();
					socket = new Socket(loc[index],65525);
					dealWithLocalPC(address);
					done = true;
					}catch(IOException e){
						locl.remove(index);
					}}
				}
				Thread.yield();
				heartbeat();
				Thread.sleep(100);
				heartbeat();
			}
		}
		catch(IOException e){
			log("I/O Error with server!\n"+e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			log("Class Not Found Exception!\n"+e.getMessage());
		} catch (InterruptedException e) {
			log("Thread problems\n"+e.getMessage());
		}
	}
	
	private void dealWithLocalPC(String address) {
		
		try {
			log("Attempting to get file, starting timer...");
			long then = System.nanoTime();
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(socket.getInputStream());
			while(!socket.isClosed()){
				
				output.writeObject("datapullreq-"+address);
				Object recv = input.readObject();
				if(recv.toString() == recv){if(recv.equals("contentunavailable")){
					double dt = (System.nanoTime() - then)/1000000000.00;
					log("The content requested was unavailable!\nTime Taken = "+dt);
					Client.writeStringToContentPane("Sorry! The data you requested is not available!");
					closeSock();
				}
				}else{log("Got File!");
				byte[] bytes = (byte[]) recv;
				File rec;
				Friends.add(socket.getInetAddress().getHostName(),socket.getInetAddress().getHostAddress());
				double dt = (System.nanoTime() - then)/1000000000.00;
				int size = bytes.length;
				int speed = (int)(size/dt)/128;
				log("Stats - Time = "+dt+"s, Size = "+size+"bytes, Speed = "+speed+"Kbps.");
				try{
					log("Making new file");
				rec = Files.createFile(Paths.get(System.getProperty("user.dir")+File.separatorChar+"cache"+File.separatorChar+address+".htm")).toFile();
				log("Made mile, opening stream");	
				PrintStream writer = new PrintStream(rec);
				log("Opened stream");
				writer.write(bytes);
				writer.flush();
				log("Closing stream");
				writer.close();
				}catch(FileAlreadyExistsException faee){
					log("OOps");
					rec = new File(System.getProperty("user.dir")+File.separatorChar+"cache"+File.separatorChar+address+".htm");
					
					//Ignore :P
				}
				Client.showHTML(rec);
				Client.serv.addToCache(address, rec.getAbsolutePath());
				closeSock();}
				
				Thread.yield();
				heartbeat();
				Thread.sleep(100);
				heartbeat();
			}
		} catch (IOException e) {
			log("I/O Exception! = "+e.getMessage());
		} catch (InterruptedException e) {
			log("Thread Exception!\n"+e.getMessage());
		} catch (ClassNotFoundException e) {
			log("Class Not Found Exception!\n"+e.getMessage());
		}
		
	}

	private void closeSock() {
		try {
			output.close();
		} catch (IOException e) {}
		try {
			input.close();
		} catch (IOException e) {
			log("SOCKET CLOSE FAILED!");
		}
		try {
			socket.close();
		} catch (IOException e) {
			log("SOCKET CLOSE FAILED!");
		}
	}
	
	private void heartbeat(){
		try {
			output.write(-111222);
			output.flush();
			output.write(222111);
			output.flush();
		} catch (Exception e) {
			closeSock();
		}	
	}
	
	public void log(String x){
		Logger.log(x);
	}
}
