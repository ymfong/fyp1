package com.example.fyp1.ImageSteganography;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.MainActivity;
import com.example.fyp1.R;
import com.example.fyp1.SHA;
import com.example.fyp1.SecurityFeatures.page2;
import com.example.fyp1.UploadPic;
import com.example.fyp1.profilepage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class d extends AppCompatActivity implements TextDecodingCallback {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Decode Class";
    //Initializing the UI components
    private TextView textView;
    private ImageView imageView;
    private EditText message;
    private EditText secret_key;
    private Uri filepath;
    //Bitmap
    private Bitmap original_image;

    //add when edit profile pic
    StorageReference mStorageRef;
    DatabaseReference mdatabaseRef;
    SHA encoding;
    String path, temp;
    String nameinhex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d);

        //Instantiation of UI components
        textView = findViewById(R.id.whether_decoded);

        imageView = findViewById(R.id.imageview);

        message = findViewById(R.id.message);
        secret_key = findViewById(R.id.secret_key);

        Button choose_image_button = findViewById(R.id.choose_image_button);
        Button decode_button = findViewById(R.id.decode_button);

        /*ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File file = new File(directory, "Encoded_key.PNG");
        //file = new File("/data/data/com.example.fyp1/app_imageDir/Encoded.PNG");
        filepath = Uri.parse(file.toString());
        Log.e("??????????????",Uri.fromFile(file.getAbsoluteFile()).toString());
        original_image = ((BitmapDrawable) Drawable.createFromPath(file.toString())).getBitmap();
        imageView.setImageDrawable(Drawable.createFromPath(file.toString()));*/

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
        final Bitmap[] bitmap = new Bitmap[1];
        //read profile pic
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mStorageRef.child(path).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                //Log.e(">>>>>>.","no way2");
                for(StorageReference file:listResult.getItems()){
                    if(file.getName().equals(temp)){
                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                /*Picasso.with(d.this)
                                        .load(uri)
                                        .into(new Target() {
                                            @Override
                                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                // Todo: Do something with your bitmap here
                                                original_image = bitmap;
                                                imageView.setImageBitmap(original_image);
                                                Log.e("hi"," i am here");
                                            }

                                            @Override
                                            public void onBitmapFailed(Drawable errorDrawable) {
                                            }

                                            @Override
                                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                            }
                                        });
                                Thread thread = new Thread() {
                                    public void run() {
                                        try {
                                            bitmap[0] = Picasso.with(d.this).load(uri).get();Log.e("?", bitmap[0].toString());

                                            } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                thread.start();original_image = bitmap[0];
                                Log.e("hi"," i am here2"+uri+"\n"+original_image);
                                Picasso.with(d.this).load(uri.toString()).into(imageView);
                                try {
                                    final File localFile = File.createTempFile("Images", "bmp");
                                    mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            original_image = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            imageView.setImageBitmap(original_image);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(d.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/

                                final long ONE_MEGABYTE = 1024 * 1024;
                                file.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                      @Override
                                      public void onSuccess(byte[] bytes) {
                                          original_image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                          imageView.setImageBitmap(original_image);filepath = uri;
                                          // Data for "images/island.jpg" is returns, use this as needed
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
            }
        });
        Log.e("hi"," i am here and done");


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
                    TextDecoding textDecoding = new TextDecoding(d.this, d.this);

                    //Execute Task
                    textDecoding.execute(imageSteganography);
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
                textView.setText("No message found");
            else {
                if (!result.isSecretKeyWrong()) {
                    textView.setText("Decoded");
                    message.setText("" + result.getMessage());
                } else {
                    textView.setText("Wrong secret key");
                }
            }
        } else {
            textView.setText("Select Image First");
        }
    }
}
