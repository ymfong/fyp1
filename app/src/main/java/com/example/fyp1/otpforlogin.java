package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.SecurityFeatures.AdditionalSecurity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class otpforlogin extends AppCompatActivity {


    TextView t1, t2, timer2;
    EditText phoneNumber, OTP1;
    Button sendOTP,verifyOTP, resendOTP,back;
    String userPhoneNumber, verificationId;
    FirebaseAuth fAuth;
    PhoneAuthProvider.ForceResendingToken token;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    //check phone and email
    DAOUser user;
    String email,phonefromdatabase,cc,pn;
    public static String currentUser;
    boolean matched;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    User users;

    //timer
    CountDownTimer countDownTimer;
    long timeLefInMillisecounds = 60000;     //1 min

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpforlogin);Log.e(">","directly to otp");

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fAuth = FirebaseAuth.getInstance();

        phoneNumber = findViewById(R.id.phoneNumber);
        t1 = findViewById(R.id.textView18);
        sendOTP = findViewById(R.id.sendotp);
        sendOTP.setEnabled(true);

        timer2 = findViewById(R.id.timer2);
        OTP1 = findViewById(R.id.OTP1);
        t2 = findViewById(R.id.textView13);
        verifyOTP = findViewById(R.id.OTPverify);
        resendOTP = findViewById(R.id.resend);
        back = findViewById(R.id.back);

        fAuth = FirebaseAuth.getInstance();

        user = new DAOUser();

        email = MainActivity.currentuser;
        phonefromdatabase = MainActivity.ph_otp;

        //Log.d("asdddddddddddddddd",email+phonefromdatabase);

        //add when check phone number
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        user = new DAOUser();

        //pre-set the phone number get from database
        //countryCode.setText(phonefromdatabase.substring(1,phonefromdatabase.length()-10));
        //phoneNumber.setText(phonefromdatabase.substring(phonefromdatabase.length()-10,phonefromdatabase.length()));
        phoneNumber.setText(phonefromdatabase.substring(1,phonefromdatabase.length()));
        userPhoneNumber = phonefromdatabase;

        sendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((phoneNumber.getText().toString().isEmpty())){
                    phoneNumber.setError("Phone Number is Required");
                    return;
                }

                userPhoneNumber = "+"+phoneNumber.getText().toString();

                //make sure email and phone is matching

                /*user.checkdetailsUser(email, "phone", userPhoneNumber);

                if(!user.isExist()){
                    Toast.makeText(otpforlogin.this, email+userPhoneNumber, Toast.LENGTH_SHORT).show();
                    phoneNumber.setError("Phone number Do Not Match!");return;
                }*/

                matched = false;
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot keyNode : snapshot.getChildren()){
                            users = keyNode.getValue(User.class);
                            if((ImageSteganography.decryptMessage(users.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(email)==true)
                                    && (ImageSteganography.decryptMessage(users.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(userPhoneNumber)!=true)){
                                Log.d("asfa", "sdfa");
                                matched = false;
                                phoneNumber.setError("Phone Number not Matched!");
                                return;
                            }else{
                                matched = true;

                                verifyPhoneNumber(userPhoneNumber);
                                Toast.makeText(otpforlogin.this, userPhoneNumber, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyPhoneNumber(userPhoneNumber);
                resendOTP.setEnabled(false);

                resettimer();
            }
        });
        updateTimer();

        //back to edit phone number to receive OTP
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*finish();
                startActivity(new Intent(getApplicationContext(), otpforlogin.class));
                finish();*/
                startActivity(new Intent(getIntent()));
                finish();
            }
        });

        verifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the otp

                if(OTP1.getText().toString().isEmpty()){
                    OTP1.setError("Enter OTP first!");
                    return;
                }
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, OTP1.getText().toString());
                authenticateUser(credential);
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                authenticateUser(phoneAuthCredential);
                resendOTP.setVisibility(View.GONE);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(otpforlogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;
                token = forceResendingToken;

                phoneNumber.setVisibility(View.GONE);
                t1.setVisibility(View.GONE);
                sendOTP.setVisibility(View.GONE);

                //timer
                timer2.setVisibility(View.VISIBLE);
                starttimer();

                OTP1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
                verifyOTP.setVisibility(View.VISIBLE);
                resendOTP.setVisibility(View.VISIBLE);
                resendOTP.setEnabled(false);
                back.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                resendOTP.setEnabled(true);
            }
        };
    }

    private void verifyPhoneNumber(String phoneNum) {
        //send OTP
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(fAuth)
                .setActivity(this)
                .setPhoneNumber(phoneNum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void authenticateUser(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(otpforlogin.this, "Success", Toast.LENGTH_SHORT).show();

                //jump to main page
                MainActivity.currentuser = email;

                fAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(otpforlogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                //ask to verify the email
                startActivity(new Intent(getApplicationContext(), AdditionalSecurity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(otpforlogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*@Override
    public void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), mainmenu.class));
            finish();
        }
    }*/

    //timer
    public void starttimer(){
        countDownTimer = new CountDownTimer(timeLefInMillisecounds, 1000) {
            @Override
            public void onTick(long l) {
                timeLefInMillisecounds = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timer2.setText("0:00");
            }
        }.start();
    }

    public void resettimer(){
        timeLefInMillisecounds = 60000;
        updateTimer();
    }

    public void updateTimer(){
        int minute = (int) timeLefInMillisecounds / 60000;
        int second = (int) timeLefInMillisecounds % 60000 / 1000;

        String timeleft;
        timeleft = "" + minute;
        timeleft += ":";

        if(second < 10 ){
            timeleft += "0";
        }

        timeleft += second;

        timer2.setText(timeleft);
    }
}