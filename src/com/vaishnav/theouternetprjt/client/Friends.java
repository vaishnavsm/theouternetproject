package com.vaishnav.theouternetprjt.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.swing.JList;

//This Class Teaches You To Manipulate Your Friends :D :D :D

public class Friends{
	
	static List<String> friends;
	static HashMap<String, String> friendLoc;
	static ObjectOutputStream output;
	static ObjectInputStream input;
	static Socket socket;
	
	public static void init(){
		friends = new ArrayList<String>();
		friendLoc = new HashMap<String,String>();
		
		//Load friendsList
		Scanner scan;
		
			try {
				scan = new Scanner(new File("friends.list"));
		
			while(scan.hasNext()){
				String line = scan.nextLine();
				String[] list = line.split(">");
				if(list.length>2) log("Error found, more than one address to a single friend");
				else if(list.length==2){
				friends.add(list[0].toLowerCase());
				friendLoc.put(list[0].toLowerCase(), list[1]);
				}
			}
			scan.close();scan = null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
	
		}

	private static void log(String x){
		Logger.log(x);
	}

	public static JList<String> populate() {
		JList<String> ret = new JList<String>();
		
		ret.setListData(objTostrArr(friends.toArray()));
		
		return ret;
	}
	
	public static String[] objTostrArr(Object[] obj){
		String[] ret = new String[obj.length];
		for(int i = 0; i<obj.length; i++) ret[i] = obj[i].toString();
		return ret;
	}

	public static void sendTo(String friend) {
		friend = friend.toLowerCase();
		if(!friends.contains(friend)) {log("Tried to navigate to non existant friend");return;}
		String loc = friendLoc.get(friend);
		try {
			socket = new Socket(loc, 63720);
			try {
				log("Attempting to get file, starting timer...");
				long then = System.nanoTime();
				output = new ObjectOutputStream(socket.getOutputStream());
				output.flush();
				input = new ObjectInputStream(socket.getInputStream());
				while(!socket.isClosed()){
					output.writeObject("datapullreq-index");
					Object recv = input.readObject();
					if(recv.toString() == recv){if(recv.equals("contentunavailable")){
						log("Friend at "+loc+" does not have index page");
						double dt = (System.nanoTime() - then)/1000000000.00;
						log("The content requested was unavailable!\nTime Taken = "+dt);
						Client.writeStringToContentPane("Sorry! The data you requested is not available!");
						closeSock();
					}
					}else{
						byte[] bytes = (byte[]) recv;
						File rec;
						double dt = (System.nanoTime() - then)/1000000000.00;
						int size = bytes.length;
						int speed = (int)(size/dt)/128;
						log("Stats - Time = "+dt+"s, Size = "+size+"bytes, Speed = "+speed+"Kbps.");
						try{
						Files.createDirectory(Paths.get(System.getProperty("user.dir")+File.separatorChar+"cache"));
						rec = Files.createFile(Paths.get(System.getProperty("user.dir")+File.separatorChar+"cache"+File.separatorChar+loc+".htm")).toFile();
						PrintStream writer = new PrintStream(rec.getAbsoluteFile());
						writer.write(bytes);
						writer.close();
						}catch(FileAlreadyExistsException faee){
							rec = new File(System.getProperty("user.dir")+File.separatorChar+"cache"+File.separatorChar+loc+".htm");
							//Ignore :P
						}
						Logger.log("Tried to get file, name = "+rec.getName()+"  path = "+rec.getAbsolutePath());
						Client.showHTML(rec);
						closeSock();}
					
					Thread.yield();
					heartbeat();
					Thread.sleep(100);
					heartbeat();
				}
			} catch (IOException e) {
				log("IOException occured");
				log(e.getMessage());
			} catch (InterruptedException e) {
				log("InterruptedException occured");
				log(e.getMessage());
			} catch (ClassNotFoundException e) {
				log("Class Not Found Exception occured");
				log(e.getMessage());
			}

		} catch (UnknownHostException e) {
			log("Unknown Host!");
			log(e.getMessage());
		} catch (IOException e) {
			log("IOException occured");
			log(e.getMessage());
		}
	}
	
	private static void closeSock() {
		try {
			output.close();
		} catch (IOException e) {log("SOCKET CLOSE FAILED!");}
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
	
	private static void heartbeat(){
		try {
			output.write(-111222);
			output.flush();
			output.write(222111);
			output.flush();
		} catch (Exception e) {
			closeSock();
		}	
	}

	public static void add(String hostName, String hostAddress) {
		if(friends.contains(hostName)){return;}
		System.out.println("Adding Friend");
		friends.add(hostName.toLowerCase());
		friendLoc.put(hostAddress.toLowerCase(), hostAddress);
		Client.friendsBoxReset();
		try {
			FileWriter wr = new FileWriter(new File("friends.list"),true);
			wr.write(System.lineSeparator()+hostName+">"+hostAddress);
			wr.flush();
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
