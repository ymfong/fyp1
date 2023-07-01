package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class mainmenu extends AppCompatActivity {
    private RecyclerView recyclerView;
    Button transfer,topup;
    String b, currentuserphone;
    TextView balance, viewmore;
    FirebaseDatabase db;
    private DatabaseReference databaseReference, dr_transaction;

    //limit the transaction record in main page
    //int t_limit=5;
    List<Transaction> transactionList;
    List<String> keylist ;
    List<Transaction> latestList ;
    List<String> latestKey;
    int latest;
    DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);Log.e(">","directly to main");

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        TextView profile1 = findViewById(R.id.profile1);
        TextView wallet = findViewById(R.id.wallet);
        TextView logout = findViewById(R.id.logout);
        transfer = findViewById(R.id.m_t);
        topup = findViewById(R.id.topup);
        viewmore = findViewById(R.id.viewmore);

        //from login or sign in
        /*try{
            current = otpforlogin.currentUser;
            Toast.makeText(mainmenu.this, "fxgj", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            current = otp.currentuser;
        }*/

        //balance
        decimalFormat = new DecimalFormat("0.00");
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    User user = keyNode.getValue(User.class);
                    if(ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser)){
                        MainActivity.ph_otp = ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        try {
                            currentuserphone = ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                            b = ImageSteganography.decryptMessage(user.getBalance(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        balance = findViewById(R.id.balance);Log.e("!!!",currentuserphone);
                        balance.setText("RM "+decimalFormat.format(Float.parseFloat(b)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //profile
        profile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), profilepage.class));
                finish();
            }
        });

        //wallet
        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getIntent()));
                finish();
            }
        });

        //logout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setView(inflater.inflate(R.layout.either_logout, null))
                    // Add action buttons
                    .setPositiveButton("Yes, Log Me Out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // log out the user ...
                            Toast.makeText(mainmenu.this, "Email Updated!", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton("Naah, Just Kidding", null).create().show();
            }
        });


        //transaction list
        recyclerView = (RecyclerView) findViewById(R.id.rt);

        //find related list
        dr_transaction = db.getReference("transaction");
        /*List<Transaction>*/ transactionList = new ArrayList<Transaction>();
        /*List<String> */keylist = new ArrayList<String>();
        dr_transaction.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    Transaction transaction = keyNode.getValue(Transaction.class);
                    //Log.d(">>>>>>>>>>>>>>>>3",transaction.getSender_no()+MainActivity.currentuser+currentuserphone);
                    if(ImageSteganography.decryptMessage(transaction.getSender_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                        transaction.setAmount("-RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                        transactionList.add(transaction);
                        keylist.add(keyNode.getKey());
                        //Log.d(">>>>>>>>>>>>>>>>1",transaction.getDate());
                        //t_limit-=1;
                    }
                    if(ImageSteganography.decryptMessage(transaction.getReceiver_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                        transaction.setAmount("+RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                        transactionList.add(transaction);
                        keylist.add(keyNode.getKey());
                        //Log.d(">>>>>>>>>>>>>>>>2",transaction.getDate());
                        //t_limit-=1;
                    }
                    /*if(t_limit==0){
                        break;
                    }*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Show latest records
        new DAOtransaction().readtransaction2(new DAOtransaction.DataStatus(){
            @Override
            public void DataIsLoaded(List<Transaction> t, List<String> keys) {
                //get the latest five record
                /*List<Transaction>*/ latestList = new ArrayList<Transaction>();
                /*List<String>*/ latestKey = new ArrayList<String>();
                //Log.d(">>>>>>>>>>>>>>>>2",transactionList.size()+"ref");
                latest=0;
                int len = transactionList.size()-1;
                for (int i = len; i >= 0 && latest<5; i--) {
                    // Append the elements in reverse order
                    latestList.add(transactionList.get(i));
                    latestKey.add(keylist.get(i));
                    latest++;
                    //Log.d(">>>>>>>>>>>>>>>>3443",transactionList.get(i).getSender_no()+"="+latestList.get(i).getSender_no());
                }

                //new RecyclerView_Cofig().setConfig(recyclerView, mainmenu.this, transactionList, keylist);
                new RecyclerView_Cofig().setConfig(recyclerView, mainmenu.this, latestList, latestKey);
                Log.d(">>>>>>>>>>>>>>>>", latestList.toString());

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

        //make transaction
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), transfer_page.class));
                finish();
            }
        });

        //link to bank sdk
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), top_up.class));
                finish();
            }
        });

        //view more transaction record
        viewmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), viewmore.class));
                finish();
            }
        });
    }
}