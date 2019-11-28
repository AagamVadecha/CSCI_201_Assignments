package com.company;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class HangmanClient extends Thread {
    private static Scanner scanner;
    private BufferedReader br;
    private PrintWriter pw;

    private String username;
    private String password;
    private String gameName;
    private String displayedWord;

    public HangmanClient(String name, int port){
        Socket s = null;
        try {
            System.out.print("Trying to connect to server...");
            s = new Socket(name, port);
            System.out.println("Connected!");

            pw = new PrintWriter(s.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            getUserLogin();
            this.start();
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                String line = br.readLine();

                if (line != null) {
                    switch (line) {
                        case "SUCCESSFULLY LOGGED IN":
                            System.out.println("\nGreat! You are now logged in as " + this.username + ".\n");
                            for (int i = 0; i < 4; i++) {
                                System.out.println(br.readLine());
                            }
                            playGame();
                            break;

                        case "INCORRECT PASSWORD":
                            System.out.println("The password you entered is incorrect. Please try again.");
                            getUserLogin();
                            break;

                        case "INVALID USER":
                            System.out.println("\nNo account exists with those credentials.");
                            System.out.print("Would you like to create a new account? ");
                            String input = scanner.nextLine();
                            if (input.toLowerCase().equals("yes") || input.toLowerCase().equals("y")) {
                                System.out.print("Would you like to use the username and password above? ");
                                input = scanner.nextLine();
                                if (input.toLowerCase().equals("yes") || input.toLowerCase().equals("y")) {
                                    pw.println("NEW ACCOUNT");
                                    pw.println(this.username);
                                    pw.println(this.password);
                                } else {
                                    getUserLogin();
                                }
                            } else {
                                getUserLogin();
                            }
                            break;

                        case "START SUCCESSFUL":
                            int numPlayers = getIntInput(null, "\nHow many users will be playing (1-4)? ", "A game can only have between 1-4 players.", 1, 4);
                            pw.println("NUMBER OF PLAYERS");
                            pw.println(numPlayers);
                            break;

                        case "START UNSUCCESSFUL - GAME ALREADY EXISTS":
                            System.out.println("\n" + gameName + " already exists.");
                            getGameOption();
                            break;

                        case "JOIN SUCCESSFUL": case "USER JOINED":
                            System.out.println("\n" + br.readLine() + "\n");
                            for (int i = 0; i < 4; i++) {
                                System.out.println(br.readLine());
                            }
                            break;

                        case "JOIN UNSUCCESSFUL - GAME DOES NOT EXIST":
                            System.out.println("\nThere is no game with the name " + gameName + ".");
                            getGameOption();
                            break;

                        case "JOIN UNSUCCESSFUL - GAME IS FULL":
                            System.out.println("\nThe game " + gameName + " does not have space for another user to join.");
                            getGameOption();
                            break;

                        case "ALL USERS HAVE JOINED":
                            System.out.println("\nAll users have joined.");
                            System.out.println("\nDetermining secret word...");
                            this.displayedWord = br.readLine();
                            System.out.println("\nSecret Word " + displayedWord);
                            break;

                        case "WAITING FOR USER(S) TO JOIN":
                            System.out.println("\nWaiting for " + (br.read() - '0') + " other user(s) to join...");
                            break;

                        case "PLAYER TURN":
                            getGuessOption(br.read() - '0');
                            break;

                        case "WAITING FOR OPPONENT":
                            System.out.println("\nYou have " + (br.read() - '0') + " incorrect guesses remaining.");
                            br.readLine();
                            System.out.println("Waiting for " + br.readLine() + " to do something...");
                            break;

                        case "GUESS - LETTER":
                            System.out.println("\n" + br.readLine() + " has guessed the letter '" + br.readLine() + "'.");
                            break;

                        case "GUESS - WORD":
                            System.out.println("\n" + br.readLine() + " has guessed the word '" + br.readLine() + "'.");
                            break;

                        case "LETTER - CORRECT GUESS":
                            System.out.println("\nThe letter '" + br.readLine() + "' is in the secret word.");
                            this.displayedWord = br.readLine();
                            System.out.println("\nSecret Word: " + displayedWord);
                            break;

                        case "LETTER - INCORRECT GUESS":
                            System.out.println("\nThe letter '" + br.readLine() + "' is not in the secret word.");
                            this.displayedWord = br.readLine();
                            System.out.println("\nSecret Word: " + displayedWord);
                            break;

                        case "LAST LETTER GUESSED":
                            System.out.println("\nYou guessed the last letter! You win!");
                            break;

                        case "WORD - CORRECT GUESS":
                            System.out.println("\nThat is correct! You win!");
                            break;

                        case "WORD - INCORRECT GUESS":
                            System.out.println("\nThat is incorrect! You are out of the game!");
                            break;

                        case "NO GUESSES REMAINING":
                            System.out.println("\nNo guesses remaining. You lose!");
                            System.out.println("The word was '" + br.readLine() + "'.");
                            break;

                        case "OPPONENT WIN - LETTER":
                            System.out.println("\n" + br.readLine() + " guessed the last letter. You lose!");
                            break;

                        case "OPPONENT WIN - WORD":
                            System.out.println("\n" + br.readLine() + " guessed the word correctly. You lose!");
                            break;

                        case "OPPONENT LOSE":
                            System.out.println("\n" + br.readLine() + " guessed the word incorrectly and is out of the game.");
                            break;

                        case "NO PLAYERS REMAINING":
                            System.out.println("\nNo players remaining. You lose!");
                            System.out.println("The word was '" + br.readLine() + "'.");
                            break;

                        case "CONTINUE GAME":
                            this.displayedWord = br.readLine();
                            System.out.println("\nSecret Word " + displayedWord);
                            break;
                        case "PLAYER RECORD":
                            for (int i = 0; i < 4; i++) {
                                System.out.print("\n" + br.readLine());
                            }
                            System.out.println();
                            break;

                        case "GAME EXIT":
                            System.out.println("\nThank you for playing Hangman!");
                            break;
                    }
                }
                }
            } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playGame() {
        getGameOption();
    }

    public void getUserLogin() {
        pw.println("LOGIN");

        System.out.print("\nUsername: ");
        this.username = scanner.nextLine();
        pw.println(this.username);

        System.out.print("Password: ");
        this.password = scanner.nextLine();
        pw.println(this.password);
    }

    public void getGameOption() {
        int input = getIntInput(displayGameOptions(), "Would you like to start a game or join a game? ", "That is not a valid option.", 1, 2);
        System.out.print("\nWhat is the name of the game? ");
        this.gameName = scanner.nextLine();

        if (input == 1) {
            pw.println("START GAME");
            pw.println(gameName);
        } else {
            pw.println("JOIN GAME");
            pw.println(gameName);
        }
    }
    public void getGuessOption(int num) {
        int input = getIntInput(displayGuessOptions(num), "What would you like to do? ", "That is not a valid option.", 1, 2);

        if (input == 1) {
            String guess = getCharInput("\nLetter to guess - ", "That is not a valid guess.", displayedWord);
            pw.println("GUESS - LETTER");
            pw.println(guess);
        } else {
            System.out.print("\nWhat is the secret word? ");
            String guess = scanner.nextLine();
            pw.println("GUESS - WORD");
            pw.println(guess);
        }
    }


    public static String displayGameOptions() {
        return "\n    1) Start a Game\n    2) Join a Game\n";
    }

    public static String displayGuessOptions(int num) {
        return "\nYou have " + num + " incorrect guesses remaining." +
                "\n\t1) Guess a letter.\n\t2) Guess the word.\n";
    }

    public static int getIntInput(String display, String prompt, String error, int min, int max) {
        int num = 0;
        do {
            if (display != null) {
                System.out.println(display);
            }
            System.out.print(prompt);
            while (!scanner.hasNextInt()) {
                System.out.println("\nThat is not a valid option.");
                if (display != null) {
                    System.out.println(display);
                }
                System.out.print(prompt);
                scanner.nextLine();
            }
            num = scanner.nextInt(); scanner.nextLine();
            if (num < min || num > max) {
                System.out.println("\n" + error);
            }
        } while (num < min || num > max);
        return num;
    }

    public static String getCharInput(String prompt, String error, String word) {
        String str = "";
        do {
            System.out.print(prompt);
            while (!scanner.hasNext()) {
                System.out.println("\n" + error);
                System.out.print(prompt);
                scanner.nextLine();
            }
            str = scanner.nextLine();
            if (str.length() != 1) {
                System.out.println("\n" + error);
            } else if (word.toLowerCase().indexOf(str.toLowerCase()) != -1) {
                System.out.println("\n" + error);
            } else if (!(str.charAt(0) >= 'a' && str.charAt(0) <= 'z') && !(str.charAt(0) >= 'A' && str.charAt(0) <= 'Z')) {
                System.out.println("\n" + error);
            }
        } while (str.length() != 1 || word.toLowerCase().indexOf(str.toLowerCase()) != -1 ||
                (!(str.charAt(0) >= 'a' && str.charAt(0) <= 'z') && !(str.charAt(0) >= 'A' && str.charAt(0) <= 'Z')));
        return str;
    }

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        String filename = "", hostname = "", port = "", connection = "", dbUsername = "", dbPassword = "", wordFile = "";

        do {
            System.out.print("What is the name of the configuration file? ");
            filename = scanner.nextLine();

            try (InputStream is = new FileInputStream(filename);) {
                Properties configFile = new Properties();
                if (is != null) {
                    configFile.load(is);
                }

                System.out.println("\nReading config file...");
                hostname = configFile.getProperty("ServerHostname");
                port = configFile.getProperty("ServerPort");
                connection = configFile.getProperty("DBConnection");
                dbUsername = configFile.getProperty("DBUsername");
                dbPassword = configFile.getProperty("DBPassword");
                wordFile = configFile.getProperty("SecretWordFile");
                break;
            } catch (FileNotFoundException fnfe) {
                System.out.println("Configuration file " + filename + " could not be found.");
            } catch (IOException ioe) {
                System.out.println("IOE: " + ioe.getMessage());
            }
        } while (true);

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
        if (dbUsername == null || dbUsername.equals("")) {
            System.out.println("DBUsername is a required parameter in the configuration file.");
            isValid = false;
        }
        if (dbPassword == null || dbPassword.equals("")) {
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
            System.out.println("Database Username - " + dbUsername);
            System.out.println("Database Password - " + dbPassword);
            System.out.println("Secret Word File - " + wordFile + "\n");
        } else {
            System.exit(0);
        }

        new HangmanClient(hostname, Integer.parseInt(port));
    }

}