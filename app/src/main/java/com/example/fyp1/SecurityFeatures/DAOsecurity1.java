package com.example.fyp1.SecurityFeatures;

import androidx.annotation.NonNull;

import com.example.fyp1.DAOUser;
import com.example.fyp1.MainActivity;
import com.example.fyp1.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DAOsecurity1 {
    private FirebaseDatabase db;
    private DatabaseReference databaseReference;
    private List<Security1> s1 = new ArrayList<>();

    public interface DataStatus {
        void DataIsLoaded(List<Security1> s1, List<String> keys);

        void DataIsInserted();

        void DataIsUpdated();

        void DataIsDeleted();
    }

    public DAOsecurity1(){
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("security1");
    }

    public void readSecurity1(final DataStatus dataStatus){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                s1.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    Security1 t1;
                    t1 = keyNode.getValue(Security1.class);
                    s1.add(t1);
                }
                dataStatus.DataIsLoaded(s1, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addSecurity1(Security1 s1, final DataStatus dataStatus){
        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(s1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsInserted();
                    }
                });
    }

    public void updateSecurity1(String key, Security1 s1, final DataStatus dataStatus){
        databaseReference.child(key).setValue(s1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsUpdated();
                    }
                });
    }
    public void deleteSecurity1(String key, final DataStatus dataStatus){
        databaseReference.child(key).setValue(null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsUpdated();
                    }
                });
    }
}
