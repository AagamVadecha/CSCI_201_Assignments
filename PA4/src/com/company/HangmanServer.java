package com.company;


import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class HangmanServer {
    private ConcurrentHashMap<String, HangmanGame> games;
    private Vector<ServerThread> serverThreadVector;
    private List<String> words;

    public HangmanServer(int port, Connection conn) {
        try {
            ServerSocket ss = new ServerSocket(port);
            serverThreadVector = new Vector<ServerThread>();
            games = new ConcurrentHashMap<String, HangmanGame>();
            while (true) {
                Socket s = ss.accept();
                ServerThread st = new ServerThread(this, s, conn);
                serverThreadVector.add(st);
            }
        } catch (Exception e) {
            System.out.println("Error initializing Hangman Server " + e.getMessage());
        }
    }

    public void gameStart(String name, HangmanGame game){
        game.
    }

}
