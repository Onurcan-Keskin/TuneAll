package com.onurcankeskin.looneytune.Model;

public class UserModel {

    private String name;
    private String image;
    private String thumb_image;
    private String status;
    private String email;
    private String membership;
    private String signinMethod;

    public UserModel(){}

    public UserModel(String name, String image, String thumb_image, String status,
                     String email, String membership, String signinMethod) {
        this.name = name;
        this.image = image;
        this.thumb_image = thumb_image;
        this.status = status;
        this.email = email;
        this.membership = membership;
        this.signinMethod = signinMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public String getSigninMethod() {
        return signinMethod;
    }

    public void setSigninMethod(String signinMethod) {
        this.signinMethod = signinMethod;
    }
}
