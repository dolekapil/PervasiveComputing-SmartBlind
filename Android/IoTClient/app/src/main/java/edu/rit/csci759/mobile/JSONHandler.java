package edu.rit.csci759.mobile;

import android.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * This class is used for sending JSON requests.
 */
public class JSONHandler {

	public static Map map;
	public static String sendJSONRequest(String server_URL_text, String request_method, Map<String, Object> additionalParameters){
		// Creating a new session to a JSON-RPC 2.0 web service at a specified URL
		Log.d("Debug serverURL", server_URL_text);
		
		// The JSON-RPC 2.0 server URL
		URL serverURL = null;

		try {
			serverURL = new URL("http://"+server_URL_text);

		} catch (MalformedURLException e) {
		// handle exception...
		}

		// Create new JSON-RPC 2.0 client session
		JSONRPC2Session mySession = new JSONRPC2Session(serverURL);


		// Once the client session object is created, you can use to send a series
		// of JSON-RPC 2.0 requests and notifications to it.
		// Construct new request
		int requestID = 0;
		JSONRPC2Request request;
		if(additionalParameters == null){
			request = new JSONRPC2Request(request_method, requestID);
		} else{
			request = new JSONRPC2Request(request_method, additionalParameters, requestID);
		}

		// Send request
		JSONRPC2Response response = null;

		try {
			response = mySession.send(request);
			Log.d("test", response.toString());
		} catch (Exception e) {

		Log.e("error", e.getMessage().toString());
		// handle exception...
		}

		// Print response result / error
		if (response.indicatesSuccess())
			Log.d("debug", response.getResult().toString());
		else
			Log.e("error", response.getError().getMessage().toString());

		return response.getResult().toString();
	}
}
