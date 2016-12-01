package edu.rit.csci759.smartblind;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is main class for running PI server and client.
 * 
 * @author Kapil Dole
 *
 */
public class SmartBlindMain {
	/*
	 * The port that the server listens on.
	 */
	private static final int PORT = 8080;

	public static void main(String[] args) {
		System.out.println("The server is running.");
		try {
			@SuppressWarnings("resource")
			ServerSocket listener = new ServerSocket(PORT);
			GpioReader.initialize();
			boolean executeClientOnce = true;
			/*
			 * Listening for request from android and also starts the client thread for
			 * sending notification. 
			 */
			while (true) {
				Socket client = listener.accept();
				System.out.println("Connected to " + client.getInetAddress().getHostAddress());
				System.out.println(client.getInetAddress().getHostAddress() + " requested blind status.");
				new PiServer(client).start();
				if (executeClientOnce) {
					new PiClient(client.getInetAddress().getHostAddress()).start();
					executeClientOnce = false;
				}
			}
		} catch (Exception e) {
		}
	}
}
