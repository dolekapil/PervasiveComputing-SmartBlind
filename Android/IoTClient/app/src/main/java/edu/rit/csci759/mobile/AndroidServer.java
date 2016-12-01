package edu.rit.csci759.mobile;

import android.os.Handler;
import android.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class basically acts as android server which listens to the
 * update sent by the PI.
 *
 * Created by Kapil Dole on 08-10-2016.
 */
public class AndroidServer extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Dispatcher dispatcher;
    public static Handler myHandler;
    /**
     * Constructs a handler thread, squirreling away the socket. All the
     * interesting work is done in the run method.
     */
    public AndroidServer(Socket socket, android.os.Handler handler) {
        this.socket = socket;
        myHandler = handler;
        // Create a new JSON-RPC 2.0 request dispatcher
        this.dispatcher = new Dispatcher();

        // Register the "receiveUpdate" handlers with it
        dispatcher.register(new AndroidJsonHandler.UpdateHandler());
    }

    public void run(){
        try {
            Log.d("Connected", "Request received.");
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

            System.out.println(body.toString());
            JSONRPC2Request request = JSONRPC2Request.parse(body.toString());
            JSONRPC2Response resp = dispatcher.process(request, null);

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
