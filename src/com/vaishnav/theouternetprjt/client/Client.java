package com.vaishnav.theouternetprjt.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

public class Client extends JFrame {
	
	//Serial UID Stuff
	private static final long serialVersionUID = -3364972635260907464L;
	
	//Finals (ie, constants)
		
	
	//Global Variables
	
	String address;
	
	JTextField addressBar;
	static JPanel addressBar_holder, contentArea, publicChatBox, friendsBox;
	
	static JEditorPane pane;
	
	static CommModule comm;
	static CommServer serv;
	
	static JScrollPane friendsBox_List_Scroller;

	static JScrollPane contentScroller;
	JLabel friendsBox_Label;
	static JList<String> friendsBox_List;
	JTextField friendsBox_Search;
	
	//Constructor - Super() and Init any variables that need to be
	public Client(){
		super("ThOrB: The Outernet Browser");
		comm = new CommModule();
		serv = new CommServer();
	}
	
	// "Run" Method, skeletal structure of the client
	public void run(){
		
		//Init GUI
		this.setSize(800, 600);
		
		this.setLayout(new BorderLayout());
		
		//Address Bar
		layoutAddressBar();
		
		//Content Pane
		contentArea = new JPanel(new BorderLayout());
		contentArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		contentArea.setBackground(Color.WHITE);
		
		pane = new JEditorPane();
		pane.setContentType("text/html");
		contentArea.add(pane);
		
		contentScroller = new JScrollPane();
		contentScroller.getViewport().add(contentArea);
		
		add(contentScroller, BorderLayout.CENTER);
		
		//Friends
		layoutFriendsBox();
			
		this.setVisible(true);
				
	}

	private void layoutAddressBar() {
		addressBar = new JTextField("Address here");
		
		addressBar.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				address = e.getActionCommand();		
				comm.request(address);
			}
			
		});
		addressBar_holder = new JPanel();
		addressBar_holder.setMaximumSize(addressBar.getMaximumSize());
		addressBar_holder.setMinimumSize(addressBar.getMinimumSize());
		addressBar_holder.add(addressBar);
		addressBar_holder.setLayout(new GridLayout(1, 0));
		add(addressBar_holder, BorderLayout.NORTH);
	}

	//populate the FriendsBox.
	private void layoutFriendsBox() {
		friendsBox = new JPanel(new BorderLayout());
		friendsBox.setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
		
		friendsBox_Label = new JLabel();
		friendsBox_Label.setText("Your Friends:");
		friendsBox_Label.setBackground(Color.LIGHT_GRAY);

		friendsBox_List_Scroller = new JScrollPane();
				
		Friends.init();
		
		friendsBox_List = Friends.populate();
		friendsBox_List.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		
		friendsBox_List.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
			}
		});
		friendsBox_List.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton()!=1) return;
				Friends.sendTo(friendsBox_List.getSelectedValue());
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			});
						
		friendsBox_List_Scroller.getViewport().add(friendsBox_List);
		
		friendsBox_Search = new JTextField();
		friendsBox_Search.setText("Search for users.");
		friendsBox_Search.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Friends.sendTo(e.getActionCommand());
			}
		});
		
		friendsBox_Search.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
		});
		
		friendsBox.add(friendsBox_Label, BorderLayout.NORTH);
		friendsBox.add(friendsBox_List_Scroller, BorderLayout.CENTER);
		friendsBox.add(friendsBox_Search, BorderLayout.SOUTH);
		add(friendsBox, BorderLayout.EAST);

	}
	
	public static void friendsBoxReset(){
		friendsBox_List = Friends.populate();
		friendsBox_List.updateUI();
		friendsBox_List_Scroller.updateUI();
		friendsBox_List_Scroller.getViewport().updateUI();	}
	
	public static void writeStringToContentPane(String s){
		System.out.println("OK, Msg Here!!");
		pane.setText("<html><body><b><span style='size:17'>"+s+"</span></b></body></html>");
	}
	
	public static void showHTML(File file){
		//pane.removeAll();
		try {
			Document temp = pane.getDocument();
			temp.putProperty(Document.StreamDescriptionProperty, null);
			pane.setPage(file.toURI().toURL());
			Logger.log(file.toURI().toURL());
		} catch (IOException e) {
			Logger.log("Couldn't show in client...");
			e.printStackTrace();
		}
	//	pane.updateUI();
	//	contentScroller.updateUI();
		//contentScroller.getViewport().updateUI();
	}
	
}
