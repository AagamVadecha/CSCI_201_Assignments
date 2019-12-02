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

    public ServerThread(HangmanServer server, Socket socket, Connection conn) {
        this.server = server;
        this.conn = conn;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
            this.start();
        } catch (IOException ioe) {
            System.out.println("ioe: " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = br.readLine();

                if (line != null) {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
                    format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                    String timestamp = format.format(new Date());
                    switch (line) {
                        case "LOGIN":
                            account.setUsername(br.readLine());
                            account.setPassword(br.readLine());
                            System.out.println(timestamp + " " + account.getUsername() + " - trying to log in with password " + account.getPassword() + ".");

                            ps = conn.prepareStatement("SELECT * FROM Account WHERE username = ?");
                            ps.setString(1, account.getUsername());

                            rs = ps.executeQuery();
                            if (rs.next()) {
                                if (account.getPassword().equals(rs.getString("password"))) {
                                    System.out.println(timestamp + " " + account.getUsername() + " - successfully logged in.");

                                    account.setWins(rs.getInt("numWins"));
                                    account.setLosses(rs.getInt("numLosses"));
                                    System.out.println(timestamp + " " + account.getUsername() + " - has record " + account.getWins() + " wins and " + account.getLosses() + " losses.");

                                    pw.println("SUCCESSFULLY LOGGED IN");
                                    pw.println(account.getWinLoss());
                                } else {
                                    System.out.println(timestamp + " " + account.getUsername() + " - has an account but not successfully logged in.");
                                    pw.println("INCORRECT PASSWORD");
                                }
                            } else {
                                System.out.println(timestamp + " " + account.getUsername() + " - does not have an account, so not successfully logged in.");
                                pw.println("INVALID USER");
                            }
                            break;

                        case "NEW ACCOUNT":
                            account.setUsername(br.readLine());
                            account.setPassword(br.readLine());
                            System.out.println(timestamp + " " + account.getUsername() + " - created an account with password " + account.getPassword() + ".");

                            ps = conn.prepareStatement("INSERT INTO Account(username, password, numWins, numLosses) VALUES (?,?,?,?)");
                            ps.setString(1, account.getUsername());
                            ps.setString(2, account.getPassword());
                            ps.setInt(3, 0);
                            ps.setInt(4, 0);
                            ps.executeUpdate();

                            System.out.println(timestamp + " " + account.getUsername() + " - has record 0 wins and 0 losses.");
                            account.setWins(0);
                            account.setLosses(0);

                            pw.println("SUCCESSFULLY LOGGED IN");
                            pw.println(account.getWinLoss());
                            break;

                        case "START GAME":
                            String gameName = br.readLine();
                            System.out.println(timestamp + " " + account.getUsername() + " - wants to start a game called " + gameName + ".");

                            if (server.containsGame(gameName)) {
                                System.out.println(timestamp + " " + account.getUsername() + " - " + gameName + " already exists, so unable to start " + gameName + ".");
                                pw.println("START UNSUCCESSFUL - GAME ALREADY EXISTS");
                            } else {
                                this.game = new HangmanGame(gameName);
                                account.setAccountID(0);
                                pw.println("START SUCCESSFUL");
                            }
                            break;

                        case "NUMBER OF PLAYERS":
                            System.out.println(timestamp + " " + account.getUsername() + " - successfully started game " + game.getName() + ".");
                            game.setNumPlayers(br.read() - '0');
                            game.addPlayer(this);
                            server.gameStart(game.getName(), game);

                            System.out.println(timestamp + " " + account.getUsername() + " - " + game.getName() + " needs " + game.getNumPlayers() + " to start game.");
                            if (game.getPlayers().size() == game.getNumPlayers()) {
                                waitForUsers(timestamp);
                            } else {
                                server.notify("WAITING FOR USER(S) TO JOIN", game.getName(), this);
                            }
                            break;

                        case "JOIN GAME":
                            String name = br.readLine();
                            System.out.println(timestamp + " " + account.getUsername() + " - wants to join a game called " + name + ".");

                            if (server.containsGame(name)) {
                                if (server.addPlayer(name, this)) {
                                    System.out.println(timestamp + " " + account.getUsername() + " - successfully joined " + name + ".");
                                    this.game = server.getGame(name);
                                    account.setAccountID(game.getPlayers().size() - 1);
                                    server.notify("JOIN SUCCESSFUL", game.getName(), this);

                                    if (game.getPlayers().size() == game.getNumPlayers()) {
                                        waitForUsers(timestamp);
                                    } else {
                                        server.notify("WAITING FOR USER(S) TO JOIN", game.getName(), this);
                                    }
                                } else {
                                    System.out.println(timestamp + " " + account.getUsername() + " - " + name + " exists, but " +
                                            account.getUsername() + " unable to join because maximum number of players have already joined " + name + ".");
                                    pw.println("JOIN UNSUCCESSFUL - GAME IS FULL");
                                }
                            } else {
                                pw.println("JOIN UNSUCCESSFUL - GAME DOES NOT EXIST");
                            }
                            break;


                        case "GUESS - LETTER":
                            this.guess = br.readLine();
                            System.out.println(timestamp + " " + account.getUsername() + " - guessed letter '" + guess + "'.");
                            server.notify(line, game.getName(), this);

                            int index = game.getSecretWord().indexOf(guess.toLowerCase());
                            if (index != -1) {
                                game.replace(guess, index);
                                StringBuilder positions = new StringBuilder(String.valueOf(index));
                                while (index != -1) {
                                    index = game.getSecretWord().indexOf(guess, index + 1);
                                    if (index != -1) {
                                        game.replace(guess, index);
                                        positions.append(", ");
                                        positions.append(index);
                                    }
                                }

                                System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is in '" + game.getSecretWord() + "' in position(s) " + positions.toString() +
                                        ". Secret word now shows " + game.getGuessedWord() + ".");

                                if (!game.getGuessedWord().contains("_")) {
                                    String opponents = updateAccounts();
                                    System.out.println(timestamp + " " + account.getUsername() + " - guessed the last letter and wins the game. " +
                                            opponents + "have lost the game.");
                                    server.notify("LAST LETTER GUESSED", game.getName(), this);
                                    server.removeGame(game.getName());
                                } else {
                                    server.notify("LETTER - CORRECT GUESS", game.getName(), this);
                                }
                            } else {
                                game.lowerGuesses();
                                System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is not in '" + game.getSecretWord() + "'. " +
                                        game.getName() + " now has " + game.getNumGuesses() + " guess(es) remaining.");

                                if (game.getNumGuesses() == 0) {
                                    System.out.println(timestamp + " " + account.getUsername() + " - no guesses remaining. All players have lost the game.");
                                    updateAccount();
                                    server.notify("NO GUESSES REMAINING", game.getName(), this);
                                    server.removeGame(game.getName());
                                } else {
                                    server.notify("LETTER - INCORRECT GUESS", game.getName(), this);
                                }
                            }
                            break;

                        case "GUESS - WORD":
                            this.guess = br.readLine();
                            System.out.println(timestamp + " " + account.getUsername() + " - guessed word '" + guess + "'.");
                            server.notify(line, game.getName(), this);

                            if (guess.toLowerCase().equals(game.getSecretWord().toLowerCase())) {
                                String temp = updateAccounts();
                                System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is correct. " +
                                        account.getUsername() + " wins the game. " + temp + "have lost the game.");
                                server.notify("WORD - CORRECT GUESS", game.getName(), this);
                                server.removeGame(game.getName());
                            } else {
                                System.out.println(timestamp + " " + account.getUsername() + " - '" + guess + "' is incorrect. " +
                                        account.getUsername() + " has lost and is no longer in the game.");
                                game.lowerGuesses();
                                account.lose();
                                server.notify("WORD - INCORRECT GUESS", game.getName(), this);

                                if (game.getNumGuesses() == 0) {
                                    System.out.println(timestamp + " " + account.getUsername() + " - no guesses remaining. All players have lost the game.");
                                    updateAccount();
                                    server.notify("NO GUESSES REMAINING", game.getName(), this);
                                    server.removeGame(game.getName());
                                } else {
                                    boolean gameOver = true;
                                    for(int x=0; x < game.getPlayers().size(); x++) {
                                        if (!game.getPlayers().get(x).getAccount().hasLost())
                                            gameOver = false;
                                    }
                                    if (gameOver) {
                                        System.out.println(timestamp + " " + account.getUsername() + " - no players remaining. All players have lost the game.");
                                        updateAccount();
                                        server.notify("NO PLAYERS REMAINING", game.getName(), this);
                                        server.removeGame(game.getName());
                                    } else {
                                        server.notify("CONTINUE GAME", game.getName(), this);
                                    }
                                }
                            }
                            break;

                    }
                }
            }
        } catch(Exception e){
            System.out.println("Error occurred");
        }
    }

    private void updateAccount() throws SQLException {
            for(int x=0; x < game.getPlayers().size(); x++){
                Account account = game.getPlayers().get(x).getAccount();
                account.incrementLoss();

                ps = conn.prepareStatement("UPDATE Account SET numLosses = ? WHERE username = ?");
                ps.setInt(1, account.getLosses());
                ps.setString(2, account.getUsername());
                ps.executeUpdate();
            }
    }
    private String updateAccounts() throws SQLException {
        StringBuilder opponents = new StringBuilder();
        for(int x=0; x < game.getPlayers().size(); x++){
            if (game.getPlayers().get(x) == this) {
                account.setWins(account.getWins() + 1);

                ps = conn.prepareStatement("UPDATE Account SET numWins = ? WHERE username = ?");
                ps.setInt(1, account.getWins());
                ps.setString(2, account.getUsername());
                ps.executeUpdate();
            } else {
                Account account = game.getPlayers().get(x).getAccount();
                account.incrementLoss();

                ps = conn.prepareStatement("UPDATE Account SET numLosses = ? WHERE username = ?");
                ps.setInt(1, account.getLosses());
                ps.setString(2, account.getUsername());
                ps.executeUpdate();

                opponents.append(account.getUsername()).append(" ");
            }
        }
        return opponents.toString();
    }

    private void waitForUsers(String timestamp) {
        System.out.println(timestamp + " " + account.getUsername() + " - " + game.getName() + " has " + game.getNumPlayers() + " player(s) so starting game. " +
                "Secret word is " + game.getSecretWord() + ".");
        game.setGuessedWord(game.getSecretWord().replaceAll("[A-Za-z]", "_ "));

        server.notify("ALL USERS HAVE JOINED", game.getName(), this);
    }

    public String getGuess() {
        return guess;
    }

    public void message(String message) {
        pw.println(message);
    }

    public void message(int num) {
        pw.println(num);
    }

    public Account getAccount() {
        return account;
    }
}

