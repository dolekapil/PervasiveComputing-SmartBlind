package edu.rit.csci759.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * This main activity used for displaying temperature and ambient values and
 * blind position based on the fuzzy logic that we received from PI server.
 */
public class MainActivity extends Activity {
	Handler handler;
    Intent homeIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        homeIntent = getIntent();

        // Sending request for getting blind status.
		new SendJSONRequest().execute("getBlindStatus");

        // Running android server for getting notification from the PI.
		Thread server = new Thread(){
			public void run(){
				Log.e("Server", "The server is running.");
				try {
					ServerSocket listener = new ServerSocket(5555);
					while(true){
						Socket socket = listener.accept();
						new AndroidServer(socket, handler).start();
						Log.e("connection established", listener.toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		server.start();

        // Using handler for making UI updates after receiving update notification from PI.
		handler = new Handler(){
			public void handleMessage(Message msg){
				Map updatedValues = (HashMap)msg.obj;
				TextView temperature = (TextView) findViewById(R.id.temperature);
				TextView ambient = (TextView) findViewById(R.id.ambient);
				TextView blindStatus = (TextView) findViewById(R.id.blindStatus);
                TextView time = (TextView) findViewById(R.id.time);
				temperature.setText(updatedValues.get("TemperatureCelsius")+"C / "+updatedValues.get("TemperatureFarenheit")+"F ("+updatedValues.get("TemperatureStatus")+")");
				blindStatus.setText(""+updatedValues.get("BlindStatus"));
				ambient.setText(""+updatedValues.get("AmbientStatus"));
                time.setText(""+updatedValues.get("time"));
			}
		};

        // Displays the fuzzy rules from the PI server, after clicking button.
		Button viewRule = (Button) findViewById(R.id.viewRules);
		viewRule.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new SendJSONRequest().execute("viewRule");
			}
		});
	}

    // When activity is restarted, we are getting updated values.
	protected void onRestart() {
		super.onRestart();
		new SendJSONRequest().execute("getBlindStatus");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    // This class is used for sending JSON requests.
	class SendJSONRequest extends AsyncTask<String, String, String> {
		String response_txt;
		String request_method;

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... params) {
			request_method = params[0];
			response_txt = JSONHandler.sendJSONRequest(homeIntent.getStringExtra("IPAddress")+":"+homeIntent.getStringExtra("port"), request_method, null);
			return response_txt;
		}

		protected void onProgressUpdate(Integer... progress) {
	         //setProgressPercent(progress[0]);
	     }

	     protected void onPostExecute(String result) {
			 //Log.d("debug", response_txt);
			 try {
				 JSONObject json = new JSONObject(response_txt);
				 if(request_method.equals("getBlindStatus")){
					 TextView temperature = (TextView) findViewById(R.id.temperature);
					 temperature.setText(json.getString("TemperatureCelsius")+"C / "+json.getString("TemperatureFarenheit")+"F ("+json.getString("TemperatureStatus")+")");
					 TextView ambient = (TextView) findViewById(R.id.ambient);
					 ambient.setText(json.getString("AmbientStatus"));
					 TextView blindStatus = (TextView) findViewById(R.id.blindStatus);
					 blindStatus.setText(json.getString("BlindStatus"));
                     TextView time = (TextView) findViewById(R.id.time);
                     time.setText(json.getString("time"));
				 } else if(request_method.equals("viewRule")){
					 Intent myIntent = new Intent(MainActivity.this, RuleActivity.class);
					 for(int i=1; i<=json.length();i++){
						 myIntent.putExtra(""+i, json.getString(""+i)); //Optional parameters
					 }
					 myIntent.putExtra("length", ""+json.length());
                     myIntent.putExtra("IPAddress", homeIntent.getStringExtra("IPAddress"));
                     myIntent.putExtra("port", homeIntent.getStringExtra("port"));
					 MainActivity.this.startActivity(myIntent);
				 }
			 }
			 catch (Exception e){
				 Log.e("error", e.getMessage().toString());
			 }
		 }
	}
}
