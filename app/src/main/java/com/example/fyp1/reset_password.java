package com.example.fyp1;

//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.NoSuchAlgorithmException;


public class reset_password extends AppCompatActivity {

    EditText new_p,  new_cp;
    Button save, back;
    FirebaseUser user;
    String n_p, n_cp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }



        user = FirebaseAuth.getInstance().getCurrentUser();

        save = findViewById(R.id.save_newp);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_p = findViewById(R.id.reset_p);
                n_p = new_p.getText().toString();
                new_cp = findViewById(R.id.reset_cp);
                n_cp = new_cp.getText().toString();

                if(n_p.isEmpty()){
                    new_p.setError("Please Insert New Password!"+n_p+n_cp);
                    return;
                }

                if(n_cp.isEmpty()){
                    new_cp.setError("Please Insert Again The Password!");
                    return;
                }

                if(!(n_p.equals(n_cp))){
                    new_cp.setError("Password Do Not Match!");
                    return;
                }

                user.updatePassword(n_p).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(reset_password.this, "Password Updated!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(reset_password.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //back
        back = findViewById(R.id.backprofile);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), profilepage.class));
                finish();
            }
        });
    }
}