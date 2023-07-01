package com.example.fyp1.SecurityFeatures;

public class Security2 {
    private String email;
    private String onoff;
    private String message;

    public Security2() {}

    public Security2(String email, String onoff, String message) {
        this.email = email;
        this.onoff = onoff;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOnoff() {
        return onoff;
    }

    public void setOnoff(String onoff) {
        this.onoff = onoff;
    }
}
