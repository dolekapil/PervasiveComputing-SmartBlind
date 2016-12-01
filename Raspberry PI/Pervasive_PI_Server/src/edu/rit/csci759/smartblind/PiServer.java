package edu.rit.csci759.smartblind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import edu.rit.csci759.smartblind.JsonHandler;

/**
 * A PI server thread class. Handlers are spawned from the listening loop and
 * are responsible for a dealing with a single client and broadcasting its
 * messages.
 * 
 * @author Kapil Dole
 */
public class PiServer extends Thread {

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private Dispatcher dispatcher;

	/*
	 * Constructs a handler thread, squirreling away the socket. All the
	 * interesting work is done in the run method.
	 */
	public PiServer(Socket socket) {
		this.socket = socket;

		// Create a new JSON-RPC 2.0 request dispatcher
		this.dispatcher = new Dispatcher();

		// Register the "getRule", "addRule", "editRule" and "deleteRule"
		// handlers with it
		dispatcher.register(new JsonHandler.RuleHandler());
		// Register the "getBlindStatus" handlers with it
		dispatcher.register(new JsonHandler.BlindStatusHandler());
	}

	/*
	 * Services this thread's client by repeatedly requesting a screen name
	 * until a unique one has been submitted, then acknowledges the name and
	 * registers the output stream for the client in a global set, then
	 * repeatedly gets inputs and broadcasts them.
	 */
	public void run() {
		try {
			// Create character streams for the socket.
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			// read request
			String line;
			line = in.readLine();
			// System.out.println(line);
			StringBuilder raw = new StringBuilder();
			raw.append("" + line);
			boolean isPost = line.startsWith("POST");
			int contentLength = 0;
			while (!(line = in.readLine()).equals("")) {
				// System.out.println(line);
				raw.append('\n' + line);
				if (isPost) {
					final String contentHeader = "Content-Length: ";
					if (line.startsWith(contentHeader)) {
						contentLength = Integer.parseInt(line.substring(contentHeader.length()));
					}
				}
			}
			StringBuilder body = new StringBuilder();
			if (isPost) {
				int c = 0;
				for (int i = 0; i < contentLength; i++) {
					c = in.read();
					body.append((char) c);
				}
			}

			JSONRPC2Request request = JSONRPC2Request.parse(body.toString());
			JSONRPC2Response resp = dispatcher.process(request, null);
			System.out.println("Blind status sent.");

			// send response
			out.write("HTTP/1.1 200 OK\r\n");
			out.write("Content-Type: application/json\r\n");
			out.write("\r\n");
			out.write(resp.toJSONString());
			// do not in.close();
			out.flush();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println(e);
		} catch (JSONRPC2ParseException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}
