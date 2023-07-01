package com.example.fyp1;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp1.ImageSteganography.Encode;
import com.example.fyp1.ImageSteganography.ImageSteganography;
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

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class otp extends AppCompatActivity {
    TextView t1, t2, timer;
    EditText countryCode, phoneNumber, OTP1;
    Button sendOTP,verifyOTP, resendOTP, back;
    String userPhoneNumber, verificationId;
    FirebaseAuth fAuth;
    PhoneAuthProvider.ForceResendingToken token;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    String name, email,pass;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    User user;
    boolean used;

    CountDownTimer countDownTimer;
    long timeLefInMillisecounds = 60000;     //1 min

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fAuth = FirebaseAuth.getInstance();

        countryCode = findViewById(R.id.countryCode);
        phoneNumber = findViewById(R.id.phoneNumber);
        t1 = findViewById(R.id.textView18);
        sendOTP = findViewById(R.id.sendotp);
        sendOTP.setEnabled(true);

        timer = findViewById(R.id.countdown_text);
        OTP1 = findViewById(R.id.OTP1);
        t2 = findViewById(R.id.textView13);
        verifyOTP = findViewById(R.id.OTPverify);
        resendOTP = findViewById(R.id.resend);
        back = findViewById(R.id.back);

        //add secret key
        MainActivity.secretkey = "123";

        fAuth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");

        sendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((countryCode.getText().toString().isEmpty())){
                    countryCode.setError("Required");
                    return;
                }
                if((phoneNumber.getText().toString().isEmpty())){
                    phoneNumber.setError("Phone Number is Required");
                    return;
                }

                userPhoneNumber = "+"+countryCode.getText().toString()+phoneNumber.getText().toString();

                used = true;
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot keyNode : snapshot.getChildren()){
                            if(snapshot.getChildrenCount() > 0){
                                user = keyNode.getValue(User.class);
                                try {
                                    if(ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(userPhoneNumber)){
                                        used = true;
                                        phoneNumber.setError("This phone number had been used!");
                                        return;
                                    }else{
                                        used = false;
                                    }
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }else{
                                used = false;
                                break;
                            }
                        }
                        verifyPhoneNumber(userPhoneNumber);
                        Toast.makeText(otp.this, userPhoneNumber, Toast.LENGTH_SHORT).show();
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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                Toast.makeText(otp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;
                token = forceResendingToken;

                countryCode.setVisibility(View.GONE);
                phoneNumber.setVisibility(View.GONE);
                t1.setVisibility(View.GONE);
                sendOTP.setVisibility(View.GONE);

                //timer
                timer.setVisibility(View.VISIBLE);
                starttimer();

                OTP1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
                verifyOTP.setVisibility(View.VISIBLE);
                resendOTP.setVisibility(View.VISIBLE);
                resendOTP.setEnabled(false);
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
                .setTimeout(60L,TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void authenticateUser(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(otp.this, "Success", Toast.LENGTH_SHORT).show();

                //save to database
                Intent intent = getIntent();
                name = intent.getStringExtra("name");
                email = intent.getStringExtra("email");

                User newuser = new User();
                try {
                    newuser.setName(ImageSteganography.encryptMessage(name,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    newuser.setPhone(ImageSteganography.encryptMessage(userPhoneNumber,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    newuser.setEmail(ImageSteganography.encryptMessage(email,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    newuser.setBalance(ImageSteganography.encryptMessage("0",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    MainActivity.ph_otp = userPhoneNumber;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                new DAOUser().addUser(newuser, new DAOUser.DataStatus() {
                    @Override
                    public void DataIsLoaded(List<User> users, List<String> keys) {
                        Toast.makeText(otp.this, "The Account is Recorded!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void DataIsInserted() {

                    }

                    @Override
                    public void DataIsUpdated() {

                    }

                    @Override
                    public void DataIsDeleted() {

                    }
                });


                //jump to main page
                MainActivity.currentuser = email;
                fAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(otp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                //ask to verify the email
                startActivity(new Intent(getApplicationContext(), profilepage.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(otp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                timer.setText("0:00");
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

        timer.setText(timeleft);
    }
}
