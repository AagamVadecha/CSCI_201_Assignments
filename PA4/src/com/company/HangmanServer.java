package com.company;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HangmanServer {
    private ConcurrentHashMap<String, HangmanGame> games;
    private Vector<ServerThread> serverThreadVector;
    private static List<String> words;

    public HangmanServer(int port, Connection conn) {
        try {
            ServerSocket ss = new ServerSocket(port);
            serverThreadVector = new Vector<ServerThread>();
            games = new ConcurrentHashMap<String, HangmanGame>();
            while (true) {
                Socket s = ss.accept();
                ServerThread st = new ServerThread(s, this, conn);
                serverThreadVector.add(st);
            }
        } catch (Exception e) {
            System.out.println("Error initializing Hangman Server " + e.getMessage());
        }
    }

    public void gameStart(String name, HangmanGame game){
        game.setSecretWord(words.get((int) (Math.random()*(words.size()+1))));
        games.put(name.toLowerCase(), game);
    }

    public void removeGame(String name) {
        games.remove(name.toLowerCase());
    }

    public HangmanGame getGame(String name) {
        return games.get(name.toLowerCase());
    }

    public boolean containsGame(String name){
        return (!(games==null) && !games.isEmpty() && games.containsKey(name.toLowerCase()));
    }

    public boolean addPlayer(String name, ServerThread thread){
        if(games == null || games.isEmpty())
            return false;
        else{
            HangmanGame game = games.get(name.toLowerCase());
            synchronized(game){
                return game.addPlayer(thread);
            }
        }
    }

    public void nextPlayer(HangmanGame game, String message, String guess) {
        for (ServerThread player : game.getPlayers()) {
            int playerID = player.getUser().getUserID();
            player.send(message);
            if (guess != null) {
                player.send(guess);
            }
            player.send(game.getGuessedWord());
            while (game.getPlayers().get(game.getTurn()).getUser().hasLost()) {
                game.nextTurn();
            }
            if (playerID == game.getTurn()) {
                player.send("PLAYER TURN");
                player.send(game.getGuesses());
            } else {
                player.send("WAITING FOR OPPONENT");
                player.send(game.getGuesses());
                player.send(game.getPlayers().get(game.getTurn()).getUser().getUsername());
            }
        }
        game.nextTurn();
    }

    public void notify(String message, String name, ServerThread st) {
        if (message != null) {
            HangmanGame game = games.get(name.toLowerCase());
            switch(message) {
                case "JOIN SUCCESSFUL":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            st.send(message);
                            st.send("User " + player.getUser().getUsername() + " is in the game.");
                            st.send(player.getUser().winLoss());

                            player.send("USER JOINED");
                            player.send("User " + st.getUser().getUsername() + " is in the game.");
                            player.send(st.getUser().winLoss());
                        }
                    }
                    break;

                case "ALL USERS HAVE JOINED":
                    nextPlayer(game, message, null);
                    break;

                case "WAITING FOR USER(S) TO JOIN":
                    for (ServerThread player : game.getPlayers()) {
                        player.send(message);
                        player.send(game.getNumPlayers() - game.getPlayers().size());
                    }
                    break;

                case "GUESS - LETTER": case "GUESS - WORD":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            player.send(message);
                            player.send(st.getUser().getUsername());
                            player.send(st.getGuess());
                        }
                    }
                    break;

                case "LETTER - CORRECT GUESS": case "LETTER - INCORRECT GUESS":
                    nextPlayer(game, message, st.getGuess());
                    break;

                case "LAST LETTER GUESSED":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            player.send("OPPONENT WIN - LETTER");
                            player.send(st.getUser().getUsername());
                        } else {
                            player.send(message);
                        }

                        for (ServerThread s : game.getPlayers()) {
                            player.send("PLAYER RECORD");
                            player.send(s.getUser().winLoss());
                        }
                        player.send("GAME EXIT");
                    }
                    break;

                case "WORD - CORRECT GUESS":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            player.send("OPPONENT WIN - WORD");
                            player.send(st.getUser().getUsername());
                        } else {
                            player.send(message);
                        }

                        for (ServerThread s : game.getPlayers()) {
                            player.send("PLAYER RECORD");
                            player.send(s.getUser().winLoss());
                        }
                        player.send("GAME EXIT");
                    }
                    break;

                case "WORD - INCORRECT GUESS":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            player.send("OPPONENT LOSE");
                            player.send(st.getUser().getUsername());
                        } else {
                            player.send(message);
                        }
                    }
                    break;

                case "NO GUESSES REMAINING": case "NO PLAYERS REMAINING":
                    for (ServerThread player : game.getPlayers()) {
                        player.send(message);
                        player.send(game.getSecretWord());

                        for (ServerThread s : game.getPlayers()) {
                            player.send("PLAYER RECORD");
                            player.send(s.getUser().winLoss());
                        }
                        player.send("GAME EXIT");
                    }
                    break;

                case "CONTINUE GAME":
                    nextPlayer(game, message, null);
                    break;
            }
        }
    }


    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String filename = "";
        String hostname = "";
        String port = "";
        String connection = "";
        String DBUsername = "";
        String DBPassword = "";
        String wordFile = "";


        boolean validFile = false;
        do {
            System.out.print("What is the name of the configuration file? ");
            filename = scan.nextLine();

            try (InputStream is = new FileInputStream(filename);) {
                Properties configFile = new Properties();
                if (is != null) {
                    configFile.load(is);
                }

                System.out.println("\nReading config file...");
                hostname = configFile.getProperty("ServerHostname");
                port = configFile.getProperty("ServerPort");
                connection = configFile.getProperty("DBConnection");
                DBUsername = configFile.getProperty("DBUsername");
                DBPassword = configFile.getProperty("DBPassword");
                wordFile = configFile.getProperty("SecretWordFile");
                validFile = true;
            } catch (FileNotFoundException exception) {
                System.out.println("Configuration file " + filename + " could not be found.");
            } catch (IOException ioe) {
                System.out.println("IOE: " + ioe.getMessage());
            }
        } while (!validFile);

        boolean isValid = true;
        if (hostname == null || hostname.equals("")) {
            System.out.println("ServerHostname is a required parameter in the configuration file.");
            isValid = false;
        }
        if (port == null || port.equals("")) {
            System.out.println("ServerPort is a required parameter in the configuration file.");
            isValid = false;
        }
        if (connection == null || connection.equals("")) {
            System.out.println("DBConnection is a required parameter in the configuration file.");
            isValid = false;
        }
        if (DBUsername == null || DBUsername.equals("")) {
            System.out.println("DBUsername is a required parameter in the configuration file.");
            isValid = false;
        }
        if (DBPassword == null || DBPassword.equals("")) {
            System.out.println("DBPassword is a required parameter in the configuration file.");
            isValid = false;
        }
        if (wordFile == null || wordFile.equals("")) {
            System.out.println("SecretWordFile is a required parameter in the configuration file.");
            isValid = false;
        }
        if (isValid) {
            System.out.println("Server Hostname - " + hostname);
            System.out.println("Server Port - " + port);
            System.out.println("Database Connection String - " + connection);
            System.out.println("Database Username - " + DBUsername);
            System.out.println("Database Password - " + DBPassword);
            System.out.println("Secret Word File - " + wordFile + "\n");
        } else {
            System.exit(0);
        }

        words = new ArrayList<String>();
        String word = "";
        try (BufferedReader br = new BufferedReader(new FileReader(wordFile));) {
            word = br.readLine();
            while (word != null) {
                words.add(word);
                word = br.readLine();
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("FNFE: " + fnfe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            System.out.print("Trying to connect to database...");
            conn = DriverManager.getConnection(connection, DBUsername, DBPassword);
            System.out.println("Connected!\n");

            new HangmanServer(Integer.parseInt(port), conn);
        } catch (SQLException exception) {
            System.out.println("Unable to connect to database " + connection +
                    " with username " + DBUsername + " and password " + DBPassword + ".");
        } catch (ClassNotFoundException exception) {
            System.out.println("Class Not Found Exception: " + exception.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException exception) {
                System.out.println("SQL Exception: " + exception.getMessage());
            }
        }
        scan.close();
    }


}
