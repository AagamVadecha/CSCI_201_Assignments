package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class HangmanClient extends Thread {
    private static Scanner scanner;
    private BufferedReader br;
    private PrintWriter pw;

    private String username;
    private String password;
    private String gameName;
    private String displayedWord;

    public HangmanClient(String name, int port) {
        try {
            System.out.print("Trying to connect to server...");
            Socket s = new Socket(name, port);
            System.out.println("Connected!");

            pw = new PrintWriter(s.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            getUserLogin();
            this.start();
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public void run() {
        try {
            breakLabel :
            {
                while (true) {
                    String line = br.readLine();

                    if (line != null) {
                        switch (line) {
                            case "logged in":
                                System.out.println("\nGreat! You are now logged in as " + this.username + ".\n");
                                for (int i = 0; i < 4; i++) {
                                    System.out.println(br.readLine());
                                }
                                getGameOption();
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
                                    if (input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y")) {
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
                                pw.println("Count Players");
                                pw.println(numPlayers);
                                break;

                            case "START UNSUCCESSFUL - GAME ALREADY EXISTS":
                                System.out.println("\n" + gameName + " already exists.");
                                getGameOption();
                                break;

                            case "Successfully joined": case "Another person joined":
                                System.out.println("\n" + br.readLine() + "\n");
                                for (int i = 0; i < 4; i++) {
                                    System.out.println(br.readLine());
                                }
                                break;

                            case "Game doesn't exist":
                                System.out.println("\nThere is no game with the name " + gameName + ".");
                                getGameOption();
                                break;

                            case "Game's full":
                                System.out.println("\nThe game " + gameName + " does not have space for another user to join.");
                                getGameOption();
                                break;

                            case "No users joining":
                                System.out.println("\nAll users have joined.");
                                System.out.println("\nDetermining secret word...");
                                this.displayedWord = br.readLine();
                                System.out.println("\nSecret Word " + displayedWord);
                                break;

                            case "Users still joining":
                                System.out.println("\nWaiting for " + (br.read() - '0') + " other user(s) to join...");
                                break;

                            case "Your turn":
                                getGuessOption(br.read() - '0');
                                break;

                            case "Waiting for another user":
                                System.out.println("\nYou have " + (br.read() - '0') + " incorrect guesses remaining.");
                                br.readLine();
                                System.out.println("Waiting for " + br.readLine() + " to do something...");
                                break;

                            case "Letter guess":
                                System.out.println("\n" + br.readLine() + " has guessed the letter '" + br.readLine() + "'.");
                                break;

                            case "Word guess":
                                System.out.println("\n" + br.readLine() + " has guessed the word '" + br.readLine() + "'.");
                                break;

                            case "Correct letter guess":
                                System.out.println("\nThe letter '" + br.readLine() + "' is in the secret word.");
                                this.displayedWord = br.readLine();
                                System.out.println("\nSecret Word: " + displayedWord);
                                break;

                            case "Incorrect Letter Guess":
                                System.out.println("\nThe letter '" + br.readLine() + "' is not in the secret word.");
                                this.displayedWord = br.readLine();
                                System.out.println("\nSecret Word: " + displayedWord);
                                break;

                            case "Guessed last letter of word":
                                System.out.println("\nYou guessed the last letter! You win!");
                                break;

                            case "Correct Word Guess":
                                System.out.println("\nThat is correct! You win!");
                                break;

                            case "Incorrect Word Guess":
                                System.out.println("\nThat is incorrect! You are out of the game!");
                                break;

                            case "No guesses left":
                                System.out.println("\nNo guesses remaining. You lose!");
                                System.out.println("The word was '" + br.readLine() + "'.");
                                break;

                            case "Opponent won through letter":
                                System.out.println("\n" + br.readLine() + " guessed the last letter. You lose!");
                                break;

                            case "OPPONENT WIN - WORD":
                                System.out.println("\n" + br.readLine() + " guessed the word correctly. You lose!");
                                break;

                            case "Opponent incorrectly guessed word":
                                System.out.println("\n" + br.readLine() + " guessed the word incorrectly and is out of the game.");
                                break;

                            case "No accounts left":
                                System.out.println("\nNo players remaining. You lose!");
                                System.out.println("The word was '" + br.readLine() + "'.");
                                break;

                            case "Continue game":
                                this.displayedWord = br.readLine();
                                System.out.println("\nSecret Word " + displayedWord);
                                break;
                            case "Get record":
                                for (int i = 0; i < 4; i++) {
                                    System.out.print("\n" + br.readLine());
                                    if (i == 3)
                                        System.out.print("\n");
                                }
                                break;

                            case "Finish Game":
                                System.out.println("\nThank you for playing Hangman!");
                                break breakLabel;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserLogin() {
        pw.println("login attempt");

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
            pw.println("New Game");
        } else {
            pw.println("Try To Join Game");
        }
        pw.println(gameName);
    }

    public void getGuessOption(int num) {
        int input = getIntInput(displayNumGuessOptions(num), "What would you like to do? ", "That is not a valid option.", 1, 2);

        if (input == 1) {
            String guess;
            boolean goneThrough;
            do {
                System.out.print("\nLetter to guess - ");
                while (!scanner.hasNext()) {
                    System.out.println("\n" + "That is not a valid guess.");
                    System.out.print("\nLetter to guess - ");
                    scanner.nextLine();
                }
                goneThrough = true;
                guess = scanner.nextLine().trim();
                if (guess.length() != 1 || displayedWord.toLowerCase().contains(guess.toLowerCase()) ||!(Character.isLetter(guess.charAt(0)))) {
                    System.out.println("\n" + displayedWord);
                    goneThrough = false;
                }
            } while (!goneThrough);
            pw.println("Letter guess");
            pw.println(guess);
        } else {
            System.out.print("\nWhat is the secret word? ");
            String guess = scanner.nextLine().trim();
            pw.println("Word guess");
            pw.println(guess);
        }
    }


    public static String displayGameOptions() {
        return "\n    1) Start a Game\n    2) Join a Game\n";
    }

    public static String displayNumGuessOptions(int num) {
        return "\nYou have " + num + " incorrect guesses remaining.\n\t1) Guess a letter.\n\t2) Guess the word.\n";
    }

    public static int getIntInput(String display, String prompt, String error, int min, int max) {
        int num;
        boolean goneThrough;
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
            goneThrough = true;
            num = scanner.nextInt();
            scanner.nextLine();
            if (num < min || num > max) {
                System.out.println("\n" + error);
                goneThrough=false;
            }
        } while (!goneThrough);
        return num;
    }

    public static boolean containsValue(String string, String name) {
        if (string == null || string.equals("")) {
            System.out.println(name + " is a required parameter in the configuration file.");
            return false;
        }
        return true;
    }


    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        String filename;
        configFileProperties properties = null;
        boolean validFile = false;
        do {
            System.out.print("What is the name of the configuration file? ");
            filename = scanner.nextLine();
            try (InputStream is = new FileInputStream(filename)) {
                properties = new configFileProperties(is);
                validFile = true;
            } catch (FileNotFoundException exception) {
                System.out.println("Configuration file " + filename + " could not be found.");
            } catch (Exception e) {
                System.out.println("Other Exception: " + e.getMessage());
            }
        } while (!validFile);

        HangmanServer.validFile(properties, containsValue(properties.getHostname(), "hostname"), containsValue(properties.getPort(), "port"), containsValue(properties.getConnection(), "connection"), containsValue(properties.getDBUsername(), "DBUsername"), containsValue(properties.getDBPassword(), "DBPassword"), containsValue(properties.getSecretWordFile(), "SecretWordFile"));

        new HangmanClient(properties.getHostname(), Integer.parseInt(properties.getPort()));
    }

}