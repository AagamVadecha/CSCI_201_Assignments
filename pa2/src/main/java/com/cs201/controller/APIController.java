package com.cs201.controller;


import com.cs201.model.Accounts;
import com.cs201.repository.AccountRepository;
import com.cs201.repository.JunctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

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
        if(!accountRepository.findAccountsByUsername(username).isEmpty()){
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
        List<Accounts> accountsList = accountRepository.findAccount(username, password);
        session.invalidate();
        HttpSession newSession = request.getSession();
        session.setAttribute("userID", accountsList.get(0).getUserID());
        session.setAttribute("username", accountsList.get(0).getUsername());
        return "HomePage.html";
    }

    @PostMapping("/login")
    public String signIn(@RequestParam(name="username", required=false, defaultValue = "") String username, @RequestParam(name="password", required=false, defaultValue = "") String password, Model model,HttpSession session, HttpServletRequest request){
        List<Accounts> account = accountRepository.findAccount(username, password);
        if(account.isEmpty()) {
            model.addAttribute("errorString", "No account was found with this combination.");
            return "Login.html";
        }
        session.invalidate();
        HttpSession newSession = request.getSession();
       session.setAttribute("userID", account.get(0).getUserID());
       session.setAttribute("username", account.get(0).getUsername());
        return "HomePage.html";
    }




}

