package com.example.securechat;

import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {
    private String email;
    private String id;
    private String imageURl;
    private String search;
    private String status;
    private String username;
    private String publicKey;

    public User() {
    }

    public User(String email, String id, String imageURl, String search, String status, String username, String publicKey) {
        this.email = email;
        this.id = id;
        this.imageURl = imageURl;
        this.search = search;
        this.status = status;
        this.username = username;
        this.publicKey = publicKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageURl() {
        return imageURl;
    }

    public void setImageURl(String imageURl) {
        this.imageURl = imageURl;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
