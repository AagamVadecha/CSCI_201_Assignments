package com.company;

public class Account {
    private String username;
    private String password;
    private int wins;
    private int losses;
    private boolean lost;
    private int userID;

    public Account(){
        lost = false;
    }

    public int getUserID(){
        return userID;
    }

    public void setUserID(int userID){
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public String winLoss(){
        return getUsername() + "'s Record\n--------------\nWins - " + wins + "\nLosses - " + losses;
    }

    public boolean hasLost(){
        return lost;
    }
    public void lose() {
        lost = true;
    }
}
