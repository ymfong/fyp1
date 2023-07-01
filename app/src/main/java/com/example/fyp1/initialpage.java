package com.example.fyp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.ImageSteganography.TextDecoding;
import com.example.fyp1.ImageSteganography.TextDecodingCallback;
import com.example.fyp1.ImageSteganography.d;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.security.NoSuchAlgorithmException;

public class initialpage extends AppCompatActivity implements TextDecodingCallback {

    StorageReference mStorageRef;
    DatabaseReference mdatabaseRef;
    SHA encoding;
    String path, temp;

    private Uri filepath;
    //Bitmap
    private Bitmap original_image;

    Button show, back;
    String backup;

    TextView backoupoutput;
    EditText backupkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialpage);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        backoupoutput = findViewById(R.id.textView40);

        //find image name
        path = "admin";
        try
        {
            //path = encoding.toHexString(encoding.getSHA(path));
            //temp = encoding.toHexString(encoding.getSHA("ENCODED_key"));
            path = encoding.toHexString(encoding.getSHA(MainActivity.currentuser.replace("@","").replace(".","")));
            temp = encoding.toHexString(encoding.getSHA("ENCODED"+MainActivity.currentuser+"."));
        }
        // For specifying wrong message digest algorithms
        catch ( NoSuchAlgorithmException e ) {
            System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
        }

        //read profile pic
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mStorageRef.child(path).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                Log.e(">>>>>>.","no way2");
                for(StorageReference file:listResult.getItems()){
                    if(file.getName().equals(temp)){
                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final long ONE_MEGABYTE = 1024 * 1024;
                                file.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Log.e(">>>>>>2","no way2");
                                        // Data for "images/island.jpg" is returns, use this as needed
                                        original_image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        //imageView.setImageBitmap(original_image);
                                        filepath = uri;

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

        //Decode Button
        show = findViewById(R.id.start);
        backupkey = findViewById(R.id.backupkey);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(backupkey.getText().toString().isEmpty()){
                    backupkey.setError("Secret Key Is Required!");return;
                }

                if (filepath != null) {

                    //Making the ImageSteganography object
                    ImageSteganography imageSteganography = new ImageSteganography(backupkey.getText().toString(), original_image);

                    //Making the TextDecoding object
                    TextDecoding textDecoding = new TextDecoding(initialpage.this, initialpage.this);

                    //Execute Task
                    textDecoding.execute(imageSteganography);
                }
            }
        });

        //back
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), profilepage.class));
            }
        });
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
                Toast.makeText(initialpage.this, "No message found", Toast.LENGTH_SHORT).show();
            else {
                if (!result.isSecretKeyWrong()) {
                    Toast.makeText(initialpage.this, "Done", Toast.LENGTH_SHORT).show();
                    backup = result.getMessage();
                    String[] strParts = backup.split( "\\s*,\\s*" );
                    backoupoutput.setText("Name : "+strParts[0]+"\nEmail : "+strParts[1]+"\nPhone : "+strParts[2]+"\nBalance : RM"+strParts[3]);
                } else {
                    Toast.makeText(initialpage.this, "Wrong Key", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(initialpage.this, "Source not found", Toast.LENGTH_SHORT).show();
        }
    }
}