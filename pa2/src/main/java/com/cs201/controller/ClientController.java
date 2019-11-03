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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;


@Controller
public class ClientController {
    @RequestMapping("/login")
    public String login() {
        return "Login";
    }
    @RequestMapping("/Register")
    public String register() {
        return "Register";
    }
    @GetMapping ("/HomePage")
    public String init(@RequestParam(name="errorString", required=false, defaultValue = "") String errorString, Model model, HttpSession session){
        model.addAttribute("errorString");
        return "HomePage.html";
    }

    @PostMapping("/SearchResults")
//    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
    public String greeting(@RequestParam(name="searchInput", required=false, defaultValue = "") String searchInput, @RequestParam(name="type", required=false, defaultValue = "") String type, Model model, HttpSession session) {
        if(searchInput.equals("")){
            String errorString = "You left an input field blank. Please try again.";
            model.addAttribute("errorString", errorString);
            return "HomePage.html";
        }
        String pref = "";
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        if(type.equals("name")){
            pref = "intitle:";

        }
        else if(type.equals("Author")){
            pref = "inauthor:";

        }
        else if(type.equals("Publisher")){
            pref = "inpublisher:";
        }
        else if(type.equals("ISBN")){
            pref = "isbn:";
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
            Volumes volumes = volumesList.execute();
            if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
                errorString = "No books were found. Please try again.";
                model.addAttribute("errorString", errorString);
                return "HomePage.html";
            }
            for (Volume volume : volumes.getItems()) {
                Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
                String author =(volumeInfo.getAuthors() != null && !volumeInfo.getAuthors().isEmpty()) ? volumeInfo.getAuthors().get(0) : "No Author Found";
                Volume.VolumeInfo.ImageLinks imageLinks = volumeInfo.getImageLinks();
                String link;
                if(imageLinks == null || imageLinks.getThumbnail() == null){
                    link = "";
                }else
                    link = imageLinks.getThumbnail();
                String isbn13 = null;
                java.util.List<Volume.VolumeInfo.IndustryIdentifiers> isbnNums = volumeInfo.getIndustryIdentifiers();
                for(Volume.VolumeInfo.IndustryIdentifiers x : isbnNums){
                    if(x.getType().equalsIgnoreCase("ISBN_13"))
                        isbn13 = x.getIdentifier();
                }
                if(isbn13 == null)
                    isbn13 = "No ISBN-13 was found";
                String title = volumeInfo.getTitle();
                String description = volumeInfo.getDescription();
                if(description==null)
                    description = "No description found";
                String pubDate = volumeInfo.getPublishedDate();
                double rating;
                String noRatingFound = "";
                if(volumeInfo.getAverageRating() == null){
                    rating = 0.0;
                    noRatingFound="No rating was found";
                }
                else
                    rating = volumeInfo.getAverageRating()*20.0;
                String publisher = (volumeInfo.getPublisher()==null || volumeInfo.getPublisher().equals("")) ?
                        "No publisher was found" : volumeInfo.getPublisher();
                BookValue temp = new BookValue(title,link,author,description,pubDate,isbn13,(int)rating,publisher,noRatingFound);
                bookValues.add(temp);
            }
        }
        catch(Exception e){
            return "HomePage.html";

        }
        model.addAttribute("bookValues", bookValues);
        model.addAttribute("searchInput", searchInput);
        model.addAttribute("type", type);
        return "SearchResults.html";
    }

    @PostMapping("/Details")
    public String details(@RequestParam(name="searchInput", required=true) String searchInput, @RequestParam(name="type", required=true)
            String type,@RequestParam(name="link", required=true) String link, @RequestParam(name="title", required=true) String title,
                          @RequestParam(name="author", required=true) String author, @RequestParam(name="publisher", required=true) String publisher,
                          @RequestParam(name="date", required=true) String date,
                          @RequestParam(name="isbn", required=true) String isbn,
                          @RequestParam(name="summary", required=true) String summary,
                          @RequestParam(name="rating", required=true) String rating,@RequestParam(name="noRatingFound",required = true) String noRatingFound,
                          Model model, HttpSession session){
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
        model.addAttribute("noRatingFound", noRatingFound);
        return "Details.html";
    }

    @PostMapping("/hello")
    public String hello(@RequestParam(name="name", required=true) String name, Model model, HttpSession session) {

//        Books books = Books.Builder


        model.addAttribute("name", name);
        return "hello";
    }
}