package com.myroutes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private Button loGin;
    private EditText userName, passWord;
    RadioGroup radioGroup;
    RadioButton radioButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = findViewById(R.id.username);
        passWord = findViewById(R.id.password);
        loGin = findViewById(R.id.login);
        radioGroup = findViewById(R.id.radioUserType);

        passWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loGin.setEnabled(true);
            }
        });

        loGin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedId);

                if (!userName.getText().toString().isEmpty() && !passWord.getText().toString().isEmpty()
                    && radioButton.getText().equals("User"))
                {
                    //Toast.makeText(getApplicationContext(),"Redirecting.....", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MapsActivityClient.class);
                    intent.putExtra("userName", userName.getText().toString());
                    intent.putExtra("passWord", passWord.getText().toString());
                    startActivity(intent);
                    finish();
                } else if(!userName.getText().toString().isEmpty() && !passWord.getText().toString().isEmpty()
                        && radioButton.getText().equals("Driver")
                ) {
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    intent.putExtra("userName", userName.getText().toString());
                    intent.putExtra("passWord", passWord.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}