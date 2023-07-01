package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.Crypto;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.BaseOnChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

public class transfer_page extends AppCompatActivity {

    EditText country, phone, amount, description, transfer_p;
    String phone_no, p_toHex, current_p;
    Button proceed, back, go_transfer, goback;
    LinearLayout transfer1, transfer2, transfer3;

    Date currentTime;

    //get current user phone
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    String currentuserphone, am, d, t, cu, ph;

    //checkbalance
    String balance;

    //check valid transfer account
    int valid;

    //add when ask password
    SHA encoding;

    //add when update balance
    String key;
    int count1, count2;
    DecimalFormat decimalFormat;
    //TextView s_t, success1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_page);Log.e(">","t directly to otp");

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //pass button reference
        proceed = findViewById(R.id.proceed);
        back = findViewById(R.id.back2);
        go_transfer = findViewById(R.id.go_transfer);
        goback = findViewById(R.id.goback);
        transfer1 = findViewById(R.id.transfer1);
        transfer2 = findViewById(R.id.transfer2);

        //get current user phone no
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    User user = keyNode.getValue(User.class);
                    if(ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser)){
                        currentuserphone = ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        //Log.d(">>>>>>>>>>>>>>>>hihi",currentuserphone);
                        //balance = user.getBalance();
                        try {
                            balance = ImageSteganography.decryptMessage(user.getBalance(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //button onclick
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valid=0;
                country = findViewById(R.id.c_code);
                String c = country.getText().toString();
                phone = findViewById(R.id.p_no);
                String p = phone.getText().toString();
                amount = findViewById(R.id.money);
                String a = amount.getText().toString();
                description = findViewById(R.id.description);
                String de = description.getText().toString();

                if(c.isEmpty()){
                    country.setError("Country Code Is Required!");return;
                }

                if(p.isEmpty()){
                    phone.setError("Phone Number Is Required!");return;
                }

                if(a.isEmpty()){
                    amount.setError("Amount of Money Is Required!");return;
                }

                if(Float.parseFloat(balance)<Float.parseFloat(a)){
                    amount.setError("You balance is not enough! Please top up!");return;
                }

                if(Float.parseFloat(a)<=0){
                    amount.setError("Money to be transfer cannot less than RM0.00!");return;
                }

                phone_no = "+"+c+p;
                if(phone_no.equals(currentuserphone)){
                    phone.setError("Cannot transfer money to yourself!");return;
                }

                currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mmLss a");
                //String dateTime = simpleDateFormat.format(currentTime);

                //find valid users to receive money
                db = FirebaseDatabase.getInstance();
                databaseReference = db.getReference("user");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                            User user = keyNode.getValue(User.class);
                            if(ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(phone_no)){
                                valid=1;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(valid==1){
                            transfer1.setVisibility(View.GONE);
                            transfer2.setVisibility(View.VISIBLE);
                        }else{
                            phone.setError("This phone number is not registered before! Please try again!");
                            return;
                        }
                    }
                });

                //ask password
                count1=0; count2=0;



                current_p = MainActivity.passinHex;
                go_transfer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transfer_p = findViewById(R.id.transfer_p);
                        String pass = transfer_p.getText().toString();

                        if(pass.isEmpty()) {
                            transfer_p.setError("Please Enter The Password!");return;
                        }

                        try
                        {
                            p_toHex = encoding.toHexString(encoding.getSHA(pass));
                            //Log.e(">>>>>>>>>>>",p_toHex);
                        }
                        // For specifying wrong message digest algorithms
                        catch ( NoSuchAlgorithmException e ) {
                            System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
                        }

                        if(!p_toHex.equals(current_p)) {
                            transfer_p.setError("Passwords Do Not Match!"+p_toHex+"+"+current_p);return;
                        }

                        //valid account
                        if(valid==1){
                            try {
                                cu = ImageSteganography.encryptMessage(currentuserphone,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                ph = ImageSteganography.encryptMessage(phone_no,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                am = ImageSteganography.encryptMessage(a,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                t = ImageSteganography.encryptMessage(currentTime.toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                d = ImageSteganography.encryptMessage(de,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                            Transaction transaction = new Transaction();
                            transaction.setSender_no(cu);
                            transaction.setReceiver_no(ph);
                            transaction.setAmount(am);
                            transaction.setDate(t);
                            transaction.setDescription(d);

                            new DAOtransaction().addTransaction(transaction, new DAOtransaction.DataStatus() {
                                @Override
                                public void DataIsLoaded(List<Transaction> t, List<String> keys) {
                                    Toast.makeText(transfer_page.this, "The Transaction is Successfully Done!", Toast.LENGTH_SHORT).show();
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

                            //update balance
                            decimalFormat = new DecimalFormat("0.00");
                            databaseReference = db.getReference("user");
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                                        key = keyNode.getKey();
                                        User u = keyNode.getValue(User.class);
                                        if(count1!=0 && count2!=0){
                                            break;
                                        }

                                        //update sender first
                                        if(ImageSteganography.decryptMessage(u.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                                            try {
                                                String newbalance = String.valueOf(decimalFormat.format(Float.parseFloat(balance)- Float.parseFloat(a)));
                                                u.setBalance(ImageSteganography.encryptMessage(newbalance,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                                Log.e("sender",newbalance);
                                            } catch (Exception exception) {
                                                exception.printStackTrace();
                                            }
                                            new DAOUser().updateUser(key, u, new DAOUser.DataStatus() {
                                                @Override
                                                public void DataIsLoaded(List<User> users, List<String> keys) {

                                                }

                                                @Override
                                                public void DataIsInserted() {

                                                }

                                                @Override
                                                public void DataIsUpdated() {
                                                    Log.e(">>>>>>>>>>>>>>>>>","balance of sender update done"+String.valueOf(Float.parseFloat(balance)));
                                                }

                                                @Override
                                                public void DataIsDeleted() {

                                                }
                                            });
                                            count1++;
                                        }

                                        //update receiver
                                        if(ImageSteganography.decryptMessage(u.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(phone_no)){
                                            try {
                                                String balance2 = ImageSteganography.decryptMessage(u.getBalance(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                                String newbalance2 = String.valueOf(decimalFormat.format(Float.parseFloat(balance2)+ Float.parseFloat(a)));
                                                u.setBalance(ImageSteganography.encryptMessage(newbalance2,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                                Log.e("receiver",newbalance2);
                                            } catch (Exception exception) {
                                                exception.printStackTrace();
                                            }

                                            new DAOUser().updateUser(key, u, new DAOUser.DataStatus() {
                                                @Override
                                                public void DataIsLoaded(List<User> users, List<String> keys) {

                                                }

                                                @Override
                                                public void DataIsInserted() {

                                                }

                                                @Override
                                                public void DataIsUpdated() {
                                                    Log.e(">>>>>>>>>>>>>>>>>","balance of receiver update done"+String.valueOf(Float.parseFloat(balance)+ Float.parseFloat(a)));
                                                }

                                                @Override
                                                public void DataIsDeleted() {

                                                }
                                            });
                                            count2++;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

                        //save into database
                        Intent intent = new Intent(getApplicationContext(), success_t.class);
                        intent.putExtra("sender", currentuserphone);
                        intent.putExtra("receiver", phone_no);
                        intent.putExtra("amount", a);
                        intent.putExtra("description", de);
                        intent.putExtra("date", currentTime.toString());

                        //jump to main menu page
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), mainmenu.class));
                finish();
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getIntent()));
                finish();
            }
        });
    }
}