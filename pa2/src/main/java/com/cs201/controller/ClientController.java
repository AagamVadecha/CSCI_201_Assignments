package com.cs201.controller;

import com.cs201.model.BookValue;
//import com.cs201.model.bookValuesList;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.google.api.services.books.Books;
import org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType;
import org.springframework.expression.spel.ast.NullLiteral;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;
import java.util.ArrayList;


@Controller
public class ClientController {
    @GetMapping ("/HomePage")
    public String init(@RequestParam(name="errorString", required=false, defaultValue = "") String errorString, Model model){
        model.addAttribute("errorString");
        return "HomePage";
    }

    @PostMapping("/SearchResults")
//    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
    public String greeting(@RequestParam(name="searchInput", required=false, defaultValue = "") String searchInput, @RequestParam(name="type", required=false, defaultValue = "") String type, Model model) {
        if(searchInput.equals("") || type.equals("")){
            String errorString = "You left an input field blank. Please try again.";
            model.addAttribute("errorString", errorString);
            return "HomePage";
        }
        String pref = "";
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        if(type.equals("name")){
            pref = "intitle: ";

        }
        else if(type.equals("Author")){
            pref = "inauthor: ";

        }
        else if(type.equals("Publisher")){
            pref = "inpublisher: ";
        }
        else if(type.equals("ISBN")){
            pref = "isbn: ";
        }
        pref = pref + searchInput.trim();

        ArrayList<BookValue> bookValues = new ArrayList<BookValue>();
        String errorString = "";
        try {
            final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
                    .setApplicationName("CSCI201Project")
                    .setGoogleClientRequestInitializer(new BooksRequestInitializer("AIzaSyBN2171MOM7i2rEobTiE8ITC_pOv13em88"))
                    .build();
            List volumesList = books.volumes().list(pref);

            // Execute the query.
            Volumes volumes = volumesList.execute();
            if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
                errorString = "No books were found. Please try again.";
                model.addAttribute("errorString", errorString);
                return "HomePage";
            }

            System.out.println(volumes.getTotalItems());
            for (Volume volume : volumes.getItems()) {
                Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
                String author =(volumeInfo.getAuthors() != null && !volumeInfo.getAuthors().isEmpty()) ? volumeInfo.getAuthors().get(0) : "No Author Found";
                Volume.VolumeInfo.ImageLinks imageLinks = volumeInfo.getImageLinks();
                String link;
                if(imageLinks == null || imageLinks.getThumbnail() == null){
                    link = "";
                }else
                    link = imageLinks.getThumbnail();

                String title = volumeInfo.getTitle();
                String description = volumeInfo.getDescription();
                if(description==null)
                    description = "No description found";
                String pubDate = volumeInfo.getPublishedDate();
                double rating;
                if(volumeInfo.getAverageRating() == null)
                    rating = 0.0;
                else
                    rating = volumeInfo.getAverageRating()*20.0;
                String publisher = volumeInfo.getPublisher();
                int isbn = type.equals("ISBN") ? Integer.parseInt(searchInput.trim()) : (int) ((Math.random() * 900000000)+100000000);
                BookValue temp = new BookValue(title, link, author , description, pubDate, isbn , (int)rating, publisher);
                bookValues.add(temp);
            }
        }
        catch(Exception e){
            return "HomePage";

        }
//        catch(Exception e){
//            errorString = "Your query failed. Please try again.";
//            model.addAttribute("errorString", errorString);
//            System.out.println("ERROR" + e.getMessage() + e.getStackTrace());
//            return "HomePage";
////            model.addAttribute("bookValues", bookValues);
////            return  "SearchResults";
//        }
        model.addAttribute("bookValues", bookValues);
        model.addAttribute("searchInput", searchInput);
        model.addAttribute("type", type);
        return  "SearchResults";
    }

    @PostMapping("/Details")
    public String details(@RequestParam(name="searchInput", required=true) String searchInput, @RequestParam(name="type", required=true)
            String type,@RequestParam(name="link", required=true) String link, @RequestParam(name="title", required=true) String title,
                          @RequestParam(name="author", required=true) String author, @RequestParam(name="publisher", required=true) String publisher,
                          @RequestParam(name="date", required=true) String date,
                          @RequestParam(name="isbn", required=true) String isbn,
                          @RequestParam(name="summary", required=true) String summary,
                          @RequestParam(name="rating", required=true) String rating,
                          Model model){
        model.addAttribute("searchInput", searchInput);
        model.addAttribute("type", type);
        model.addAttribute("link", link);
        model.addAttribute("title", title);
        model.addAttribute("author",author);
        model.addAttribute("publisher",publisher);
        model.addAttribute("date",date);
        model.addAttribute("isbn",isbn);
        model.addAttribute("summary",summary);
        model.addAttribute("rating",rating);
        return "Details.html";
    }

    @PostMapping("/hello")
    public String hello(@RequestParam(name="name", required=true) String name, Model model) {

//        Books books = Books.Builder


        model.addAttribute("name", name);
        return "hello";
    }
}