//package com.cs201.controller;
//
//import com.cs201.model.User;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController("/")
//public class APIController {
//    @GetMapping("/user/{name}")
//    public ResponseEntity<User> hello(@PathVariable("name") String name ) {
//        User user = new User();
//        user.name = name;
//        user.age = 9;
//        return new ResponseEntity<>(user, HttpStatus.OK);
//    }
//
//}
