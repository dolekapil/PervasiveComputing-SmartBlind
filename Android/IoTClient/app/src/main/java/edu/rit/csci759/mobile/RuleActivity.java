package edu.rit.csci759.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used for displaying the rules on the android application.
 */
public class RuleActivity extends Activity {
    Intent intent;
    public static String IPAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);
        intent = getIntent();
        IPAddress = intent.getStringExtra("IPAddress")+":"+intent.getStringExtra("port");
        for (int i = 0; i < Integer.parseInt(intent.getStringExtra("length")) ; i++) {
            RadioGroup radiogroup = (RadioGroup)findViewById(R.id.radiogroup);
            RadioButton radiobutton = new RadioButton(this);
            radiobutton.setId(i);
            String currentRule = intent.getStringExtra("" + (i + 1)).replaceAll("\\[(.*?)\\]", "").replaceAll("\\(","").replaceAll("\\)","").replaceAll("[[0-9].]", "");
            String formattedRule = currentRule.substring(2);
            radiobutton.setText(formattedRule);
            radiobutton.setTextColor(Color.WHITE);
            radiogroup.addView(radiobutton);
            radiogroup.check(0);
        }

        // On click event of add rule button.
        Button addRule = (Button) findViewById(R.id.addRule);
        addRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(RuleActivity.this, AddRuleActivity.class);
                RuleActivity.this.startActivityForResult(myIntent, 1);
            }
        });

        // On click event of edit rule button.
        Button editRule = (Button) (Button) findViewById(R.id.editRule);
        editRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup radiogroup = (RadioGroup)findViewById(R.id.radiogroup);
                int radioButtonID = radiogroup.getCheckedRadioButtonId();
                View radioButton = radiogroup.findViewById(radioButtonID);
                int index = radiogroup.indexOfChild(radioButton);
                RadioButton btn = (RadioButton) radiogroup.getChildAt(index);
                String selection = (String) btn.getText();
                Intent myIntent = new Intent(RuleActivity.this, EditRuleActivity.class);
                myIntent.putExtra("rule", selection);
                RuleActivity.this.startActivityForResult(myIntent, 2);
            }
        });

        // On click event of delete rule button.
        Button deleteRule = (Button) findViewById(R.id.deleteRule);
        deleteRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup radiogroup = (RadioGroup)findViewById(R.id.radiogroup);
                int radioButtonID = radiogroup.getCheckedRadioButtonId();
                View radioButton = radiogroup.findViewById(radioButtonID);
                int index = radiogroup.indexOfChild(radioButton);
                new SendJSONRuleRequest().execute("deleteRule", index+"");
                radiogroup.removeViewAt(index);
                radiogroup.check(((RadioButton)radiogroup.getChildAt(0)).getId());
            }
        });
    }

    // This method is used for receiving data from the successor activity.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            new SendJSONRuleRequest().execute("addRule", data.getStringExtra("temperature"), data.getStringExtra("ambient"), data.getStringExtra("Connector"), data.getStringExtra("blind"));
            RadioGroup radiogroup = (RadioGroup)findViewById(R.id.radiogroup);
            RadioButton radiobutton = new RadioButton(this);
            radiobutton.setId(radiogroup.getChildCount());
            radiobutton.setText("if temperature IS " + data.getStringExtra("temperature") +" "+ data.getStringExtra("Connector")+ " ambient IS " + data.getStringExtra("ambient")+" then blind IS "+data.getStringExtra("blind"));
            radiobutton.setTextColor(Color.WHITE);
            radiogroup.addView(radiobutton);
        } else if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            RadioGroup radiogroup = (RadioGroup)findViewById(R.id.radiogroup);
            int radioButtonID = radiogroup.getCheckedRadioButtonId();
            View radioButton = radiogroup.findViewById(radioButtonID);
            int index = radiogroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) radiogroup.getChildAt(index);
            btn.setText(data.getStringExtra("editedRule"));
            new SendJSONRuleRequest().execute("editRule", data.getStringExtra("editedRule"), "" + index);
        }
    }

    // This class is used for sending the JSON request for rules to PI.
    class SendJSONRuleRequest extends AsyncTask<String, String, String> {
        String response_txt;
        String request_method;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            request_method = params[0];
            Map<String, Object> map = new HashMap<>();
            if(request_method.equals("addRule")){
                map.put("temperature",params[1]);
                map.put("ambient",params[2]);
                map.put("Connector",params[3]);
                map.put("blind",params[4]);
            } else if(request_method.equals("deleteRule")){
                map.put("index",params[1]);
            } else if(request_method.equals("editRule")){
                if(params[1].contains("freezing")){
                    map.put("temperature", "freezing");
                } else if(params[1].contains("cold")){
                    map.put("temperature", "cold");
                } else if(params[1].contains("comfort")){
                    map.put("temperature", "comfort");
                } else if(params[1].contains("warm")){
                    map.put("temperature", "warm");
                }

                if(params[1].contains("dark")){
                    map.put("ambient", "dark");
                } else if(params[1].contains("dim")){
                    map.put("ambient", "dim");
                } else if(params[1].contains("bright")){
                    map.put("ambient", "bright");
                }

                if(params[1].contains("open")){
                    map.put("blind", "open");
                } else if(params[1].contains("half")){
                    map.put("blind", "half");
                } else if(params[1].contains("close")){
                    map.put("blind", "close");
                }

                if(params[1].contains("AND")){
                    map.put("connector", "AND");
                } else if(params[1].contains("OR")){
                    map.put("connector", "OR");
                }
                map.put("index", params[2]);
            }
            response_txt = JSONHandler.sendJSONRequest(IPAddress, request_method, map);
            return response_txt;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            //Log.d("debug", response_txt);
            try {
                if(request_method.equals("addRule")){
                    Toast.makeText(getApplicationContext(), response_txt+"! Rule added.", Toast.LENGTH_LONG).show();
                } else if(request_method.equals("deleteRule")){
                    Toast.makeText(getApplicationContext(), response_txt+"! Rule deleted.", Toast.LENGTH_LONG).show();
                } else if(request_method.equals("editRule")){
                    Toast.makeText(getApplicationContext(), response_txt+"! Rule edited.", Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e){
                Log.e("error", e.getMessage().toString());
            }
        }
    }
}
