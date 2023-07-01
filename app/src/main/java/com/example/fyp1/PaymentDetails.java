package com.example.fyp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

public class PaymentDetails extends AppCompatActivity {
    TextView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        details = findViewById(R.id.details);

        Intent intent = getIntent();

        try{
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("PaymentDetails"));
            showDetail(jsonObject.getJSONObject("response"), intent.getStringExtra("PaymentAmount"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showDetail(JSONObject response, String paymentAmount) {
        try{
            details.setText(response.getString("id")+"\n"+response.getString("state")+"\n"+"RM"+paymentAmount);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}