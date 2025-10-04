package com.example.SpringREST.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
public class HomeController {

    @GetMapping("/demo")
    public String demo() {
        return "demo api";
    }

    @GetMapping("/test")
    @Tag(name = "Test" ,description = "this is test api")
    @SecurityRequirement(name="restful-demo-api")
    public String test() {
        return "test mapping";
    }
    
    
}
