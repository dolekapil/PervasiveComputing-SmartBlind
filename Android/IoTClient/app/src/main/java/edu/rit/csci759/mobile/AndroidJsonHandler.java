package edu.rit.csci759.mobile;

import android.os.Message;
import android.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

import java.util.Map;

/**
 * This class is used for handling the Json requests from PI.
 *
 * Created by Kapil Dole on 09-10-2016.
 */
public class AndroidJsonHandler {
    // Implements a handler for an "temperature updates" JSON-RPC method
    public static class UpdateHandler implements RequestHandler {

        // Reports the method names of the handled requests
        public String[] handledRequests() {
            return new String[] { "receiveUpdate" };
        }

        // Processes the request after receiving the update notification.
        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
            if (req.getMethod().equals("receiveUpdate")) {
                Log.e("Received", "Update received");
                Map updatedValues = req.getNamedParams();
                Message message = Message.obtain();
                message.obj = updatedValues;
                AndroidServer.myHandler.sendMessage(message);
                return new JSONRPC2Response("Updated values on device!", req.getID());
            } else {
                // Method name not supported
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }
}
