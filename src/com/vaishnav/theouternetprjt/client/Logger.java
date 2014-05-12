package com.vaishnav.theouternetprjt.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Logger {
	
	public static String SERVER_IP = "127.0.0.1";
	public static final int PORT = 6813;
	
	public static void init(){
		try {
			Scanner ipscan = new Scanner(new File("ip.txt"));
			do{
				String line = ipscan.nextLine();
				if(line.isEmpty()){continue;}
				SERVER_IP = line;
				ipscan.close();
				ipscan = null;
			}while(ipscan!=null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(Object x){
		innerLog(x.toString());
	}
	public static void log(int x){
		innerLog(Integer.toString(x));
	}
	public static void log(String x){
		innerLog(x);
	}
	public static void log(char x){
		innerLog(Character.toString(x));
	}
	
	
	private static void innerLog(String s){
		try {
			Socket sock = new Socket(SERVER_IP, PORT);
			ObjectOutputStream out  = new ObjectOutputStream(sock.getOutputStream());
			out.flush();
			ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
			out.writeObject(s);
			out.flush();
			int x = input.readInt();
			if(x!=1){System.out.println("ERROR, MESSAGE NOT GOING THROUGH!!");}
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
