package cit.jauc.model;

import java.io.Serializable;
import java.util.Date;

public class SupportMessage implements Serializable {

    private String id;
    private String body;
    private String response;
    private Date date;
    private String email;
    private String photoUrl;
    private boolean read;
    private String userId;
    private String userName;


    public SupportMessage() {
        this.setBody("");
        this.setResponse(null);
        this.setDate(new Date());
        this.setEmail("");
        this.setPhotoUrl("");
        this.setRead(false);
        this.setUserId("");
        this.setUserName("");
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
