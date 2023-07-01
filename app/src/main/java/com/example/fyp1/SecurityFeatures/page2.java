package com.example.fyp1.SecurityFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fyp1.ImageLoadAsyncTask;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.ImageSteganography.TextDecoding;
import com.example.fyp1.ImageSteganography.TextDecodingCallback;
import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.SHA;
import com.example.fyp1.profilepage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class page2 extends AppCompatActivity implements TextDecodingCallback {
    //s2
    String on;
    LinearLayout s2;
    FirebaseDatabase db;
    DatabaseReference databaseReference2;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Decode Class";
    //Initializing the UI components
    private TextView whether_decoded;
    private ImageView imageView;
    private String message, message1, message2;
    private EditText secret_key;
    private Uri filepath;
    //Bitmap
    private Bitmap original_image;

    private ProgressDialog decoding;

    String path, temp;
    SHA encoding;
    StorageReference mStorageRef;
    Button skip, check;
    Button choose_image_button ;
    Button decode_button;
    ImageView imageView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //s2
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        //find image name
        path = MainActivity.ph_otp.replace("+","");
        try
        {
            path = encoding.toHexString(encoding.getSHA(path));
            temp = encoding.toHexString(encoding.getSHA(MainActivity.ph_otp+".imagepassword"+ ".PNG"));
        }
        // For specifying wrong message digest algorithms
        catch ( NoSuchAlgorithmException e ) {
            System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
        }

        skip = findViewById(R.id.skip);
        choose_image_button = findViewById(R.id.choose_image_button);
        decode_button = findViewById(R.id.decode_button);
        check = findViewById(R.id.check);
        imageView1 = findViewById(R.id.imageview1);

        on = ImageSteganography.encryptMessage("on",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
        db = FirebaseDatabase.getInstance();
        databaseReference2 = db.getReference("security2");

        //Instantiation of UI components
        whether_decoded = findViewById(R.id.whether_decoded);
        imageView = findViewById(R.id.imageview);
        secret_key = findViewById(R.id.secret_key);

        whether_decoded.setText("Please Select An Image First");

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keyNode : snapshot.getChildren()) {
                    Security2 security2 = keyNode.getValue(Security2.class);

                    if((ImageSteganography.decryptMessage(security2.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))){

                        if(security2.getOnoff().equals(on)) {
                            choose_image_button.setEnabled(true);
                            skip.setEnabled(false);

                            //read from database
                            databaseReference2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot keyNode : snapshot.getChildren()) {
                                        String key = keyNode.getKey();
                                        Security2 security2 = keyNode.getValue(Security2.class);
                                        if((ImageSteganography.decryptMessage(security2.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))) {
                                            message1 = security2.getMessage();
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //read from storage
                            /**mStorageRef.child(path).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    for(StorageReference file:listResult.getItems()){
                                        if(file.getName().equals(temp)){
                                            file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    //Thread thread = new Thread() {
                                                        //public void run() {
                                                           // try {
                                                             //   original_image = Picasso.with(getApplicationContext()).load(uri).get();
                                                            //} catch (Exception e) {
                                                              //  e.printStackTrace();
                                                            //}
                                                        //}
                                                    //};
                                                    //thread.start();
                                                    ImageLoadAsyncTask imageLoadAsyncTask = null;
                                                    try {
                                                        imageLoadAsyncTask = new ImageLoadAsyncTask(new URL(uri.toString()), imageView1);
                                                    } catch (MalformedURLException e) {
                                                        e.printStackTrace();
                                                    }
                                                    imageLoadAsyncTask.execute();

                                                    if(imageLoadAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
                                                        original_image = ((BitmapDrawable)imageView1.getDrawable()).getBitmap();

                                                        //Making the ImageSteganography object
                                                        ImageSteganography imageSteganography = new ImageSteganography(secret_key.getText().toString(), original_image);

                                                        //Making the TextDecoding object
                                                        TextDecoding textDecoding = new TextDecoding(page2.this, page2.this);

                                                        //Execute Task
                                                        textDecoding.execute(imageSteganography);

                                                        message1 = message;
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });**/
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),profilepage.class));
                finish();
            }
        });

        //Choose Image Button
        choose_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageChooser();
            }
        });

        //Decode Button
        decode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filepath != null) {
                    //Making the ImageSteganography object
                    ImageSteganography imageSteganography = new ImageSteganography(secret_key.getText().toString(),
                            original_image);

                    //Making the TextDecoding object
                    TextDecoding textDecoding = new TextDecoding(page2.this, page2.this);

                    //Execute Task
                    textDecoding.execute(imageSteganography);

                    decoding = new ProgressDialog(page2.this);
                    decoding.setMessage("Processing, Please Wait...");
                    decoding.setTitle("Processing Image");
                    decoding.setIndeterminate(false);
                    decoding.setCancelable(false);
                    decoding.show();
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message2 = message;
                Log.e("?",message1+message2);

                try {
                    message2 = encoding.toHexString(encoding.getSHA(message2));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                if(message1.equals(message2)){
                    //jump to profile
                    startActivity(new Intent(getApplicationContext(), profilepage.class));
                    finish();
                }else{
                    whether_decoded.setText("Not this image!");
                }
            }
        });

    }
    private void ImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Image set to imageView
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filepath = data.getData();
            try {
                original_image = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);

                imageView.setImageBitmap(original_image);
                whether_decoded.setText("Image Selected");
                decode_button.setEnabled(true);
            } catch (IOException e) {
                Log.d(TAG, "Error : " + e);
            }
        }

    }

    @Override
    public void onStartTextEncoding1() {
        //Whatever you want to do by the start of textDecoding
    }

    @Override
    public void onCompleteTextEncoding1(ImageSteganography result) {

        //By the end of textDecoding

        if (result != null) {
            if (!result.isDecoded())
                whether_decoded.setText("No message found");
            else {
                if (!result.isSecretKeyWrong()) {
                    whether_decoded.setText("Decoded");
                    message = result.getMessage();
                    check.setEnabled(true);
                } else {
                    whether_decoded.setText("Wrong secret key");
                }
            }
        } else {
            whether_decoded.setText("Select Image First");
        }
        decoding.dismiss();
    }
}