package com.cs201.controller;


import com.cs201.model.Accounts;
import com.cs201.model.BookValue;
import com.cs201.model.Junction;
import com.cs201.repository.AccountRepository;
import com.cs201.repository.JunctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.google.api.services.books.Books;
import org.springframework.stereotype.Controller;

@Controller()
public class APIController {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    JunctionRepository junctionRepository;

        @PostMapping("/createAccount")
    public String insertAccount(@RequestParam(name="username", required=false, defaultValue = "") String username, @RequestParam(name="password", required=false, defaultValue = "") String password, @RequestParam(name="password2", required=false, defaultValue = "") String password2, Model model, HttpServletRequest request, HttpSession session) {
        if(username.equals("") || password.equals("") || password2.equals("")){
            model.addAttribute("errorString", "One or more of the input fields are blank. Please try again.");
            return "Register.html";
        }
        if(!(accountRepository.findAccountsByUsernameEquals(username)).isEmpty()){
            model.addAttribute("errorString", "This username is already taken.");
            return "Register.html";
        }
        if(!password.equals(password2))
        {
            model.addAttribute("errorString", "The passwords do not match.");
            return "Register.html";
        }
        Accounts account = new Accounts();
        account.setPassword(password);
        account.setUsername(username);
        accountRepository.save(account);
        List<Accounts> accountsList = accountRepository.findAccountsByUsernameEqualsAndPasswordEquals(username, password);
        session.setAttribute("user", true);
        session.setAttribute("userID", accountsList.get(0).getUserID());
        session.setAttribute("username", accountsList.get(0).getUsername());
        model.addAttribute("sessionActive", session.getAttribute("user"));
        return "HomePage.html";
    }

    @PostMapping("/login")
    public String signIn(@RequestParam(name="username", required=false, defaultValue = "") String username, @RequestParam(name="password", required=false, defaultValue = "") String password, Model model,HttpSession session){
        List<Accounts> account = accountRepository.findAccountsByUsernameEquals(username);
        if(account.isEmpty()){
            model.addAttribute("errorString", "This user does not exist.");
            return "Login.html";
        }
        account = accountRepository.findAccountsByUsernameEqualsAndPasswordEquals(username, password);
        if(account.isEmpty()) {
            model.addAttribute("errorString", "Incorrect password.");
            return "Login.html";
        }
        session.setAttribute("user", true);
        session.setAttribute("userID", account.get(0).getUserID());
        session.setAttribute("username", account.get(0).getUsername());
        model.addAttribute("errorString","");
        model.addAttribute("sessionActive", session.getAttribute("user"));
        return "HomePage.html";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model){
            session.setAttribute("user",false);
            session.setAttribute("userID",0);
            session.setAttribute("username","");
            model.addAttribute("errorString","");
            model.addAttribute("sessionActive", session.getAttribute("user"));
            return "HomePage.html";
    }

    @PostMapping("/RemoveProfile")
    public String yeetThisBook(@RequestParam(name="bookID", required=true) String bookID, HttpSession session, Model model){
        junctionRepository.removeJunctionsByAccount_UserIDAndBookID((Integer) session.getAttribute("userID"), bookID);
        return returnSessionBooks(model, session);
    }
    @GetMapping("/Profile")
    public String returnSessionBooks(Model model, HttpSession session){
        List<Junction> junctions = junctionRepository.findJunctionByAccount_UserIDOrderByTimeStamp((Integer) session.getAttribute("userID"));
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        ArrayList<BookValue> bookValues = new ArrayList<BookValue>();
        try {
            final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
                    .setApplicationName("CSCI201Project")
                    .setGoogleClientRequestInitializer(new BooksRequestInitializer("AIzaSyBN2171MOM7i2rEobTiE8ITC_pOv13em88"))
                    .build();
            for (Junction  y: junctions){
                Volume volume = (books.volumes().get(y.getBookID())).execute();
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
                BookValue temp = new BookValue(title,link,author,description,pubDate,isbn13,(int)rating,publisher,noRatingFound,volume.getId());
                bookValues.add(temp);
            }

        }
        catch(Exception e){
            return "Profile";
        }
        model.addAttribute("bookValues", bookValues);
        model.addAttribute("name", session.getAttribute("username"));
        return "Profile";
    }




}

