package com.example.firebasedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    private FirebaseAuth auth;
    String TAG;
    EditText email;
    Button resetPassword;
    boolean connected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.resetPassEmail);
        resetPassword = findViewById(R.id.resetButton);
        resetPassword.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if(checkConnection()){
                ResetPassword();
            }
            else {
                Toast.makeText(this, "Please connect to the Internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void ResetPassword() {
        String emailAddress = email.getText().toString();
        if(emailAddress.length()>0){
            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(this, "Email sent!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(this, "Please enter valid email!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            Toast.makeText(this, "Please enter valid email!", Toast.LENGTH_SHORT).show();
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