/**
 * Copyright (C) 2011 Snowball Effect
 * 
 * Original source code:
 * http://wiki.mikrotik.com/wiki/API_in_Java
 * 
 * @author Janisk
 * 
 * Adapted for Android by Snowball
 * 
 * See additional class Network.java
 * 
 */

package com.mikrodroid.router.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.mikrodroid.router.ConfigCollection;
import com.mikrodroid.router.ConfigItem;
import com.mikrodroid.router.ConfigList;

import android.util.Log;

/**
 * MikrotikAPI extends a Thread
 * @author eugene
 *
 */
public class MikrotikApi extends Thread {

	private Socket sock = null;
	private DataOutputStream out = null;
	private DataInputStream in = null;
	private String ipAddress;
	private int ipPort;
	private boolean connected = false;
	private String message = "Not connected";
	private ReadCommand readCommand = null;
	private WriteCommand writeCommand = null;
	private Thread listener = null;
	LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>(40);
	
	public String routerName;

	private static String TAG = "MikrotikApi";

	/**
	 * Constructor of the connection class
	 * 
	 * @param ipAddress - IP address of the router you want to connect to
	 * @param ipPort - port used for connection, ROS default is 8728
	 */
	public MikrotikApi(String ipAddress, int ipPort) {
		this.ipAddress = ipAddress;
		this.ipPort = ipPort;
		// Set the name of this thread
		this.setName("MikrotikApiConn");
		Log.v(TAG, "In constructor of " + TAG);
		Log.d(TAG, "Passed ipAddress: " +ipAddress);
	}

	/**
	 * State of the connection
	 * 
	 * @return If a connection is established to the router it returns true
	 */
	public boolean isConnected() {
		return connected;
	}

	public void disconnect() throws IOException {
		listener.interrupt();
		sock.close();
	}

	/**
	 * Start a new listener thread
	 */
	private void listen() {
		if (this.isConnected()) {
			if (readCommand == null) {
				readCommand = new ReadCommand(in, queue);
			}
			listener = new Thread(readCommand);
			listener.setDaemon(true);
			listener.setName("listener");
			listener.start();
		}
	}

	/**
	 * Get IP address of the connection. Reads data from socket created.
	 * 
	 * @return InetAddress
	 */
	public InetAddress getIpAddress() {
		return sock == null ? null : sock.getInetAddress();
	}

	/**
	 * Returns IP address that socket is associated with
	 * 
	 * @return InetAddress
	 */
	public InetAddress getLocalIpAddress() {
		return sock == null ? null : sock.getLocalAddress();
	}

	/**
	 * Socket remote port number
	 * 
	 * @return
	 */
	public int getPort() {
		return sock == null ? null : sock.getPort();
	}

	/**
	 * Return local port used by socket
	 * 
	 * @return
	 */
	public int getLocalPort() {
		return sock == null ? null : sock.getLocalPort();
	}

	/**
	 * Returns status message set up by class
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Retrieve a parameter param from a line of output from the MikroTik API
	 * @param line
	 * @param param
	 * @return
	 */
	public String getParam(String line, String param) {		
		int start = line.indexOf("\n=" + param + "=");
		if (start != -1 ) {
			line = line.substring(start + param.length() + 3, line.length());
			int end = line.indexOf("\n");
			// If there are multiple parameters the \n works
			if (end != -1) {
				return line.substring(0, end);
			// else if there is no \n just return the parameter (e.g. /system/identity/print)
			} else if (end == -1 ) {
				return line;			
			}		
		}
		return null;			
	}

	/**
	 * Sets and executes command (sends it to RouterOS host connected)
	 * 
	 * @param s - command will be sent to RouterOS for example
	 *            "/ip/address/print\n=follow="
	 * @return
	 */
	public String sendCommand(String s) {
		s = s.toLowerCase();
		Log.v(TAG, "RouterOS command: " + s);
		return writeCommand.setCommand(s).runCommand();
	}

	/**
	 * Executes already set command
	 * 
	 * @return returns status of the command sent
	 */
	public String runCommand() {
		return writeCommand.runCommand();
	}

	/**
	 * Tries to fetch data that is replied to commands sent, it will wait till it can return something
	 * 
	 * @return Returns String data sent by RouterOS
	 * @throws java.lang.InterruptedException
	 */
	public String getData() throws InterruptedException {
		String s = (String) queue.take();
		return s;
	}

	/**
	 * Returns command that is set at this moment. And will be executed if
	 * runCommand is executed.
	 * 
	 * @return
	 */
	public String getCommand() {
		return writeCommand.getCommand();
	}

	/**
	 * Set up method that will log you in
	 * 
	 * @param name - user name of the user on the router
	 * @param password - password for the user
	 * @return String Result of login method
	 */
	public String login(String name, String password) {
		this.sendCommand("/login");
		String s = "a";
		try {
			s = this.getData();
			Log.v(TAG, "RouterOS result #1: " + s);
		} catch (InterruptedException ex) {
			Log.e(MikrotikApi.class.getName(), ex.getMessage(), ex);			
			return "failed read #1";
		}
		if (!s.contains("!trap") && s.length() > 4) {
			String[] tmp = s.trim().split("\n");
			if (tmp.length > 1) {
				tmp = tmp[1].split("=ret=");
				s = "";
				String transition = tmp[tmp.length - 1];
				String chal = "";
				chal = Hasher.hexStrToStr("00") + password
						+ Hasher.hexStrToStr(transition);
				chal = Hasher.hashMD5(chal);
				String m = "/login\n=name=" + name + "\n=response=00" + chal;
				s = this.sendCommand(m);
				try {
					s = this.getData();
					Log.v(TAG, "RouterOS result #2: " + s);
				} catch (InterruptedException ex) {
					Log.e(MikrotikApi.class.getName(), ex.getMessage(), ex);
					return "failed read #2";
				}
				if (s.contains("!done")) {
					if (!s.contains("!trap")) {
						return "Login successful";
					}
				}
			}
		}
		return "Login failed";		
	}

	/**
	 *  
	 */
	@Override
	public void run() {
		try {
			String socketResult = openSocket(ipAddress, ipPort);
			if (socketResult == "Success") {
				sock = new Socket(ipAddress, ipPort);
				in = new DataInputStream(sock.getInputStream());
				out = new DataOutputStream(sock.getOutputStream());
				connected = true;
				readCommand = new ReadCommand(in, queue);
				writeCommand = new WriteCommand(out);
				this.listen();
				message = "Connected";
			} else {
				message = socketResult;		
				Log.e(TAG, "Socket error to " + ipAddress + ": " +socketResult);
			}
		} catch (Exception e) {
			connected = false;
			message = e.getMessage();
			Log.e(TAG, "Exception in run() thread: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Check if a TCP/IP socket on port is open on host
	 * 
	 * @param host
	 * @param port
	 * @return "Success" or exception message
	 */	
	private String openSocket(String host, int port) {	
		InetAddress inet;
		try {
			inet = InetAddress.getByName(host);			
		} catch (UnknownHostException e) {
			return e.getMessage();
		}
		Socket sock = new Socket();
		try {
			sock.connect(new InetSocketAddress(inet, port), 3000);
		} catch (IOException e) {
			return e.getMessage();
		}
		return "Success";
	}
	
	/**
	 * Get file using FTP from MikroTik router and store it on the local Android SDCard storage.
	 * 
	 * Test by using 'export file=commands' from '/' on the router
	 * 
	 * TODO: Make routine more generic by making IP, username and password parameters
	 * 
	 * @param remoteFile
	 */
	public static boolean getExportFile(String remoteFile) {					
		
		File f = new File("/mnt/sdcard/"	+ remoteFile);
		
		if (checkBootMenuExists(remoteFile) == false) { // If the file is not there create it			
			try {				
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		// TODO Better debugging for file not found etc.
		FileOutputStream localFile = null;
		try {
			localFile = new FileOutputStream(f.getAbsoluteFile());
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		
		Log.d(TAG, "FTPing to router to get menu system");
		FTPClient conn = new FTPClient();
		
		try {
			conn.connect("192.168.0.2");
			if (conn.login("eugene", "moresecure69")) {
				Log.d(TAG, "Retrieving file from FTP...");
				conn.enterLocalPassiveMode(); // Android needs this line
				conn.setFileTransferMode(FTP.BINARY_FILE_TYPE);
				conn.retrieveFile(remoteFile, localFile);
				conn.logout();
				conn.disconnect();
				return true;
			} else {
				Log.e(TAG, "Unable to FTP to router");		
				// Delete the file
				f.delete();
			}
		} catch (Exception e3) {
			e3.printStackTrace();
		}
		return false;

	}
	
	public static boolean checkBootMenuExists(String remoteFile) {			
		File f = new File("/mnt/sdcard/"	+ remoteFile);		
		return (f.exists());		
	}
	
	/**
	 * Get a locally store file called 'fileName' and upload it using FTP to the a MikroTik router
	 * 
	 *  TODO: Make routine more generic by making IP, username and password parameters
	 * 
	 * @param fileName
	 */
	public static void upload(String fileName) {
		FTPClient conn = new FTPClient();
		FileInputStream fis = null;
		try {
			conn.connect("192.168.0.2");
			if (conn.login("eugene", "moresecure69")) {
				conn.enterLocalPassiveMode();
				conn.setFileTransferMode(FTP.BINARY_FILE_TYPE);				
				String filename = "/mnt/sdcard/" + fileName;
				fis = new FileInputStream(filename);
				conn.storeFile(fileName + ".upload", fis);
				conn.logout();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				conn.logout();
				conn.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Called after successful login
	 */
	public void setupRouter() {
		// Set routerName
		// /system resource print
		// Determine MT version 4.12
		// Determine RB version RB450
		// Determine Uptime
		// Determine CPU Load
		// /system RouterBOARD print
		// RouterBOARD upgradable  
	}
	
	/**
	 * Search search string for param and return it
	 * @param search
	 * @param param
	 * @return
	 */
	public String getRouterParam(String search, String param) {
		int param_start = search.indexOf("\n=" + param + "=");
		int value_start = param_start + 3 + param.length();
		String result = search.substring(value_start, search.length());
		return result;
	}
	
	/**
	 * TODO: Buggy routine does not return !done in log and subsequently crashes. To simulate, add new router and go to system identity
	 * Could be that you need a while loop, not sure!
	 * @return
	 */
	public String setRouterName() {
		routerName = null;
		this.sendCommand("/system/identity/print");
		String s;
		try {
			s = this.getData();
			Log.d("setRouterName", "RouterOS reply: " + s);
			routerName = getRouterParam(s, "name");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}			
		return routerName;
	}
	
	public String getRouterName() {
		return routerName;
	}
	
	public static ConfigCollection getCollection(String data) {

		ConfigList list = new ConfigList();
		ConfigCollection collection = null;		
		ConfigItem item = null;
		
		String paramName, paramValue;
		int start_of_value;

		String lines[] = data.split("\\n");		
		for (String line : lines) {
			
			// Check if an error occurred
			if (line.equals("!trap")) {
				return null;
			}			
			
			// Start of result
			if (line.equals("!re") || line.equals("!trap")) {
				collection = new ConfigCollection();
			}
			
			// If there are two or more = signs then this line should contain a parameter
			if (StringUtils.countMatches(line, "=") >= 2 && line.charAt(0) == '=') {
				start_of_value = line.indexOf('=', 1);
				paramName = line.substring(1, start_of_value);
				paramValue = line.substring(start_of_value + 1, line.length());
				item = new ConfigItem();				
				item.setName(paramName);
				item.setValue(paramValue);
				collection.addItem(item);
				item = null;
			}
			
			// End of result
			if (line.contains(".tag=") == true) {
				list.addItem(collection);
				//this.addItem(collection);
				collection = null;
			}
			
			// End of configuration request
			if (line.equals("!done") == true) {
				//return this;
				//return list.getItem(0);
				return collection;
			}			
			
		}
		return null;
	}
	
}