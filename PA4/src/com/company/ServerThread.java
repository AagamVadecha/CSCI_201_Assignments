package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ServerThread extends Thread {

    private BufferedReader br;
    private PrintWriter pw;
    private HangmanServer server;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    private Account account = new Account();
    private HangmanGame game;
    private String guess;

    public ServerThread(Socket s, HangmanServer server, Connection conn) {
        try {
            this.server = server;
            this.conn = conn;
            pw = new PrintWriter(s.getOutputStream());
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.start();
        } catch (IOException ioe) {
            System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
        }
    }

    public void sendMessage(String message) {
        pw.println(message);
        pw.flush();
    }

    public void run() {
        try {
            while(true) {
                lock.lock();
                if(!firstThread){
                    condition.await();
                }
                else
                    firstThread = false;
                this.sendMessage("Send a message:");
                String string = br.readLine();
                while (string.isEmpty()|| !string.contains("END_OF_MESSAGE")){
                    cr.broadcast(string, this);
                    string = br.readLine();
                }
                this.sendMessage("Your turn is now over. Please wait for your next turn.");
                cr.signal(this);
                lock.unlock();
            }
        } catch (IOException | InterruptedException ioe) {
            System.out.println("ioe in ServerThread.run(): " + ioe.getMessage());
        }
    }
}
