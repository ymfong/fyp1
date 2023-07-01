package com.example.fyp1;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DAOtransaction {
    private FirebaseDatabase db;
    private DatabaseReference databaseReference;
    private List<Transaction> t = new ArrayList<>();
    Transaction transaction;

    public interface DataStatus {
        void DataIsLoaded(List<Transaction> t, List<String> keys);

        void DataIsInserted();

        void DataIsUpdated();

        void DataIsDeleted();
    }

    public DAOtransaction(){
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("transaction");
    }

    public void readtransaction(final DataStatus dataStatus){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                t.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    Transaction t1;
                    t1 = keyNode.getValue(Transaction.class);
                    t.add(t1);
                }
                dataStatus.DataIsLoaded(t, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addTransaction(Transaction t, final DataStatus dataStatus){
        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(t)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsInserted();
                    }
                });
    }

    public void readtransaction2(final DataStatus dataStatus){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                t.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : dataSnapshot.getChildren()){
                    transaction = keyNode.getValue(Transaction.class);
                    if(transaction.getSender_no().equals(MainActivity.currentuser)){
                        transaction.setAmount("-"+transaction.getAmount());
                        t.add(transaction);
                        keys.add(keyNode.getKey());
                    }
                    if(transaction.getReceiver_no().equals(MainActivity.currentuser)){
                        t.add(transaction);
                        transaction.setAmount("+"+transaction.getAmount());
                        keys.add(keyNode.getKey());
                    }
                }
                dataStatus.DataIsLoaded(t, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
