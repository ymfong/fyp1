package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.example.fyp1.Config.Config;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class top_up extends AppCompatActivity {
    EditText amount1, password;
    Button go_pay, goback, success3, fail3;
    String current_p, p_toHex,a;
    LinearLayout success_l, fail_l,amountInput, group_waiting;
    SHA encoding;

    FirebaseDatabase db;
    DatabaseReference databaseReference;

    //add when bank API
    private static final int REQUEST_CODE = 1234;
    String API_GET_TOKEN = "http://10.0.2.2/braintree/main.php";
    String API_CHECK_OUT = "http://10.0.2.2/braintree/checkout.php";

    String token, amount;
    HashMap<String, String> paramsHash;
    DecimalFormat decimalFormat;
    String balance;
    String key, cu, ph, am, t, d;
    User u;
    Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        go_pay = findViewById(R.id.go_pay);
        goback = findViewById(R.id.goback);
        amountInput = findViewById(R.id.amountInput);
        success_l = findViewById(R.id.success_l);
        success3 = findViewById(R.id.success3);
        fail_l = findViewById(R.id.fail_l);
        fail3 = findViewById(R.id.fail3);
        group_waiting = findViewById(R.id.waiting_group);

        current_p = MainActivity.passinHex;
        decimalFormat = new DecimalFormat("0.00");

        new getToken().execute();

        go_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount1 = findViewById(R.id.amount_topup);
                a = amount1.getText().toString();
                password = findViewById(R.id.topup_p);
                String p = password.getText().toString();

                if(a.isEmpty()) {
                    amount1.setError("Please Enter The Amount!");return;
                }

                if(p.isEmpty()) {
                    password.setError("Please Enter The Password!");return;
                }

                try
                {
                    p_toHex = encoding.toHexString(encoding.getSHA(p));
                    Log.e(">>>>>>>>>>>",p_toHex);
                }
                // For specifying wrong message digest algorithms
                catch ( NoSuchAlgorithmException e ) {
                    System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
                }

                if(Float.parseFloat(a)<=0){
                    amount1.setError("Top up cannot less than RM0.00!");return;
                }

                if(!p_toHex.equals(current_p)) {
                    password.setError("Passwords Do Not Match!");return;
                }

                amountInput.setVisibility(View.GONE);

                db = FirebaseDatabase.getInstance();
                databaseReference = db.getReference("user");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                            key = keyNode.getKey();
                            u = keyNode.getValue(User.class);
                            //update receiver
                            if(ImageSteganography.decryptMessage(u.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser)){
                                try {
                                    balance = ImageSteganography.decryptMessage(u.getBalance(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                    String newbalance2 = String.valueOf(decimalFormat.format(Float.parseFloat(balance)+ Float.parseFloat(a)));
                                    u.setBalance(ImageSteganography.encryptMessage(newbalance2,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));

                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                submitPayment();
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), mainmenu.class));
                finish();
            }
        });
    }


    private void submitPayment(){
        String payValue=amount1.getText().toString();
        if(!payValue.isEmpty())
        {
            DropInRequest dropInRequest=new DropInRequest().clientToken(token);
            startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);
        }
        else
            Toast.makeText(this, "Enter a valid amount for payment", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNounce = nonce.getNonce();

                if (!amount1.getText().toString().isEmpty()) {
                    amount = amount1.getText().toString();
                    paramsHash = new HashMap<>();
                    paramsHash.put("amount", amount);
                    paramsHash.put("nonce", strNounce);

                    sendPayments();
                } else {
                    Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "User cancel", Toast.LENGTH_SHORT).show();
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("EDMT_ERROR", error.toString());
            }
        }
    }

    private void sendPayments(){
        RequestQueue queue= Volley.newRequestQueue(top_up.this);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, API_CHECK_OUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.toString().contains("Successful")){
                            Toast.makeText(top_up.this, "Reload Success", Toast.LENGTH_SHORT).show();

                            new DAOUser().updateUser(key, u, new DAOUser.DataStatus() {
                                @Override
                                public void DataIsLoaded(List<User> users, List<String> keys) {

                                }

                                @Override
                                public void DataIsInserted() {

                                }

                                @Override
                                public void DataIsUpdated() {
                                    Log.e(">>>>>>>>>>>>>>>>>","balance of update done"+String.valueOf(Float.parseFloat(balance)));
                                }

                                @Override
                                public void DataIsDeleted() {

                                }
                            });

//reload record
                            Date currentTime = Calendar.getInstance().getTime();
                            try {
                                cu = ImageSteganography.encryptMessage("VISA",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                ph = ImageSteganography.encryptMessage(MainActivity.ph_otp,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                am = ImageSteganography.encryptMessage(a,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                t = ImageSteganography.encryptMessage(currentTime.toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                                d = ImageSteganography.encryptMessage("top up purpose",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
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
                                    Toast.makeText(top_up.this, "The Transaction is Successfully Done!", Toast.LENGTH_SHORT).show();
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

                            startActivity(new Intent(getApplicationContext(), mainmenu.class));
                            finish();
                        }
                        else {
                            Toast.makeText(top_up.this, "Reload Failed", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("Response",response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Err",error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(paramsHash==null)
                    return null;
                Map<String,String> params=new HashMap<>();
                for(String key:paramsHash.keySet())
                {
                    params.put(key,paramsHash.get(key));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("Content-type","application/x-www-form-urlencoded");
                return params;
            }
        };
        RetryPolicy mRetryPolicy=new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(mRetryPolicy);
        queue.add(stringRequest);
    }

    private class getToken extends AsyncTask {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(top_up.this, android.R.style.Theme_DeviceDefault_Dialog);
            mDialog.setCancelable(false);
            mDialog.setMessage("Please wait");
            mDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects){
            HttpClient client = new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    mDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            group_waiting.setVisibility(View.GONE);
                            amountInput.setVisibility(View.VISIBLE);

                            token = responseBody;
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {
                    mDialog.dismiss();
                    Log.d("EDMT_ERROR", exception.toString());
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

}