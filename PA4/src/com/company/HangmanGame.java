package com.company;

import java.util.Vector;

public class HangmanGame {
    private int numPlayers;
    private Vector<ServerThread> players;
    private Vector<ServerThread> validPlayers;
    private String name;
    private String secretWord;
    private String guessedWord;
    private int guesses;
    private int turn;

    public HangmanGame(String name){
        this.name = name;
        players = new Vector<ServerThread>();
        guesses = 7;
        turn = 0;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public Vector<ServerThread> getPlayers() {
        return players;
    }



    public String getName() {
        return name;
    }

    public boolean addPlayer(ServerThread server){
        if(players.size() == numPlayers)
            return false;
        players.add(server);
        validPlayers.add(server);
        return true;
    }


    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }

    public String getGuessedWord() {
        return guessedWord;
    }

    public void setGuessedWord(String guessedWord) {
        this.guessedWord = guessedWord;
    }

    public int getGuesses() {
        return guesses;
    }

    public void lowerGuesses(){
        guesses-=1;
    }

    public int getTurn() {
        synchronized (this) {
            return turn;
        }
    }

    public void nextTurn() {
        this.turn = ((this.turn+1)% players.size());
    }

    public String replace(String letter, int index) {
        this.guessedWord=guessedWord.substring(0, index * 2) + letter.toUpperCase() + guessedWord.substring(index * 2 + 1);
        return guessedWord;
    }
}
