package edu.rit.csci759.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This class is basically used for editing the fuzzy rules.
 */
public class EditRuleActivity extends Activity {
    EditText ruleEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_rule);
        Intent intent = getIntent();
        String rule = intent.getStringExtra("rule");
        ruleEditText = (EditText) findViewById(R.id.ruleEditText);
        ruleEditText.setText(rule);

        // After clicking edit button, we are sending request to the PI
        // server for updating the rule in FCL file.
        Button editButton = (Button) findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ruleIntent = new Intent();
                ruleIntent.putExtra("editedRule", ruleEditText.getText().toString());
                setResult(RESULT_OK, ruleIntent);
                Toast.makeText(getApplicationContext(), "Editing rule.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
