package com.example.fyp1.ImageSteganography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.fyp1.*;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Encode extends AppCompatActivity implements TextEncodingCallback{

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Encode Class";
    //Created variables for UI
    private TextView whether_encoded;
    private ImageView imageView;
    private EditText secret_key;
    //Objects needed for encoding
    private TextEncoding textEncoding,textEncoding2;
    private ImageSteganography imageSteganography, imageSteganography2;
    private ProgressDialog save;
    private Uri filepath;
    //Bitmaps
    private Bitmap original_image;
    private Bitmap encoded_image;

    //add when image steganography
    Button encode_button;
    StorageReference mStorageRef;
    StorageTask mUploadTask;
    String message;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    String key;
    SHA encoding;
    Bitmap imgToSave1, imgToSave2;

    DatabaseReference mdatabaseRef;
    String path, temp;
    Activity activity;
    public static String extension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        //initialized the UI components
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        whether_encoded = findViewById(R.id.whether_encoded);
        imageView = findViewById(R.id.imageview);
        secret_key = findViewById(R.id.secret_key);

        Button choose_image_button = findViewById(R.id.choose_image_button);
        encode_button = findViewById(R.id.encode_button);
        Button save_image_button = findViewById(R.id.save_image_button);
        Button back = findViewById(R.id.back);
        encode_button.setEnabled(false);
        save_image_button.setEnabled(false);

        //read data to encrypt
        whether_encoded.setText("Please Select An Image First");
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    key = keyNode.getKey();
                    User user = keyNode.getValue(User.class);
                    if(ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser)){
                        String getname = ImageSteganography.decryptMessage(user.getName(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        String getemail = ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        String getphone = ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        String getbalance = ImageSteganography.decryptMessage(user.getBalance(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        message = getname+","+getemail+","+getphone+","+getbalance;
                        //message="DNBFL^%&^%%874769";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //checkAndRequestPermissions();


        //Choose image button
        choose_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageChooser();
            }
        });

        //Encode Button
        encode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filepath != null) {
                    if (message != null) {
                        //for personal information
                        //ImageSteganography Object instantiation
                        imageSteganography = new ImageSteganography(message,
                                secret_key.getText().toString(),
                                original_image);
                        //TextEncoding object Instantiation
                        textEncoding = new TextEncoding(Encode.this, Encode.this);
                        //Executing the encoding
                        textEncoding.execute(imageSteganography);

                        save_image_button.setEnabled(true);
                    }else{
                        Log.e("???","no message");
                    }
                }else{
                    Log.e("???","no path");
                }
            }
        });

        //Save image button
        save_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lock = new ReentrantLock();
                Bitmap imgToSave = encoded_image;
                Thread PerformEncoding = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //lock.lock();
                        saveToStorage(imgToSave);
                    }
                });
                save = new ProgressDialog(Encode.this);
                save.setMessage("Saving, Please Wait...");
                save.setTitle("Saving Image");
                save.setIndeterminate(false);
                save.setCancelable(false);
                save.show();
                PerformEncoding.start();
                //for secret key
                whether_encoded.setText("Image Uploaded");


                //Log.e(">>>>>>>>>>>>",filepath+ secret_key.getText().toString()+ original_image+ Encode.this+ getFileExtension(filepath));
                //decode(filepath, secret_key.getText().toString(), imgToSave, Encode.this, getFileExtension(filepath));

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), initialpage.class));
                finish();
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
                Log.e("dsf",original_image+".");

                imageView.setImageBitmap(original_image);

                encode_button.setEnabled(true);
                whether_encoded.setText("Image Selected");
            } catch (IOException e) {
                Log.d(TAG, "Error : " + e);
            }
        }

    }

    // Override method of TextEncodingCallback

    @Override
    public void onStartTextEncoding() {
        //Whatever you want to do at the start of text encoding
    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {

        //By the end of textEncoding

        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image();
            whether_encoded.setText("Data Encoded");
            imageView.setImageBitmap(encoded_image);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void saveToStorage(Bitmap encoded_image){
        if(filepath != null){
            //extension = getFileExtension(filepath);
            try
            {
                StorageReference fileReference = mStorageRef
                        .child(encoding.toHexString(encoding.getSHA(MainActivity.currentuser.replace("@","").replace(".",""))))
                        .child(encoding.toHexString(encoding.getSHA("ENCODED"+MainActivity.currentuser+".")));

                /*StorageReference fileReference = mStorageRef
                        .child(encoding.toHexString(encoding.getSHA("admin")))
                        .child(encoding.toHexString(encoding.getSHA("ENCODED_key")));*/

                OutputStream fOut;
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File file = new File(directory, "Encoded" + ".PNG"); // the File to save ,
                try {
                    fOut = new FileOutputStream(file);
                    encoded_image.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file
                    fOut.flush(); // Not really required
                    fOut.close(); // do not forget to close the stream
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                filepath = Uri.fromFile(file.getAbsoluteFile());
                mUploadTask = fileReference.putFile(filepath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                whether_encoded.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        save.dismiss();
                                    }
                                });

                                Toast.makeText(Encode.this, "Upload successful", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Encode.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            // For specifying wrong message digest algorithms
            catch ( NoSuchAlgorithmException e ) {
                System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
            }

        }else{
            Toast.makeText(this, "No file selected",Toast.LENGTH_SHORT).show();
        }
    }

    /**private void saveSecretkeyToStorage(){
        Log.e("???",encoded_image.toString());
        //ImageSteganography Object instantiation
        encoded_image = original_image;
        imageSteganography2 = new ImageSteganography(secret_key.getText().toString(),
                MainActivity.secretkey,
                original_image);
        //TextEncoding object Instantiation
        textEncoding2 = new TextEncoding(Encode.this, Encode.this);
        //Executing the encoding
        textEncoding2.execute(imageSteganography2);
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        imgToSave2 = encoded_image;
            Log.e("new???",imgToSave2.toString());

            if(filepath != null){
                try
                {
                    String newname = encoding.toHexString(encoding.getSHA(secret_key.getText().toString()));
                    StorageReference fileReference = mStorageRef
                            .child(encoding.toHexString(encoding.getSHA(MainActivity.currentuser.replace("@","").replace(".",""))))
                            .child(encoding.toHexString(encoding.getSHA("ENCODED"+newname+MainActivity.currentuser+".")));

                    OutputStream fOut2;
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    File file2 = new File(directory, "Encoded_key" + ".PNG"); // the File to save ,
                    if(file2.exists()) file2.delete();
                    try {
                        fOut2 = new FileOutputStream(file2);
                        original_image.compress(Bitmap.CompressFormat.PNG, 100, fOut2); // saving the Bitmap to a file
                        fOut2.flush(); // Not really required
                        fOut2.close(); // do not forget to close the stream
                    }catch (Exception e){

                    }

                    Uri uri = Uri.fromFile(file2.getAbsoluteFile());

                    mUploadTask = fileReference.putFile(uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    whether_encoded.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            save.dismiss();
                                        }
                                    });

                                    Toast.makeText(Encode.this, "Upload successful", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Encode.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                // For specifying wrong message digest algorithms
                catch ( NoSuchAlgorithmException e ) {
                    System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
                }

            }else{
                Toast.makeText(this, "No file selected",Toast.LENGTH_SHORT).show();
            }
    }**/
}