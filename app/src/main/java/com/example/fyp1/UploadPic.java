package com.example.fyp1;

public class UploadPic {

    String name;
    String mImageUrl;
    String imagePath;

    public UploadPic(){}

    public UploadPic(String name, String mImageUrl, String imagePath) {
        if (name.trim().equals("")){
            name = "No Name";
        }

        this.name = name;
        this.mImageUrl = mImageUrl;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
