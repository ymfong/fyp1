package com.example.fyp1;

import static android.widget.Toast.LENGTH_SHORT;


import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DAOUser {
    private FirebaseDatabase db;
    private DatabaseReference databaseReference;
    private List<User> users = new ArrayList<>();

    //add when check email, name, phone
    private User user;

    private boolean exist;
    String tocompare, name="name", phone="phone";

    public interface DataStatus {
        void DataIsLoaded(List<User> users, List<String> keys);

        void DataIsInserted();

        void DataIsUpdated();

        void DataIsDeleted();
    }

    public DAOUser(){
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("user");
        exist = false;
    }

    public void readUsers(final DataStatus dataStatus){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    User user = keyNode.getValue(User.class);
                    users.add(user);
                }
                dataStatus.DataIsLoaded(users, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addUser(User u, final DAOUser.DataStatus dataStatus){
        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(u)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsInserted();
                    }
                });
    }

    public void updateUser(String key, User u, final DataStatus dataStatus){
        databaseReference.child(key).setValue(u)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsUpdated();
                    }
                });
    }

    public void deleteUser(String key, final DataStatus dataStatus){
        databaseReference.child(key).setValue(null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsDeleted();
                    }
                });
    }


    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public void checkdetailsUser(String email, String keyword, String k){
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                        user = keyNode.getValue(User.class);

                        if(name.equals(keyword)){
                            tocompare = user.getName();
                        }else if(phone.equals(keyword)){
                            tocompare = user.getPhone();
                        }

                        if(user.getEmail().equals(email)){
                            if(tocompare.equals(k)){
                                setExist(true);
                                break;
                            }
                        }
                        else{
                            setExist(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    setExist(false);
                }
            };

        databaseReference.addValueEventListener(valueEventListener);
    }
}
