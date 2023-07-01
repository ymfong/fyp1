package com.example.fyp1.SecurityFeatures;

public class Security1 {
    private String email;
    private String onoff;
    private String question1;
    private String ans1;
    private String question2;
    private String ans2;

    public Security1() {}

    public Security1(String email, String onoff, String question1, String ans1, String question2, String ans2) {
        this.email = email;
        this.onoff = onoff;
        this.question1 = question1;
        this.ans1 = ans1;
        this.question2 = question2;
        this.ans2 = ans2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQuestion1() {
        return question1;
    }

    public void setQuestion1(String question1) {
        this.question1 = question1;
    }

    public String getAns1() {
        return ans1;
    }

    public void setAns1(String ans1) {
        this.ans1 = ans1;
    }

    public String getQuestion2() {
        return question2;
    }

    public void setQuestion2(String question2) {
        this.question2 = question2;
    }

    public String getAns2() {
        return ans2;
    }

    public void setAns2(String ans2) {
        this.ans2 = ans2;
    }

    public String getOnoff() {
        return onoff;
    }

    public void setOnoff(String onoff) {
        this.onoff = onoff;
    }
}
