package com.example.hp.sjtuphysics;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private Button SignIn;
    private EditText Name;
    private EditText Password;
    private CheckBox AutoSignIn;

    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefManager = new PrefManager(this);

        initViews();

        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Name.getText().toString().isEmpty() && !Password.getText().toString().isEmpty()) {
                    if(AutoSignIn.isChecked()) {
                        prefManager.setAutoLoginin(true);
                    }
                    prefManager.setUserName(Name.getText().toString());
                    prefManager.setUserPassword(Password.getText().toString());
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra("name", Name.getText().toString());
                    intent.putExtra("password", Password.getText().toString());
                    startActivity(intent);
                    Login.this.finish();
                }
            }
        });
    }

    public void initViews(){
        SignIn = (Button) findViewById(R.id.SignIn);
        Name = (EditText) findViewById(R.id.Name);
        Password = (EditText) findViewById(R.id.Password);
        AutoSignIn = (CheckBox) findViewById(R.id.AutoSignIn);
        AutoSignIn.setChecked(prefManager.isAutoLogin());
        //Name.setText("517021910320");
        //Password.setText("517021910320");
    }
}
