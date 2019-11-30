DROP DATABASE if exists HangmanAccounts;
CREATE DATABASE HangmanAccounts;
USE HangmanAccounts;
CREATE TABLE Account (
    userID    INT(11)     PRIMARY KEY NOT NULL AUTO_INCREMENT,
    username  VARCHAR(50) NOT NULL,
    password  VARCHAR(50) NOT NULL,
    numWins   INT(11)     NOT NULL,
    numLosses INT(11)     NOT NULL
);