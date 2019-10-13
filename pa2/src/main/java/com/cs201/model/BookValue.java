package com.cs201.model;

import java.util.ArrayList;
import java.util.List;

public class BookValue{
    public String getBookName() {
        return bookName;
    }

    public String getBookLink() {
        return bookLink;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public String getBookSummary() {
        return bookSummary;
    }

    public String getDate() {
        return date;
    }

    public int getIsbn() {
        return isbn;
    }

    public double getRating() {
        return rating;
    }

    public String getPublisher() {
        return publisher;
    }

    String bookName;
    String bookLink;
    String bookAuthor;
    String bookSummary;
    String date;
    int isbn;
    int rating;
    String publisher;

    public BookValue(String bookName, String bookLink, String bookAuthors, String bookSummary, String date, int isbn, int rating, String publisher) {
        this.bookName = bookName;
        this.bookLink = bookLink;
        this.bookAuthor = bookAuthors;
        this.bookSummary = bookSummary;
        this.date = date;
        this.isbn = isbn;
        this.rating = rating;
        this.publisher = publisher;
    }
    public BookValue(String bookName, String bookLink, String bookAuthors, String bookSummary, String date, int rating, String publisher) {
        this.bookName = bookName;
        this.bookLink = bookLink;
        this.bookAuthor = bookAuthors;
        this.bookSummary = bookSummary;
        this.date = date;
        this.rating = rating;
        this.publisher = publisher;
    }
}
