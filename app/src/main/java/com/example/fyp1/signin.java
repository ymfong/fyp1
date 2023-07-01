package com.example.fyp1;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.security.NoSuchAlgorithmException;

public class signin extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    public boolean recaptcha = false;
    View.OnClickListener buttonListener;
    CheckBox checkBox;
    GoogleApiClient googleApiClient;
    String SiteKey = "6Le-4RMfAAAAAFEuF3cHQ4jzEIwOZtsU3y5sASY-";

    //add when firebase sign in
    FirebaseAuth fAuth;
    Button register, log_in;
    SHA encoding;
    String n, e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //recaptcha
        checkBox = findViewById(R.id.checkBox2);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(signin.this)
                .build();
        googleApiClient.connect();

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    SafetyNet.SafetyNetApi.verifyWithRecaptcha(googleApiClient, SiteKey)
                            .setResultCallback(new ResultCallback<SafetyNetApi.RecaptchaTokenResult>() {
                                @Override
                                public void onResult(@NonNull SafetyNetApi.RecaptchaTokenResult recaptchaTokenResult) {
                                    Status status = recaptchaTokenResult.getStatus();

                                    if((status != null) && status.isSuccess()){
                                        Toast.makeText(getBaseContext(), "Successfully Varified!", Toast.LENGTH_SHORT).show();
                                        recaptcha = true;
                                    }
                                }
                            });
                }
            }
        });

        //register function
        fAuth = FirebaseAuth.getInstance();

        register = findViewById(R.id.tosign);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText name = (EditText)findViewById(R.id.s_name);
                n = name.getText().toString();
                EditText email = (EditText)findViewById(R.id.s_email);
                e = email.getText().toString();
                EditText password = (EditText)findViewById(R.id.s_password);
                String p = password.getText().toString();
                EditText cpassword = (EditText)findViewById(R.id.s_cpassword);
                String cp = cpassword.getText().toString();

                if(n.isEmpty()){
                    name.setError("Name Is Required!");return;
                }

                if(e.isEmpty()){
                    email.setError("Email Is Required!");return;
                }

                if(p.isEmpty()){
                    password.setError("Password Is Required!");return;
                }

                if(cp.isEmpty()){
                    cpassword.setError("Please Enter Again The Password!");return;
                }

                if(!p.equals(cp)){
                    cpassword.setError("Passwords Do Not Match!");return;
                }

                Toast.makeText(signin.this, "Data Validated!", Toast.LENGTH_SHORT).show();

                fAuth.createUserWithEmailAndPassword(e,p).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        try
                        {
                            MainActivity.passinHex = encoding.toHexString(encoding.getSHA(p));
                            Log.e(">>>>>>>>>>>",MainActivity.passinHex);
                        }
                        // For specifying wrong message digest algorithms
                        catch ( NoSuchAlgorithmException e ) {
                            System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
                        }

                        //save into database
                        Intent intent = new Intent(getApplicationContext(), otp.class);
                        intent.putExtra("name", n);
                        intent.putExtra("email", e);

                        //jump to main menu page
                        MainActivity.userauth = ImageSteganography.encryptMessage(p,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(signin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //login
        log_in = findViewById(R.id.s_to_l);
        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}