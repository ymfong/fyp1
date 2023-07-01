package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class viewmore extends AppCompatActivity {
    TextView rt_back, inandout;
    Button in, out;
    RecyclerView whole_rt;
    FirebaseDatabase db;
    DatabaseReference databaseReference, dr_transaction;
    String currentuserphone;
    DecimalFormat decimalFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmore);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        rt_back = findViewById(R.id.rt_back);
        inandout = findViewById(R.id.inandout);
        in = findViewById(R.id.in);
        out = findViewById(R.id.out);

        //back to main menu
        rt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), mainmenu.class));
                finish();
            }
        });

        //get current user phone
        whole_rt = findViewById(R.id.whole_rt);
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    User user = keyNode.getValue(User.class);
                    if(ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser)){
                        currentuserphone = ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //search records
        decimalFormat = new DecimalFormat("0.00");
        dr_transaction = db.getReference("transaction");
        List<Transaction> transactionList = new ArrayList<Transaction>();
        List<String> keylist = new ArrayList<String>();
        dr_transaction.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    Transaction transaction = keyNode.getValue(Transaction.class);
                    if(ImageSteganography.decryptMessage(transaction.getSender_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                        transaction.setAmount("-RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                        transactionList.add(transaction);
                        keylist.add(keyNode.getKey());
                    }
                    if(ImageSteganography.decryptMessage(transaction.getReceiver_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                        transaction.setAmount("+RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                        transactionList.add(transaction);
                        keylist.add(keyNode.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //display records
        new DAOtransaction().readtransaction2(new DAOtransaction.DataStatus(){
            @Override
            public void DataIsLoaded(List<Transaction> t, List<String> keys) {
                //get the latest records
                List<Transaction> latestList = new ArrayList<Transaction>();
                List<String> latestKey = new ArrayList<String>();
                int len = transactionList.size() - 1;
                for (int i = len; i >= 0 ; i--) {
                    // Append the elements in reverse order
                    latestList.add(transactionList.get(i));
                    latestKey.add(keylist.get(i));
                }

                //new RecyclerView_Cofig().setConfig(recyclerView, mainmenu.this, transactionList, keylist);
                new RecyclerView_Cofig().setConfig(whole_rt, viewmore.this, latestList, latestKey);
                //Log.d(">>>>>>>>>>>>>>>>", transactionList.toString());

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

        //declare the listener first
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //find related list
                //Log.e(">>>>>>>>>>>>>>","dsafaaf");
                dr_transaction.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        transactionList.clear();
                        keylist.clear();
                        switch(view.getId())
                        {
                            case R.id.inandout:
                                in.setBackgroundColor(Color.parseColor("#5271FF"));
                                in.setTextColor(Color.parseColor("#FFFFFFFF"));
                                out.setBackgroundColor(Color.parseColor("#5271FF"));
                                out.setTextColor(Color.parseColor("#FFFFFFFF"));
                                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                                    Transaction transaction = keyNode.getValue(Transaction.class);
                                    if(ImageSteganography.decryptMessage(transaction.getSender_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                                            transaction.setAmount("-RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                                            transactionList.add(transaction);
                                        keylist.add(keyNode.getKey());
                                    }
                                    if(ImageSteganography.decryptMessage(transaction.getReceiver_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                                        transaction.setAmount("+RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                                        transactionList.add(transaction);
                                        keylist.add(keyNode.getKey());
                                    }
                                }
                                break;
                            case R.id.in :
                                in.setBackgroundColor(Color.parseColor("#5271FF"));
                                in.setTextColor(Color.parseColor("#FFFFFFFF"));
                                out.setBackgroundColor(Color.parseColor("#EDEBEB"));
                                out.setTextColor(Color.parseColor("#000000"));
                                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                                    Transaction transaction = keyNode.getValue(Transaction.class);
                                    if(ImageSteganography.decryptMessage(transaction.getReceiver_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                                        transaction.setAmount("+RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                                        transactionList.add(transaction);
                                        keylist.add(keyNode.getKey());
                                    }
                                }
                                break;
                            case R.id.out :
                                Log.e(">>>>>>>>>>>>>>","dsafaaf_out");
                                out.setBackgroundColor(Color.parseColor("#5271FF"));
                                out.setTextColor(Color.parseColor("#FFFFFFFF"));
                                in.setBackgroundColor(Color.parseColor("#EDEBEB"));
                                in.setTextColor(Color.parseColor("#000000"));
                                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                                    Transaction transaction = keyNode.getValue(Transaction.class);
                                    if(ImageSteganography.decryptMessage(transaction.getSender_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(currentuserphone)){
                                        transaction.setAmount("-RM"+decimalFormat.format(Float.parseFloat(ImageSteganography.decryptMessage(transaction.getAmount(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)))));
                                        transactionList.add(transaction);
                                        keylist.add(keyNode.getKey());
                                    }
                                }
                                break;
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
                        //get the latest records
                        List<Transaction> latestList = new ArrayList<Transaction>();
                        List<String> latestKey = new ArrayList<String>();
                        int len = transactionList.size() - 1;
                        for (int i = len; i >= 0 ; i--) {
                            // Append the elements in reverse order
                            latestList.add(transactionList.get(i));
                            latestKey.add(keylist.get(i));
                        }

                        //new RecyclerView_Cofig().setConfig(recyclerView, mainmenu.this, transactionList, keylist);
                        new RecyclerView_Cofig().setConfig(whole_rt, viewmore.this, latestList, latestKey);
                        Log.d(">>>>>>>>>>>>>>>>", transactionList.toString());

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
            }
        };

        //assign listener
        inandout.setOnClickListener(onClickListener);
        in.setOnClickListener(onClickListener);
        out.setOnClickListener(onClickListener);
    }
}