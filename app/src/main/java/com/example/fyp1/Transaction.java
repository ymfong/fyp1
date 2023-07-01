package com.example.fyp1;

import java.sql.Date;
import java.util.Calendar;

public class Transaction {
    private String sender_no;
    private String receiver_no;
    private String amount;
    private String date;
    private String description;

    public Transaction(){};

    public Transaction(String sender_no, String receiver_no, String amount, String date, String description) {
        this.sender_no = sender_no;
        this.receiver_no = receiver_no;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public String getSender_no() {
        return sender_no;
    }

    public void setSender_no(String sender_no) {
        this.sender_no = sender_no;
    }

    public String getReceiver_no() {
        return receiver_no;
    }

    public void setReceiver_no(String receiver_no) {
        this.receiver_no = receiver_no;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
