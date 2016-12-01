package edu.rit.csci759.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * This is entry point of our application, which takes IP and
 * port number of PI to connect with it.
 */
public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // After clicking connect button we are sending connection request to the PI.
        Button connect = (Button) findViewById(R.id.connectButton);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText IP = (EditText) findViewById(R.id.IPEditText);
                EditText port = (EditText) findViewById(R.id.portEditText);
                Intent myIntent = new Intent(HomeActivity.this, MainActivity.class);
                myIntent.putExtra("IPAddress", IP.getText().toString());
                myIntent.putExtra("port", port.getText().toString());
                startActivity(myIntent);
            }
        });
    }
}
