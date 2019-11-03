package com.cs201.model;

import javax.persistence.*;

@Entity
public class Junction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int timeStamp;


    private int userID;
    private String bookID;

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }
}
