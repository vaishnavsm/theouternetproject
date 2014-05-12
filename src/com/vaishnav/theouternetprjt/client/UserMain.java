package com.vaishnav.theouternetprjt.client;

import javax.swing.JFrame;

public class UserMain {

	public static void main(String[] args) {
		Logger.init();
		Logger.log("\n\nNEW SESSION\n");
		Client client = new Client();
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.run();
	}

}
