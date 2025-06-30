package com.example.bank.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

@RestController
public class HelloController {
    @GetMapping("/hello1")
    public String hello() {
        return ("Hello world");
    }
    @GetMapping("/hello2")
    public String hello(@RequestParam String name) {
        return ("Hello world2" + name);
    }

    @GetMapping("/hello3")
    public String hello3(@RequestParam(required = false) String name) {
        if (name != null) {
            return "Hello world " + name;
        } else {
            return "Hello world3";
        }
    }


    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.status(HttpStatus.CREATED).body("Hello world");
    }



}
