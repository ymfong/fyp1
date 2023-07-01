package com.example.fyp1.SecurityFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.fyp1.DAOUser;
import com.example.fyp1.ImageSteganography.Encode;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.User;
import com.example.fyp1.profilepage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class security_features extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference databaseReference1, databaseReference2;
    Switch switch1, switch2;
    Button submit, backup, back;
    Security1 finals1;
    Security2 finals2;
    String keys1, keys2;
    String on, off;
    String exist1, exist2;
    String statusSwitch1, statusSwitch2;
    ImageButton imageButton1, imageButton2;
    int stoploop1, stoploop2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_features);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);

        on = ImageSteganography.encryptMessage("on",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
        off = ImageSteganography.encryptMessage("off",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
        stoploop1=0;stoploop2=0;

        db = FirebaseDatabase.getInstance();
        databaseReference1 = db.getReference("security1");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keyNode : snapshot.getChildren()) {
                    keys1 = keyNode.getKey();
                    Security1 security1 = keyNode.getValue(Security1.class);Log.e("?","in 1 loop");
                    if((ImageSteganography.decryptMessage(security1.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))){
                        if(security1.getOnoff().equals(on)){
                            switch1.setChecked(true);
                        }else{
                            switch1.setChecked(false);
                        }
                        exist1 = ImageSteganography.decryptMessage(security1.getOnoff(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        switch1.setEnabled(true);
                        switch1.setTextColor(Color.parseColor("#4CAF50"));
                        break;
                    }else{
                        switch1.setChecked(false);
                        switch1.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        db = FirebaseDatabase.getInstance();
        databaseReference2 = db.getReference("security2");
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keyNode : snapshot.getChildren()) {
                    keys2 = keyNode.getKey();
                    Security2 security2 = keyNode.getValue(Security2.class);Log.e("?","in 1 loop");
                    if((ImageSteganography.decryptMessage(security2.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))){
                        if(security2.getOnoff().equals(on)){
                            switch2.setChecked(true);
                        }else{
                            switch2.setChecked(false);
                        }
                        exist2 = ImageSteganography.decryptMessage(security2.getOnoff(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        switch2.setEnabled(true);
                        switch2.setTextColor(Color.parseColor("#4CAF50"));
                        break;
                    }else{
                        switch2.setChecked(false);
                        switch2.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageButton1 = findViewById(R.id.imageButton1);
        imageButton2 = findViewById(R.id.imageButton2);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), questiontoask.class));
            }
        });

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), imagepassword.class));
            }
        });

        submit = findViewById(R.id.save);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch1.isChecked()) {
                    statusSwitch1 = switch1.getTextOn().toString();
                }else{
                    statusSwitch1 = switch1.getTextOff().toString();
                }
                if (switch2.isChecked()) {
                    statusSwitch2 = switch2.getTextOn().toString();
                }else{
                    statusSwitch2 = switch2.getTextOff().toString();
                }

                Log.e("?1",exist1+statusSwitch1);
                if(!(statusSwitch1.toLowerCase().equals(exist1))){
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot keyNode : snapshot.getChildren()) {
                                keys1 = keyNode.getKey();
                                Security1 security1 = keyNode.getValue(Security1.class);
                                if((ImageSteganography.decryptMessage(security1.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))){
                                    finals1 = security1;
                                    if (switch1.isChecked()) {Log.e("?","in on 1 loop");
                                        finals1.setOnoff(on);
                                        exist1 = "on";
                                    }
                                    else {Log.e("?","in off 1 loop");
                                        finals1.setOnoff(off);
                                        exist1 = "off";
                                    }
                                    if(stoploop1==0){
                                        new DAOsecurity1().updateSecurity1(keys1, finals1, new DAOsecurity1.DataStatus() {
                                            @Override
                                            public void DataIsLoaded(List<Security1> s1, List<String> keys) {

                                            }

                                            @Override
                                            public void DataIsInserted() {

                                            }

                                            @Override
                                            public void DataIsUpdated() {
                                                Toast.makeText(security_features.this, "Security Questions Updated", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void DataIsDeleted() {

                                            }
                                        });
                                        stoploop1++;
                                    }
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                Log.e("?2",exist2+statusSwitch2);
                if(!(statusSwitch2.toLowerCase().equals(exist2))) {
                    databaseReference2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot keyNode : snapshot.getChildren()) {Log.e("?","in loop");
                                keys2 = keyNode.getKey();
                                Security2 security2 = keyNode.getValue(Security2.class);
                                if ((ImageSteganography.decryptMessage(security2.getEmail(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))) {
                                    finals2 = security2;
                                    if (switch2.isChecked()) {
                                        finals2.setOnoff(on);
                                        exist2 = "on";
                                    } else {
                                        finals2.setOnoff(off);
                                        switch2.setChecked(false);Log.e("?","in loop off");
                                        exist2 = "off";
                                    }

                                    if(stoploop2==0){
                                        new DAOsecurity2().updateSecurity2(keys2, finals2, new DAOsecurity2.DataStatus() {
                                            @Override
                                            public void DataIsLoaded(List<Security2> s2, List<String> keys) {

                                            }

                                            @Override
                                            public void DataIsInserted() {

                                            }

                                            @Override
                                            public void DataIsUpdated() {
                                                Toast.makeText(security_features.this, "Image Password Updated"+off+","+finals2.getOnoff(), Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void DataIsDeleted() {

                                            }
                                        });
                                        stoploop2++;
                                        startActivity(new Intent(getIntent()));
                                        finish();
                                    }
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                Toast.makeText(getApplicationContext(), "Security Questions Setup :" + statusSwitch1 + "\n" + "Image password :" + statusSwitch2, Toast.LENGTH_LONG).show(); // display the current state for switch's
            }
        });

        backup = findViewById(R.id.backup);
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Encode.class));
            }
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), profilepage.class));
                finish();
            }
        });
    }
}