package com.cs201.model;
import java.lang.String;

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

    public String getIsbn() {
        return isbn;
    }

    public double getRating() {
        return rating;
    }

    public String getPublisher() {
        return publisher;
    }
    public String getNoRatingFound(){
        return noRatingFound;
    }
    public String getBookID(){ return bookID; }

    String bookName;
    String bookLink;
    String bookAuthor;
    String bookSummary;
    String date;
    String isbn;
    int rating;
    String publisher;
    String noRatingFound;
    String bookID;

    public BookValue(String bookName, String bookLink, String bookAuthors, String bookSummary, String date, String isbn13, int rating, String publisher, String noRatingFound, String bookID) {
        this.bookName = bookName;
        this.bookLink = bookLink;
        this.bookAuthor = bookAuthors;
        this.bookSummary = bookSummary;
        this.date = date;
        this.isbn = isbn13;
        this.rating = rating;
        this.publisher = publisher;
        this.noRatingFound = noRatingFound;
        this.bookID = bookID;
    }
}
