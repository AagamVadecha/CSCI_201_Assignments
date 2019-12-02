package com.company;

public class Account {
    private String username;
    private String password;
    private int wins;
    private int losses;
    private boolean lost;
    private int accountID;

    public Account(){
        lost = false;
    }

    public int getAccountID(){
        return accountID;
    }

    public void setAccountID(int accountID){
        this.accountID = accountID;
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

    public void incrementLoss(){
        losses+=1;
    }
    public void incrementWin() {
        wins +=1;
    }

    public String getWinLoss(){
        return getUsername() + "'s Record\n--------\nWins - " + wins + "\nLosses - " + losses;
    }

    public boolean hasLost(){
        return lost;
    }
    public void lose() {
        lost = true;
    }
}
