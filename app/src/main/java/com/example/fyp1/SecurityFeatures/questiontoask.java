package com.example.fyp1.SecurityFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fyp1.DAOUser;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.User;
import com.example.fyp1.otp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class questiontoask extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference databaseReference1;
    EditText q1,q2,aq1,aq2;
    Button save, back;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questiontoask);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        aq1 = findViewById(R.id.aq1);
        aq2 = findViewById(R.id.aq2);

        db = FirebaseDatabase.getInstance();
        databaseReference1 = db.getReference("security1");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keyNode : snapshot.getChildren()) {
                    Security1 security1 = keyNode.getValue(Security1.class);
                    if((ImageSteganography.decryptMessage(security1.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))){
                        q1.setText(ImageSteganography.decryptMessage(security1.getQuestion1(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                        aq1.setText(ImageSteganography.decryptMessage(security1.getAns1(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                        q2.setText(ImageSteganography.decryptMessage(security1.getQuestion2(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                        aq2.setText(ImageSteganography.decryptMessage(security1.getAns2(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        count = 0;
        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((q1.getText().toString().isEmpty())){
                    q1.setError("Question Required");
                    return;
                }
                if((aq1.getText().toString().isEmpty())){
                    aq1.setError("Answer is Required");
                    return;
                }
                if((q2.getText().toString().isEmpty())){
                    q2.setError("Question Required");
                    return;
                }
                if((aq2.getText().toString().isEmpty())){
                    aq2.setError("Answer is Required");
                    return;
                }

                Security1 s1 = new Security1();
                try {
                    s1.setOnoff(ImageSteganography.encryptMessage("on",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    s1.setEmail(ImageSteganography.encryptMessage(MainActivity.currentuser,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    s1.setQuestion1(ImageSteganography.encryptMessage(q1.getText().toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    s1.setAns1(ImageSteganography.encryptMessage(aq1.getText().toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    s1.setQuestion2(ImageSteganography.encryptMessage(q2.getText().toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                    s1.setAns2(ImageSteganography.encryptMessage(aq2.getText().toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                databaseReference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot keyNode : snapshot.getChildren()) {
                            String key = keyNode.getKey();
                            Security1 security1 = keyNode.getValue(Security1.class);
                            if((ImageSteganography.decryptMessage(security1.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))) {
                                count++;
                                new DAOsecurity1().updateSecurity1(key, s1, new DAOsecurity1.DataStatus() {
                                    @Override
                                    public void DataIsLoaded(List<Security1> security1s, List<String> keys) {

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
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                databaseReference1.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(count==0){
                            new DAOsecurity1().addSecurity1(s1, new DAOsecurity1.DataStatus() {
                                @Override
                                public void DataIsLoaded(List<Security1> s1, List<String> keys) {

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
                    }
                });
                new DAOsecurity1().addSecurity1(s1, new DAOsecurity1.DataStatus() {
                    @Override
                    public void DataIsLoaded(List<Security1> s1, List<String> keys) {

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
                Toast.makeText(questiontoask.this, "The data is Updated!", Toast.LENGTH_SHORT).show();

            }
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), security_features.class));
                finish();
            }
        });
    }
}