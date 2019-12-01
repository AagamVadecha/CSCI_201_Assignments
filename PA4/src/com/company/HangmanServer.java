package com.company;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//
public class HangmanServer {

        private ConcurrentHashMap<String, HangmanGame> games;
    private Vector<ServerThread> serverThreadVector;
    private static List<String> words;

    public HangmanServer(int port, Connection conn) {
        try(ServerSocket ss = new ServerSocket(port)) {
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
        game.setSecretWord(words.get((int) (Math.random()*(words.size()))));
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
                if (game.getPlayers().size() < game.getNumPlayers()) {
                    game.addPlayer(thread);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public void nextPlayer(HangmanGame game, String message, String guess) {
        for (ServerThread player : game.getPlayers()) {
            int playerID = player.getAccount().getAccountID();
            player.send(message);
            if (guess != null) {
                player.send(guess);
            }
            player.send(game.getGuessedWord());
            while (game.getPlayers().get(game.getTurn()).getAccount().hasLost()) {
                game.nextTurn();
            }
            if (playerID == game.getTurn()) {
                player.send("PLAYER TURN");
                player.send(game.getGuesses());
            } else {
                player.send("WAITING FOR OPPONENT");
                player.send(game.getGuesses());
                player.send(game.getPlayers().get(game.getTurn()).getAccount().getUsername());
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
                            st.send("User " + player.getAccount().getUsername() + " is in the game.");
                            st.send(player.getAccount().getWinLoss());

                            player.send("USER JOINED");
                            player.send("User " + st.getAccount().getUsername() + " is in the game.");
                            player.send(st.getAccount().getWinLoss());
                        }
                    }
                    break;

                case "ALL USERS HAVE JOINED": case "CONTINUE GAME":
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
                            player.send(st.getAccount().getUsername());
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
                            player.send(st.getAccount().getUsername());
                        } else {
                            player.send(message);
                        }

                        for (ServerThread s : game.getPlayers()) {
                            player.send("PLAYER RECORD");
                            player.send(s.getAccount().getWinLoss());
                        }
                        player.send("GAME EXIT");
                    }
                    break;

                case "WORD - CORRECT GUESS":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            player.send("OPPONENT WIN - WORD");
                            player.send(st.getAccount().getUsername());
                        } else {
                            player.send(message);
                        }

                        for (ServerThread s : game.getPlayers()) {
                            player.send("PLAYER RECORD");
                            player.send(s.getAccount().getWinLoss());
                        }
                        player.send("GAME EXIT");
                    }
                    break;

                case "WORD - INCORRECT GUESS":
                    for (ServerThread player : game.getPlayers()) {
                        if (player != st) {
                            player.send("OPPONENT LOSE");
                            player.send(st.getAccount().getUsername());
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
                            player.send(s.getAccount().getWinLoss());
                        }
                        player.send("GAME EXIT");
                    }
                    break;
            }
        }
    }


    public static boolean containsValue(String string, String name) {
        if(string == null || string == ""){
            System.out.println(name + " is a required parameter in the configuration file.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String filename = "";
        configFileProperties properties = null;


        boolean validFile = false;
        do {
            System.out.print("What is the name of the configuration file? ");
            filename = scan.nextLine();

            try (InputStream is = new FileInputStream(filename);) {
                properties = new configFileProperties(is);
                validFile = true;
            } catch (FileNotFoundException exception) {
                System.out.println("Configuration file " + filename + " could not be found.");
            } catch (IOException ioe) {
                System.out.println("IOE: " + ioe.getMessage());
            }
        } while (!validFile);

        validFile(properties, containsValue(properties.getHostname(), "hostname"), containsValue(properties.getPort(), "port"), containsValue(properties.getConnection(), "connection"), containsValue(properties.getDBUsername(), "DBUsername"), containsValue(properties.getDBPassword(), "DBPassword"), containsValue(properties.getSecretWordFile(), "SecretWordFile"));

        words = new ArrayList<String>();
        String word = "";
        try (BufferedReader br = new BufferedReader(new FileReader(properties.getSecretWordFile()))) {
            word = br.readLine().trim();
            while (word != null) {
                words.add(word);
                word = br.readLine();
            }
        } catch (Exception e){
            System.out.println("Could not find a file");
            System.exit(0);
        }

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.print("Trying to connect to database...");
            conn = DriverManager.getConnection(properties.getConnection(), properties.getDBUsername(), properties.getDBPassword());
            System.out.println("Connected!\n");
            new HangmanServer(Integer.parseInt(properties.getPort()), conn);
        } catch (SQLException exception) {
            System.out.println("Unable to connect to database " + properties.getConnection() + " with username " + properties.getDBUsername() + " and password " + properties.getDBPassword() + ".");
        } catch (Exception exception) {
            System.out.println("Exception occurred");
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

    static void validFile(configFileProperties properties, boolean hostname, boolean port, boolean connection, boolean dbUsername, boolean dbPassword, boolean secretWordFile) {
        boolean isValidFile = true;

        isValidFile &= hostname;
        isValidFile &= port;
        isValidFile &= connection;
        isValidFile &= dbUsername;
        isValidFile &= dbPassword;
        isValidFile &= secretWordFile;

        if (isValidFile) {
            System.out.println("Server Hostname - " + properties.getHostname());
            System.out.println("Server Port - " + properties.getPort());
            System.out.println("Database Connection String - " + properties.getConnection());
            System.out.println("Database Username - " + properties.getDBUsername());
            System.out.println("Database Password - " + properties.getDBPassword());
            System.out.println("Secret Word File - " + properties.getSecretWordFile() + "\n");
        } else {
            System.exit(-1);
        }
    }
}


