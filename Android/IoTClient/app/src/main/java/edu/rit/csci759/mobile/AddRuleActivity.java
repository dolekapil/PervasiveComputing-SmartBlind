package edu.rit.csci759.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * This class basically used for Adding the fuzzy rules.
 */
public class AddRuleActivity extends Activity {

    String[] temperatureSpinnerArray = new String[] {"freezing", "cold", "comfort", "warm", "hot"};
    String[] ambientSpinnerArray = new String[]{"dark", "dim", "bright"};
    String[] connectorArray = new String[]{"AND", "OR"};
    String[] blindSpinnerArray = new String[]{"open", "half", "close"};
    Spinner temperatureSpinner, ambientSpinner, connectorSpinner, blindSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rule);

        // Initializing the spinner values.
        temperatureSpinner = (Spinner) findViewById(R.id.temperatureSpinner);
        ArrayAdapter<String> temperatureAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, temperatureSpinnerArray);
        temperatureSpinner.setAdapter(temperatureAdapter);

        ambientSpinner = (Spinner) findViewById(R.id.editAmbientSpinner);
        ArrayAdapter<String> ambientAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ambientSpinnerArray);
        ambientSpinner.setAdapter(ambientAdapter);

        connectorSpinner = (Spinner) findViewById(R.id.editConnectorSpinner);
        ArrayAdapter<String> connectorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, connectorArray);
        connectorSpinner.setAdapter(connectorAdapter);

        blindSpinner = (Spinner) findViewById(R.id.blindSpinner);
        ArrayAdapter<String> blindAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, blindSpinnerArray);
        blindSpinner.setAdapter(blindAdapter);

        // When add button is pressed, we are sending request to the PI for updating the
        // rules in the FCL file.
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ruleIntent = new Intent();
                ruleIntent.putExtra("temperature", temperatureSpinner.getSelectedItem().toString());
                ruleIntent.putExtra("ambient", ambientSpinner.getSelectedItem().toString());
                ruleIntent.putExtra("Connector", connectorSpinner.getSelectedItem().toString());
                ruleIntent.putExtra("blind", blindSpinner.getSelectedItem().toString());
                setResult(RESULT_OK, ruleIntent);
                Toast.makeText(getApplicationContext(), "Adding rule.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
