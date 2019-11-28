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

    public void run() {
        try {
            while (true) {
                String line = br.readLine();

                if (line != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                    timeFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                    String timestamp = timeFormat.format(new Date());

                    if (line.equalsIgnoreCase("LOGIN")) {
                        account.setUsername(br.readLine());
                        account.setPassword(br.readLine());
                        System.out.println(timestamp + " " + account.getUsername() + " - trying to log in with password " + account.getPassword() + ".");

                        ps = conn.prepareStatement("SELECT * FROM User WHERE accountname = ?");
                        ps.setString(1, account.getUsername());

                        rs = ps.executeQuery();
                        if (rs.next()) {
                            if (account.getPassword().equalsIgnoreCase(rs.getString("password"))) {
                                System.out.println(timestamp + " " + account.getUsername() + " - successfully logged in.");
                                account.setWins(rs.getInt("numWins"));
                                account.setLosses(rs.getInt("numLosses"));
                                System.out.println(timestamp + " " + account.getUsername() + " - has record " + account.getWins() + " wins and " + account.getLosses() + " losses.");
                                pw.println("SUCCESSFULLY LOGGED IN");
                                pw.println(account.winLoss());
                            } else {
                                System.out.println(timestamp + " " + account.getUsername() + " - has an account but not successfully logged in.");
                                pw.println("INCORRECT PASSWORD");
                            }
                        } else {
                            System.out.println(timestamp + " " + account.getUsername() + " - does not have an account, so not successfully logged in.");
                            pw.println("INVALID USER");
                        }
                    } else if (line.equalsIgnoreCase("NEW ACCOUNT")) {
                        account.setUsername(br.readLine());
                        account.setPassword(br.readLine());
                        System.out.println(timestamp + " " + account.getUsername() + " - created an account with password " + account.getPassword() + ".");

                        ps = conn.prepareStatement("INSERT INTO User(accountname, password, wins, losses) VALUES (?,?,?,?)");
                        ps.setString(1, account.getUsername());
                        ps.setString(2, account.getPassword());
                        ps.setInt(3, 0);
                        ps.setInt(4, 0);
                        ps.executeUpdate();

                        System.out.println(timestamp + " " + account.getUsername() + " - has record 0 wins and 0 losses.");
                        account.setWins(0);
                        account.setLosses(0);

                        pw.println("SUCCESSFULLY LOGGED IN");
                        pw.println(account.winLoss());
                    } else if (line.equals("START GAME")) {
                        String gameName = br.readLine();
                        System.out.println(timestamp + " " + account.getUsername() + " - wants to start a game called " + gameName + ".");

                        if (server.containsGame(gameName)) {
                            System.out.println(timestamp + " " + account.getUsername() + " - " + gameName + " already exists, so unable to start " + gameName + ".");
                            pw.println("START UNSUCCESSFUL - GAME ALREADY EXISTS");
                        } else {
                            this.game = new HangmanGame(gameName);
                            account.setUserID(0);
                            pw.println("START SUCCESSFUL");
                        }
                    } else if (line.equals("NUMBER OF PLAYERS")) {
                        System.out.println(timestamp + " " + account.getUsername() + " - successfully started game " + game.getName() + ".");
                        game.addPlayer(this);
                        game.setNumPlayers(br.read() - '0');
                        server.gameStart(game.getName(), game);

                        System.out.println(timestamp + " " + account.getUsername() + " - " + game.getName() + " needs " + game.getNumPlayers() + " to start game.");
                        if (game.getPlayers().size() == game.getNumPlayers()) {
                            System.out.println(timestamp + " " + account.getUsername() + " - " + game.getName() + " has " + game.getNumPlayers() + " player(s) so starting game. " +
                                    "Secret word is " + game.getSecretWord() + ".");
                            game.setGuessedWord(new String(new char[game.getSecretWord().length()]).replace("\0", "_ "));

                            server.notify("ALL USERS HAVE JOINED", game.getName(), this);
                        } else {
                            server.notify("WAITING FOR USER(S) TO JOIN", game.getName(), this);
                        }
                    } else if (line.equals("JOIN GAME")) {
                        String gameName = br.readLine();
                        System.out.println(timestamp + " " + account.getUsername() + " - wants to join a game called " + gameName + ".");

                        if (server.containsGame(gameName)) {
                            if (server.addPlayer(gameName, this)) {
                                System.out.println(timestamp + " " + account.getUsername() + " - successfully joined " + gameName + ".");
                                this.game = server.getGame(gameName);
                                account.setUserID(game.getPlayers().size() - 1);
                                server.notify("JOIN SUCCESSFUL", game.getName(), this);

                                if (game.getPlayers().size() == game.getNumPlayers()) {
                                    System.out.println(timestamp + " " + account.getUsername() + " - " + game.getName() + " has " + game.getNumPlayers() + " player(s) so starting game. " +
                                            "Secret word is " + game.getSecretWord() + ".");
                                    game.setGuessedWord(new String(new char[game.getSecretWord().length()]).replace("\0", "_ "));

                                    server.notify("ALL USERS HAVE JOINED", game.getName(), this);
                                } else {
                                    server.notify("WAITING FOR USER(S) TO JOIN", game.getName(), this);
                                }
                            } else {
                                System.out.println(timestamp + " " + account.getUsername() + " - " + gameName + " exists, but " +
                                        account.getUsername() + " unable to join because maximum number of players have already joined " + gameName + ".");
                                pw.println("JOIN UNSUCCESSFUL - GAME IS FULL");
                            }
                        } else {
                            pw.println("JOIN UNSUCCESSFUL - GAME DOES NOT EXIST");
                        }
                    } else if (line.equals("GUESS - LETTER")) {
                        this.guess = br.readLine();
                        System.out.println(timestamp + " " + account.getUsername() + " - guessed letter '" + guess + "'.");
                        server.notify(line, game.getName(), this);

                        int index = game.getSecretWord().indexOf(guess.toLowerCase());
                        if (index != -1) {
                            game.replace(guess, index);
                            String positions = String.valueOf(index);
                            while (index != -1) {
                                index = game.getSecretWord().indexOf(guess, index + 1);
                                if (index != -1) {
                                    game.replace(guess, index);
                                    positions += ", " + index;
                                }
                            }

                            System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is in '" + game.getSecretWord() + "' in position(s) " + positions +
                                    ". Secret word now shows " + game.getGuessedWord() + ".");

                            if (game.getGuessedWord().indexOf("_") == -1) {
                                String opponents = "";
                                for (ServerThread player : game.getPlayers()) {
                                    if (player == this) {
                                        account.setWins(account.getWins() + 1);

                                        ps = conn.prepareStatement("UPDATE User SET numWins = ? WHERE accountname = ?");
                                        ps.setInt(1, account.getWins());
                                        ps.setString(2, account.getUsername());
                                        ps.executeUpdate();
                                    } else {
                                        Account opponent = player.getUser();
                                        opponent.setLosses(opponent.getLosses() + 1);

                                        ps = conn.prepareStatement("UPDATE User SET numLosses = ? WHERE accountname = ?");
                                        ps.setInt(1, opponent.getLosses());
                                        ps.setString(2, opponent.getUsername());
                                        ps.executeUpdate();

                                        opponents += opponent.getUsername() + " ";
                                    }
                                }
                                System.out.println(timestamp + " " + account.getUsername() + " - guessed the last letter and wins the game. " +
                                        opponents + "have lost the game.");
                                server.notify("LAST LETTER GUESSED", game.getName(), this);
                                server.removeGame(game.getName());
                            } else {
                                server.notify("LETTER - CORRECT GUESS", game.getName(), this);
                            }
                        } else {
                            game.lowerGuesses();
                            String pluralGuess = game.getGuesses() == 1 ? " guess " : " guesses ";
                            System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is not in '" + game.getSecretWord() + "'. " +
                                    game.getName() + " now has " + game.getGuesses() + pluralGuess + "remaining.");

                            if (game.getGuesses() == 0) {
                                System.out.println(timestamp + " " + account.getUsername() + " - no guesses remaining. All players have lost the game.");
                                for (ServerThread player : game.getPlayers()) {
                                    Account account = player.getUser();
                                    account.setLosses(account.getLosses() + 1);

                                    ps = conn.prepareStatement("UPDATE User SET numLosses = ? WHERE accountname = ?");
                                    ps.setInt(1, account.getLosses());
                                    ps.setString(2, account.getUsername());
                                    ps.executeUpdate();
                                }
                                server.notify("NO GUESSES REMAINING", game.getName(), this);
                                server.removeGame(game.getName());
                            } else {
                                server.notify("LETTER - INCORRECT GUESS", game.getName(), this);
                            }
                        }
                    } else if (line.equals("GUESS - WORD")) {
                        this.guess = br.readLine();
                        System.out.println(timestamp + " " + account.getUsername() + " - guessed word '" + guess + "'.");
                        server.notify(line, game.getName(), this);

                        if (guess.toLowerCase().equals(game.getSecretWord().toLowerCase())) {
                            String opponents = "";
                            for (ServerThread player : game.getPlayers()) {
                                if (player == this) {
                                    account.setWins(account.getWins() + 1);

                                    ps = conn.prepareStatement("UPDATE User SET numWins = ? WHERE accountname = ?");
                                    ps.setInt(1, account.getWins());
                                    ps.setString(2, account.getUsername());
                                    ps.executeUpdate();
                                } else {
                                    Account opponent = player.getUser();
                                    opponent.setLosses(opponent.getLosses() + 1);

                                    ps = conn.prepareStatement("UPDATE User SET numLosses = ? WHERE accountname = ?");
                                    ps.setInt(1, opponent.getLosses());
                                    ps.setString(2, opponent.getUsername());
                                    ps.executeUpdate();

                                    opponents += opponent.getUsername() + " ";
                                }
                            }
                            System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is correct. " +
                                    account.getUsername() + " wins the game. " + opponents + "have lost the game.");
                            server.notify("WORD - CORRECT GUESS", game.getName(), this);
                            server.removeGame(game.getName());
                        } else {
                            System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is incorrect. " +
                                    account.getUsername() + " has lost and is no longer in the game.");
                            game.lowerGuesses();
                            account.lose();
                            server.notify("WORD - INCORRECT GUESS", game.getName(), this);

                            if (game.getGuesses() == 0) {
                                System.out.println(timestamp + " " + account.getUsername() + " - no guesses remaining. All players have lost the game.");
                                for (ServerThread player : game.getPlayers()) {
                                    Account account = player.getUser();
                                    account.setLosses(account.getLosses() + 1);

                                    ps = conn.prepareStatement("UPDATE User SET numLosses = ? WHERE accountname = ?");
                                    ps.setInt(1, account.getLosses());
                                    ps.setString(2, account.getUsername());
                                    ps.executeUpdate();
                                }
                                server.notify("NO GUESSES REMAINING", game.getName(), this);
                                server.removeGame(game.getName());
                            } else {
                                boolean gameOver = true;
                                for (ServerThread player : game.getPlayers()) {
                                    if (!player.getUser().hasLost()) {
                                        gameOver = false;
                                    }
                                }

                                if (gameOver) {
                                    System.out.println(timestamp + " " + account.getUsername() + " - no players remaining. All players have lost the game.");
                                    for (ServerThread player : game.getPlayers()) {
                                        Account account = player.getUser();
                                        account.setLosses(account.getLosses() + 1);

                                        ps = conn.prepareStatement("UPDATE User SET numLosses = ? WHERE accountname = ?");
                                        ps.setInt(1, account.getLosses());
                                        ps.setString(2, account.getUsername());
                                        ps.executeUpdate();
                                    }
                                    server.notify("NO PLAYERS REMAINING", game.getName(), this);
                                    server.removeGame(game.getName());
                                } else {
                                    server.notify("CONTINUE GAME", game.getName(), this);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println("SQLE: " + sqle.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    public Account getUser() {
        return account;
    }

    public String getGuess() {
        return guess;
    }

    public void send(String message) {
        pw.println(message);
    }

    public void send(int num) {
        pw.println(num);
    }
}

