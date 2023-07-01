package com.example.fyp1.SecurityFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.Encode;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.ImageSteganography.TextEncoding;
import com.example.fyp1.ImageSteganography.TextEncodingCallback;
import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.SHA;
import com.example.fyp1.User;
import com.example.fyp1.profilepage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

public class imagepassword extends AppCompatActivity implements TextEncodingCallback {
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Encode Class";
    //Created variables for UI
    private TextView whether_encoded;
    private ImageView imageView;
    private EditText secret_key;
    //Objects needed for encoding
    private TextEncoding textEncoding;
    private ImageSteganography imageSteganography;
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
    SHA encoding;
    DatabaseReference databaseReference2;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagepassword);

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
        message = generatePassword(8).toString()+MainActivity.ph_otp;

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
                        textEncoding = new TextEncoding(imagepassword.this, imagepassword.this);
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
                save = new ProgressDialog(imagepassword.this);
                save.setMessage("Saving, Please Wait...");
                save.setTitle("Saving Image");
                save.setIndeterminate(false);
                save.setCancelable(false);
                save.show();
                PerformEncoding.start();
                //for secret key
                whether_encoded.setText("Image Uploaded");

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), security_features.class));
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
        count = 0;
        if(filepath != null){
            //extension = getFileExtension(filepath);
            try
            {
                StorageReference fileReference = mStorageRef
                        .child(encoding.toHexString(encoding.getSHA(MainActivity.ph_otp.replace("+",""))))
                        .child(encoding.toHexString(encoding.getSHA(MainActivity.ph_otp+".imagepassword"+ ".PNG")));

                OutputStream fOut;
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), MainActivity.ph_otp+"imagepassword"+ ".PNG"); // the File to save ,
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

                            Toast.makeText(imagepassword.this, "Upload successful", Toast.LENGTH_LONG).show();

                            Security2 s2 = new Security2();
                            try {
                                s2.setOnoff(ImageSteganography.encryptMessage("on",ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                s2.setEmail(ImageSteganography.encryptMessage(MainActivity.currentuser,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                s2.setMessage(encoding.toHexString(encoding.getSHA(message)));
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                            db = FirebaseDatabase.getInstance();
                            databaseReference2 = db.getReference("security2");
                            try{
                                databaseReference2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot keyNode : snapshot.getChildren()) {
                                            String key = keyNode.getKey();
                                            Security2 security2 = keyNode.getValue(Security2.class);
                                            Log.e("i", ImageSteganography.decryptMessage(security2.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)+"="+MainActivity.currentuser));
                                            if((ImageSteganography.decryptMessage(security2.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser))) {
                                                new DAOsecurity2().updateSecurity2(key, s2,new DAOsecurity2.DataStatus() {
                                                    @Override
                                                    public void DataIsLoaded(List<Security2> security2s, List<String> keys) {

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
                                                count++;Log.e("i","update"+count);
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                databaseReference2.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if(count==0){
                                            new DAOsecurity2().addSecurity2(s2, new DAOsecurity2.DataStatus() {
                                                @Override
                                                public void DataIsLoaded(List<Security2> s2, List<String> keys) {
                                                    Toast.makeText(imagepassword.this, "The data is Recorded!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void DataIsInserted() {

                                                }

                                                @Override
                                                public void DataIsUpdated() {
                                                    Toast.makeText(imagepassword.this, "The data is Updated!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void DataIsDeleted() {

                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            catch (Exception e){

                            }

                            whether_encoded.setText("The image password had save inside your storage.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(imagepassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private static char[] generatePassword(int length) {
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[length];

        password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password[3] = numbers.charAt(random.nextInt(numbers.length()));

        for(int i = 4; i< length ; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return password;
    }
}