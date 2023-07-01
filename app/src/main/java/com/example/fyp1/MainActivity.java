package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.Crypto;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.ImageSteganography.d;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

import com.example.fyp1.ImageSteganography.Encode;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    boolean recaptcha = false;
    View.OnClickListener buttonListener;
    //private SQLiteAdapter mySQLiteAdapter;
    CheckBox checkBox;
    GoogleApiClient googleApiClient;
    String SiteKey = "6Le-4RMfAAAAAFEuF3cHQ4jzEIwOZtsU3y5sASY-";
    public static String ph_otp, currentuser=null, passinHex, secretkey, userauth;

    //add when login
    FirebaseAuth fAuth;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    int matched;
    //User users1;
    SHA encoding;
    String decodingname, n;

    //add when forget password
    AlertDialog.Builder reset_alert;
    LayoutInflater inflater;
    String r_ep;
    EditText reset_ep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // recaptcha
        checkBox = findViewById(R.id.checkBox1);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(MainActivity.this)
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

        //add secret key
        secretkey = "123";
        //Log.e("secretkey", initialpage.newsecret_key);

        //add login function
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        //users = new User();
        DAOUser user = new DAOUser();
        fAuth = FirebaseAuth.getInstance();
        Button l = findViewById(R.id.login);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name = (EditText)findViewById(R.id.l_name);
                n = name.getText().toString();
                EditText email = (EditText)findViewById(R.id.l_email);
                String e = email.getText().toString();
                EditText password = (EditText)findViewById(R.id.l_password);
                String p = password.getText().toString();
                EditText cpassword = (EditText)findViewById(R.id.l_cpassword);
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

                //initialize the database value listener first
                /*user.checkdetailsUser(e, "name", n);

                if (!user.isExist()){
                    name.setError("Name Do Not Match!");return;
                }

                if(matched==0) {
                matched = 0;*/
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot keyNode : snapshot.getChildren()) {
                            User users = keyNode.getValue(User.class);
                            Log.e(">>>>", "in loop" + users.getEmail());
                            if((ImageSteganography.decryptMessage(users.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(e))){
                                try {
                                    decodingname = ImageSteganography.decryptMessage(users.getName(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                                if ((decodingname.equals(n))) {
                                    //matched = true;
                                    Toast.makeText(MainActivity.this, "Loading..." + ImageSteganography.decryptMessage(users.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)), Toast.LENGTH_SHORT).show();
                                    currentuser = e;
                                    ph_otp = ImageSteganography.decryptMessage(users.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                fAuth.signInWithEmailAndPassword(e, p).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //pass email to make compare
                        if ((!decodingname.equals(n))) {
                            //matched = false;
                            name.setError("Name not Matched!");
                            return;
                        }

                        try {
                            passinHex = encoding.toHexString(encoding.getSHA(p));
                            userauth = ImageSteganography.encryptMessage(p,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                            //Log.e(">>>>>>>>>>>", passinHex);
                        }
                        // For specifying wrong message digest algorithms
                        catch (NoSuchAlgorithmException error) {
                            System.out.println(" Exception thrown for incorrect algorithm : " + error);
                        }

                        startActivity(new Intent(getApplicationContext(), otpforlogin.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        });

        //sign new account function
        Button s = findViewById(R.id.sign);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), signin.class));
                finish();
            }
        });

        //forget password function
        reset_alert = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        Button f = findViewById(R.id.forget);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = inflater.inflate(R.layout.reset_pop, null);

                reset_alert.setTitle("Reset Password?")
                        .setMessage("Enter Your Email To Get Password Reset Link.")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                reset_ep = (EditText) v.findViewById(R.id.e_tochange);
                                r_ep = reset_ep.getText().toString();
                                Log.d("asdddddddddddddddd",r_ep);

                                if(r_ep.isEmpty()){
                                    reset_ep.setError("Email Required!");
                                    return;
                                }

                                fAuth.sendPasswordResetEmail(r_ep).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(MainActivity.this, "Reset Password Email Sent", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", null)
                        .setView(v)
                        .create().show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /*public int setMatched() {
        matched = 1;
        return matched;
    }


    @Override
    protected void onStart(){
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), otp.class));
        }
    }*/

}

