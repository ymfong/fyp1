package com.example.fyp1.ImageSteganography;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp1.MainActivity;
import com.example.fyp1.SHA;
import com.example.fyp1.UploadPic;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class Decode implements TextDecodingCallback{

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Decode Class";

    static StorageReference mStorageRef;
    static DatabaseReference mdatabaseRef;
    static String path, temp;
    static SHA encoding;
    String secret;
    //Decode
    //Decode
    public String decode(String secret_key, Activity activity){

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mdatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        //find image name
        path = MainActivity.ph_otp.replace("+","");
        try
        {
            path = encoding.toHexString(encoding.getSHA(path));
            temp = encoding.toHexString(encoding.getSHA(MainActivity.ph_otp+".imagepassword"));
        }
        // For specifying wrong message digest algorithms
        catch ( NoSuchAlgorithmException e ) {
            System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
        }

        mStorageRef.child(path).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference file:listResult.getItems()){
                    if(file.getName().equals(temp)){
                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Bitmap original_image = getBitmapFromURL(uri.toString());
                                //Making the ImageSteganography object
                                ImageSteganography imageSteganography = new ImageSteganography(secret_key, original_image);

                                //Making the TextDecoding object
                                TextDecoding textDecoding = new TextDecoding(activity, Decode.this);

                                //Execute Task
                                textDecoding.execute(imageSteganography);
                            }
                        });
                        break;
                    }
                }
            }
        });
        return secret;
    }


    @Override
    public void onStartTextEncoding1() {

    }

    @Override
    public void onCompleteTextEncoding1(ImageSteganography result) {
        if (result != null) {
            if (!result.isDecoded())
                Log.e("decode","No message found");
            else {
                if (!result.isSecretKeyWrong()) {
                    Log.e("decode","Decoded");
                    secret = result.getMessage();
                } else {
                    Log.e("decode","Wrong secret key");
                }
            }
        } else {
            Log.e("decode","Select Image First");
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}
