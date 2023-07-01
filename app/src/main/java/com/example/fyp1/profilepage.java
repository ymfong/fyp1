package com.example.fyp1;


import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.example.fyp1.ImageSteganography.d;
import com.example.fyp1.SecurityFeatures.security_features;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class profilepage extends AppCompatActivity {

    // add when ask to verify email
    TextView tell1, toImageSteganography;
    Button verify1;

    //add when edit profile pic
    ProgressBar progressBar;
    ShapeableImageView pic,addpic;
    Button upload;
    Uri ImageUri;
    final int PICK_IMAGE_REQUEST=1;
    StorageReference mStorageRef;
    DatabaseReference mdatabaseRef;
    StorageTask mUploadTask;
    SHA encoding;
    String userphone;
    String path, temp;
    String nameinhex;

    //add when change password or email
    EditText name, phone, email, password;
    AlertDialog.Builder reset_alert;
    LayoutInflater inflater;
    String b,userauth;
    User record;

    //add when delete account
    Button deleteaccount;
    Button back;
    String key;
    FirebaseDatabase db;
    DatabaseReference databaseReference;

    //add when ask to verify
    FirebaseAuth fAuth;

    //database
    DAOUser dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //set profile fields
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        name = findViewById(R.id.profile_n);
        phone = findViewById(R.id.profile_ph);
        email = findViewById(R.id.profile_e);
        password = findViewById(R.id.profile_p);

        //read from database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    key = keyNode.getKey();
                    User user = keyNode.getValue(User.class);
                    if(ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.currentuser)){
                        //decrypt the data
                        try {
                            //set text
                            name.setText(ImageSteganography.decryptMessage(user.getName(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                            phone.setText(ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                            email.setText(ImageSteganography.decryptMessage(user.getEmail(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                            password.setText("******");
                            b = user.getBalance();
                            userauth = ImageSteganography.decryptMessage(MainActivity.userauth,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                            //userphone = ImageSteganography.decryptMessage(user.getPhone(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        name.setEnabled(false);
        phone.setEnabled(false);
        //email.setEnabled(false);
        //password.setEnabled(false);

        //add when ask to verify after login by email
        back = findViewById(R.id.back);
        fAuth = FirebaseAuth.getInstance();
        try {
            userauth = ImageSteganography.decryptMessage(MainActivity.userauth,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey));
            } catch (Exception exception) {
            exception.printStackTrace();
        }

        tell1 = findViewById(R.id.tell);
        verify1 = findViewById(R.id.verify);

        fAuth.signInWithEmailAndPassword(MainActivity.currentuser, userauth).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.e("??","inside ady"+fAuth.getCurrentUser().getEmail());
                if(!fAuth.getCurrentUser().isEmailVerified()){
                    back.setEnabled(false);
                    tell1 = findViewById(R.id.tell);
                    verify1 = findViewById(R.id.verify);
                    tell1.setVisibility(View.VISIBLE);
                    verify1.setVisibility(View.VISIBLE);

                    verify1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(profilepage.this, "Verification Email Send!", Toast.LENGTH_SHORT).show();
                                    tell1.setVisibility(View.GONE);
                                    verify1.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(profilepage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else{
                    back.setEnabled(true);
                    //startActivity(new Intent(getApplicationContext(), mainmenu.class));
                    //finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(profilepage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        });

        //go image steganography
        toImageSteganography = findViewById(R.id.textViewToImageSteganography);
        toImageSteganography.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), security_features.class));
            }
        });

        //add when add profile picture
        pic = findViewById(R.id.profilepic);
        addpic = findViewById(R.id.addprofilepicture);
        progressBar = findViewById(R.id.progressBar);
        upload = findViewById(R.id.upload);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mdatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        //find image name
        path = MainActivity.ph_otp.replace("+","");
        try
        {
            path = encoding.toHexString(encoding.getSHA(path));
            temp = encoding.toHexString(encoding.getSHA(MainActivity.ph_otp+"."));
        }
        // For specifying wrong message digest algorithms
        catch ( NoSuchAlgorithmException e ) {
            System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
        }

        //read profile pic
        mdatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    UploadPic up = keyNode.getValue(UploadPic.class);
                    try{
                        nameinhex = encoding.toHexString(encoding.getSHA(MainActivity.currentuser));;
                    }
                    catch (Exception e){

                    }
                    if(up.getName().equals(nameinhex)){
                        //real_pic = up;
                        //Log.e(">>>>>>.","no way");
                        mStorageRef.child(path).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                //Log.e(">>>>>>.","no way2");
                                for(StorageReference file:listResult.getItems()){
                                    if(file.getName().equals(temp)){
                                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Picasso.with(profilepage.this).load(uri.toString()).into(pic);
                                                pic.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                                //pic.setImageURI(uri);
                                                //Log.e("Itemvalue1",uri.toString());
                                            }
                                        });
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profilepage.this, error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        addpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //browser local storage to upload image
                progressBar.setVisibility(View.VISIBLE);

                openFileChooser();
                //addpic.setVisibility(View.GONE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setVisibility(View.GONE);
                addpic.setVisibility(View.VISIBLE);

                if (mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(profilepage.this, "Upload in Progress", Toast.LENGTH_SHORT).show();
                }else{
                    uploadFile();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


        //add when change password or email
        dao = new DAOUser();
        reset_alert = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = inflater.inflate(R.layout.reset_pop, null);

                reset_alert.setTitle("Update Email?")
                        .setMessage("Enter New Email Address.")
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText reset_ep = v.findViewById(R.id.e_tochange);
                                String r_ep = reset_ep.getText().toString();

                                if(r_ep.isEmpty()){
                                    reset_ep.setError("New Email Required!");
                                    return;
                                }

                                //read database first for further update
                                //find which user first
                                //String past_email = user.getEmail().toString();
                                record = new User();


                                //user = fAuth.getCurrentUser();
                                //Log.d("dsfs","sdf");
                                fAuth.getCurrentUser().updateEmail(r_ep).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        try {
                                        //read newest email
                                            record.setName(ImageSteganography.encryptMessage(name.getText().toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                            record.setPhone(ImageSteganography.encryptMessage(phone.getText().toString(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                            record.setEmail(ImageSteganography.encryptMessage(r_ep,ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                                            record.setBalance(b);
                                        } catch (Exception exception) {
                                            exception.printStackTrace();
                                        }

                                        //change email in database
                                        new DAOUser().updateUser(key, record, new DAOUser.DataStatus() {
                                            @Override
                                            public void DataIsLoaded(List<User> users, List<String> keys) {

                                            }

                                            @Override
                                            public void DataIsInserted() {

                                            }

                                            @Override
                                            public void DataIsUpdated() {
                                                Toast.makeText(profilepage.this, "Loading..", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void DataIsDeleted() {

                                            }
                                        });

                                        //redirect to login again
                                        //Log.d("profile","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+r_ep);
                                        Toast.makeText(profilepage.this, "Email Updated!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(profilepage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", null)
                        .setView(v)
                        .create().show();
            }
        });

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), reset_password.class));
                finish();
            }
        });

        //delete account
        //add when delete email
        deleteaccount = findViewById(R.id.deleteaccount);
        deleteaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reset_alert.setTitle("Delete Account?")
                        .setMessage("Are You Sure ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                fAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //delete from database
                                        new DAOUser().deleteUser(key, new DAOUser.DataStatus() {
                                            @Override
                                            public void DataIsLoaded(List<User> users, List<String> keys) {

                                            }

                                            @Override
                                            public void DataIsInserted() {

                                            }

                                            @Override
                                            public void DataIsUpdated() {

                                            }

                                            @Override
                                            public void DataIsDeleted() {
                                                Toast.makeText(profilepage.this, "Loading..", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        Toast.makeText(profilepage.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                        fAuth.signOut();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(profilepage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("No", null)
                        .create()
                        .show();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), mainmenu.class));
                finish();
            }
        });
    }

    //add when upload profile picture
    private void openFileChooser() {
        Intent intentimage = new Intent();
        intentimage.setType("image/*");
        intentimage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentimage,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
        && data!=null && data.getData()!=null){
            ImageUri = data.getData();
            pic.setImageURI(ImageUri);

            upload.setVisibility(View.VISIBLE);
        }else{
            upload.setVisibility(View.GONE);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(ImageUri != null){

            try
            {
                StorageReference fileReference = mStorageRef
                        .child(encoding.toHexString(encoding.getSHA(MainActivity.ph_otp.replace("+",""))))
                        .child(encoding.toHexString(encoding.getSHA(MainActivity.ph_otp+".")));

                mUploadTask = fileReference.putFile(ImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(0);
                                    }
                                }, 500);

                                Toast.makeText(profilepage.this, "Upload successful", Toast.LENGTH_LONG).show();

                                String a = MainActivity.currentuser.trim()+"."+getFileExtension(ImageUri);
                                String b = MainActivity.currentuser.trim();
                                try
                                {
                                    a = encoding.toHexString(encoding.getSHA(a));
                                    b = encoding.toHexString(encoding.getSHA(b));
                                }
                                // For specifying wrong message digest algorithms
                                catch ( NoSuchAlgorithmException e ) {
                                    System.out.println( " Exception thrown for incorrect algorithm : " + e ) ;
                                }

                                UploadPic uploadPic = new UploadPic(b,
                                        taskSnapshot.getMetadata().getReference().getDownloadUrl().toString(),
                                        a);
                                String uploadId = mdatabaseRef.push().getKey();
                                mdatabaseRef.child(uploadId).setValue(uploadPic);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(profilepage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressBar.setProgress((int)progress);
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
}