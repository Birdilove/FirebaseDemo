package com.example.firebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button login;
    TextView signup, resetPassword;
    EditText email_text, pass_text;
    String email, password;
    boolean connected = false;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        login = findViewById(R.id.loginButton);
        signup = findViewById(R.id.signupButtonText);
        email_text = findViewById(R.id.loginEmail);
        pass_text = findViewById(R.id.loginPassword);
        resetPassword = findViewById(R.id.forgotPassButtonText);

        resetPassword.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Intent forgotPasswordActivty = new Intent(this, ResetPassword.class);
            startActivity(forgotPasswordActivty);
        });
        signup.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Intent intent = new Intent(this, SignUp.class);
            startActivity(intent);
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (checkConnection()) {
                    Login();
                } else {
                    Toast.makeText(MainActivity.this, "Please connect to the Internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void Login(){
        email = email_text.getText().toString();
        password = pass_text.getText().toString();
        if(email.length() > 0 && password.length() > 0){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){

                    Intent main_acitvity = new Intent(this, Home.class);
                    startActivity(main_acitvity);
                }
                else{
                    Toast.makeText(this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(this,"Please enter email and password!",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else {
            connected = false;
        }
        return connected;
    }
}