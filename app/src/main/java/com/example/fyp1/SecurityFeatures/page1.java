package com.example.fyp1.SecurityFeatures;

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

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.profilepage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class page1 extends AppCompatActivity {
    //s1
    LinearLayout s1;
    FirebaseDatabase db;
    DatabaseReference databaseReference1;
    EditText q1,q2,aq1,aq2;
    Button save, back, skip;
    String ans1, ans2, on;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //s1

        skip = findViewById(R.id.skip);
        save = findViewById(R.id.save);
        back = findViewById(R.id.clean);
        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        aq1 = findViewById(R.id.aq1);
        aq2 = findViewById(R.id.aq2);

        q1.setEnabled(false);
        q2.setEnabled(false);
        //skip.setEnabled(true);

        on = ImageSteganography.encryptMessage("on",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
        db = FirebaseDatabase.getInstance();
        databaseReference1 = db.getReference("security1");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keyNode : snapshot.getChildren()) {
                    Security1 security1 = keyNode.getValue(Security1.class);

                    if((ImageSteganography.decryptMessage(security1.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))){
                        if(security1.getOnoff().equals(on)) {
                            Log.e("fuck"," you");
                            q1.setText(ImageSteganography.decryptMessage(security1.getQuestion1(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                            q2.setText(ImageSteganography.decryptMessage(security1.getQuestion2(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                            skip.setEnabled(false);
                            save.setEnabled(true);//setClickable(skip, false);

                            ans1 = ImageSteganography.decryptMessage(security1.getAns1(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                            ans2 = ImageSteganography.decryptMessage(security1.getAns2(), ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));

                        }break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((aq1.getText().toString().isEmpty())) {
                    aq1.setError("Answer is Required");
                    return;
                }
                if ((aq2.getText().toString().isEmpty())) {
                    aq2.setError("Answer is Required");
                    return;
                }
                if (!aq1.getText().toString().equals(ans1)) {
                    aq1.setError("Answer Wrong!");
                    return;
                }
                if (!aq2.getText().toString().equals(ans2)) {
                    aq2.setError("Answer Wrong!");
                    return;
                }
                //jump to page2
                Toast.makeText(page1.this,"Correct Answer!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), page2.class));
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aq1.setText("");
                aq2.setText("");
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),page2.class));
                finish();
            }
        });

    }

    public void setClickable(Button button, boolean b){
        button.setEnabled(b);
    }
}