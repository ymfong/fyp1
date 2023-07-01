package com.example.fyp1.SecurityFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.User;
import com.example.fyp1.profilepage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class AdditionalSecurity extends AppCompatActivity{

    FirebaseDatabase db;
    DatabaseReference databaseReference1, databaseReference2;
    String s1, s2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_security);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseDatabase.getInstance();
        databaseReference1 = db.getReference("security1");
        databaseReference2 = db.getReference("security2");
        s1="";s2="";

        try {
            databaseReference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot keyNode : snapshot.getChildren()) {
                        User users = keyNode.getValue(User.class);
                        if ((ImageSteganography.decryptMessage(users.getEmail(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))) {
                            s1="true";
                            //startActivity(new Intent(getApplicationContext(), page1.class));
                            //finish();
                            break;
                        }
                    }
                    Log.e("?", "end 1");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            databaseReference1.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(s1=="true"){
                        startActivity(new Intent(getApplicationContext(), page1.class));
                        finish();
                    }
                }
            });

            databaseReference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot keyNode : snapshot.getChildren()) {
                        User users = keyNode.getValue(User.class);
                        if ((ImageSteganography.decryptMessage(users.getEmail(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))) {
                            s2="true";
                            Log.e("?1",s2);
                            break;
                        }
                    }
                    Log.e("?",s2);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            databaseReference2.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(s2=="true"){
                        startActivity(new Intent(getApplicationContext(), page2.class));
                        finish();
                    }
                }
            });

        }catch (Exception e){

        }finally {
            startActivity(new Intent(getApplicationContext(), profilepage.class));
            finish();
        }
    }
}