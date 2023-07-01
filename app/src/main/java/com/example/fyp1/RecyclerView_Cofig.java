package com.example.fyp1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.ImageSteganography.Crypto;
import com.example.fyp1.ImageSteganography.ImageSteganography;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.List;

public class RecyclerView_Cofig {
    private Context nContext;
    private TransactionAdapter transactionAdapter;

    public void setConfig(RecyclerView recyclerView, Context context, List<Transaction> t ,List<String> keys){
        nContext = context;
        transactionAdapter = new TransactionAdapter(t, keys);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(transactionAdapter);
    }

    class TransactionItemView extends RecyclerView.ViewHolder{
        private TextView sender;
        private TextView datetime;
        private TextView amount;

        private String key;

        public TransactionItemView(ViewGroup parent){
            super(LayoutInflater.from(nContext).
            inflate(R.layout.list_item, parent, false));

            sender = (TextView) itemView.findViewById(R.id.sender);
            datetime = (TextView)itemView.findViewById(R.id.datetime);
            amount = (TextView)itemView.findViewById(R.id.amount);
        }
        public void bind(Transaction t, String key){
            try {
                if(ImageSteganography.decryptMessage(t.getSender_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)).equals(MainActivity.ph_otp)){
                    sender.setText("TO "+ImageSteganography.decryptMessage(t.getReceiver_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                }else{
                    sender.setText("From "+ImageSteganography.decryptMessage(t.getSender_no(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                }
                datetime.setText(ImageSteganography.decryptMessage(t.getDate(),ImageSteganography.convertKeyTo128bit(MainActivity.secretkey)));
                amount.setText(t.getAmount());
                this.key = key;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    class TransactionAdapter extends RecyclerView.Adapter<TransactionItemView>{
        private List<Transaction> tlist;
        private List<String> tkey;

        public TransactionAdapter(List<Transaction> tlist, List<String> tkey){
            this.tlist = tlist;
            this.tkey = tkey;
        }

        @Override
        public TransactionItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TransactionItemView(parent);
        }

        @Override
        public void onBindViewHolder( TransactionItemView holder, int position) {
            holder.bind(tlist.get(position), tkey.get(position));
        }

        @Override
        public int getItemCount() {
            return tlist.size();
        }
    }
}
