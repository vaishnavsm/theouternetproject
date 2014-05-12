package com.vaishnav.theouternetprjt.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class CommServer {
	
	ServerSocket serv;
	Socket sock,sock2;
	ObjectInputStream input, in;
	ObjectOutputStream output, out;
	List<String> cache;
	List<String> myData;
	HashMap<String, String> cacheMap, myDataMap;
	
Thread ServerRun;
public CommServer(){
	ServerRun = new Thread(new Runnable(){

		@Override
		public void run() {
			cache = new ArrayList<String>();
			myData = new ArrayList<String>();
			cacheMap = new HashMap<String, String>();
			myDataMap = new HashMap<String, String>();
			
			readStoredFiles();
			try {
				serv = new ServerSocket(65525, 100);
				
				while(true){
					try{			
					try{
					sock = serv.accept();
					}catch(SocketTimeoutException ignorethisexception1){continue;}
					
					if(sock != null){
						log("Found Connection: "+sock.getInetAddress().getHostAddress());
						input = new ObjectInputStream(sock.getInputStream());
						output = new ObjectOutputStream(sock.getOutputStream());
						output.flush();
						
						//Interact with Client
						
						while(!sock.isClosed()){
							Object inp = input.readObject();
							if(inp != null){
							if(inp.toString() == inp){
								String req = inp.toString();
								inp = null;
								if(req.toLowerCase().contains("datapullreq-")){
									String reqDats[] = req.split("datapullreq-");
									if(reqDats.length>2){log("Other side requesting too much data at once");
									closeSock();
									continue;}
									String reqDat = reqDats[1];
									if(myData.contains(reqDat)){
										log("Serving Stuff From My Data.");
										long then = System.nanoTime();
										String loc = myDataMap.get(reqDat);
										File data = new File(loc);
										byte[] content = Files.readAllBytes(Paths.get(data.toURI()));
										output.writeObject(content);
										double dt = (System.nanoTime()-then)/1000000000.00;
										int bytes = content.length;
										int speed = (int) ((bytes/dt)/128);
										log("Successfully served file. Stats - \nTime taken = "+dt+",   size = "+bytes+" bytes\nSpeed = "+speed);
										Thread.sleep(100);
									}
									else if(cache.contains(reqDat)){
										log("Serving Stuff From Cache");
										long then = System.nanoTime();
										String loc = cacheMap.get(reqDat);
										File data = new File(loc);
										byte[] content = Files.readAllBytes(Paths.get(data.toURI()));
										output.writeObject(content);
										double dt = (System.nanoTime()-then)/1000000000.00;
										int bytes = content.length;
										int speed = (int) ((bytes/dt)/128);
										log("Successfully served file. Stats - \nTime taken = "+dt+",   size = "+bytes+" bytes\nSpeed = "+speed);
										Thread.sleep(100);
									}
									else{
										//Send Content Not Available msg
										log("Content Requested Unavailable");
										output.writeObject("contentunavailable");
										output.flush();
									}
								}							
							}else{
								//The input is not a string
							}}
							
							Thread.yield();
							heartbeat();
							Thread.sleep(100);
							heartbeat();
						}
						
						sock = null;
					}
					Thread.yield();
					}catch(IOException ioEx1){ 
						log("IOException Occured.");
						log(ioEx1.getMessage());}
					catch (ClassNotFoundException e) {
						log("Class Not Found Exception Occured.");
						log(e.getMessage());
					} catch (InterruptedException e) {
						log("Thread Problems");
						log(e.getMessage());
					}
				}
				
				
			} catch (IOException e1) {
				log("IOException Occured.");
				log(e1.getMessage());
			}
			//Thread Maintenance
			Thread.yield();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				log("Thread problems");
				log(e.getMessage());
			}
		}
	});
	ServerRun.start();
}

private void closeSock(){
	try {
		output.close();
	} catch (IOException e) {		}
	try {
		input.close();
		sock.close();
		} catch (IOException e) {
		log("Error while closing socket!\n"+e.getMessage());
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

public void readStoredFiles(){
		Scanner scan;
		try {
			scan = new Scanner(new File("cache.list"));
			while(scan.hasNext()){
				String line = scan.nextLine();
				String[] list = line.split(">");
				if(list.length>2) log("Cache Error, data not unique!");
				else if(list.length==2){
				cache.add(list[0]);
				cacheMap.put(list[0], list[1]);
				}
			}
			scan.close();scan = null;
			
			scan = new Scanner(new File("mydata.list"));
			while(scan.hasNext()){
				String line = scan.nextLine();
				String[] list = line.split(">");
				if(list.length>2) log("My Data Error, data not unique!");
				myData.add(list[0]);
				myDataMap.put(list[0], list[1]);
			}
			scan.close();scan = null;
		} catch (FileNotFoundException e) {
			log("File not found (Cache or  My data) while reading!");
			log(e.getMessage());
		}
		sendWhatAllStuffIHaveToServer();
}

public void writeToStoredFiles(){
	try {
		PrintStream writer = new PrintStream(new File("cache.list"));
		String[] keys = objTostrArr(cache.toArray());
		for(String key:keys){
			String prt = key+">"+cacheMap.get(key);
			writer.println(prt);
		}
		writer.close();writer=null;
		keys = null;
		writer = new PrintStream(new File("mydata.list"));
		String[] keyz = objTostrArr(myData.toArray());
		for(String key:keyz){
			String prt = key+">"+myDataMap.get(key);
			writer.println(prt);
		}
	} catch (FileNotFoundException e) {
		log("File not found (Cache or  My data) while writing!");
	}
}

public void sendWhatAllStuffIHaveToServer(){
	try {
		sock2 = new Socket(CommModule.HOST,6653);
		out = new ObjectOutputStream(sock2.getOutputStream());
		out.flush();
		in = new ObjectInputStream(sock2.getInputStream());
		while(!sock2.isClosed()){
			log("Updating Server About Stuff I have...");
			long then = System.nanoTime();
			out.writeObject("stuffihave");
			out.flush();
			String inp = in.readObject().toString();
			if(inp.equals("listening")){
				List<String> stuffIHave = new ArrayList<String>();
				stuffIHave.addAll(cache);
				stuffIHave.addAll(myData);
				out.writeObject(stuffIHave);
				out.flush();
				closeSock2();
				double dt = (System.nanoTime() - then)/1000000000.00;
				log("Done! Time taken = "+dt);
			}
			
			//local heartbeat
			Thread.yield();
			try {
					out.write(-111222);
					out.flush();
					out.write(222111);
					out.flush();
				} catch (Exception e) {
					log("Server Closed");
					closeSock2();}	
			Thread.sleep(100);
			try {
					out.write(-111222);
					out.flush();
					out.write(222111);
					out.flush();
				} catch (Exception e) {
					closeSock2();}
			
		}
		
	} catch (UnknownHostException e) {
		log("Unknown Host Exception Occured!\n"+e.getMessage());
	} catch (IOException e) {
		log("I/O Exception Occured!\n"+e.getMessage());
	} catch (InterruptedException e) {
		log("Thread Exception Occured!\n"+e.getMessage());
	} catch (ClassNotFoundException e) {
		log("Class Not Found Exception Occured!\n"+e.getMessage());
	}
}

private void closeSock2() {
	try {
		out.close();
	} catch (IOException e) {		}
	try {
		in.close();
		sock2.close();
		} catch (IOException e) {
			log("I/O Exception Occured!\n"+e.getMessage());
	}		
}

public String[] objTostrArr(Object[] obj){
	String[] ret = new String[obj.length];
	for(int i = 0; i<obj.length; i++) ret[i] = obj[i].toString();
	return ret;
}

public void log(String x){Logger.log(x);}

public void addToCache(String address, String absolutePath) {
	if(cache.contains(address)){return;}
	cache.add(address);
	cacheMap.put(address, absolutePath);	
	writeToStoredFiles();
	log("Added "+address+" to Cache");
}
}
