package com.cs201.model;

import javax.persistence.*;

@Entity
@Table(name="Junction")
public class Junction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeStamp", updatable = false, nullable = false)
    private int timeStamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userID", nullable = false)
    private Accounts account;

    private String bookID;


    public int getTimeStamp(){
        return timeStamp;
    }
    public Accounts getAccount() {
        return account;
    }

    public void setAccount(Accounts account) {
        this.account = account;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }
}
