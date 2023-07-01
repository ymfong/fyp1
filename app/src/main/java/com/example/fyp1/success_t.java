package com.example.fyp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class success_t extends AppCompatActivity {
Button button;
TextView t5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_t);
        Log.e(">","success directly to otp");

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        button = findViewById(R.id.button);
        t5 = findViewById(R.id.textView5);

        Intent intent = getIntent();

        t5.setText("From \t" + intent.getStringExtra("sender")
                +"\nTransfer \tRM " + intent.getStringExtra("amount")
                +"\nTo \t" + intent.getStringExtra("receiver")
                +"\nFor \t" + intent.getStringExtra("description")
                +"\nAt \t" + intent.getStringExtra("date"));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), mainmenu.class));
                finish();
            }
        });
    }
}