package com.example.SpringREST.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.service.Accountservice;
import com.example.SpringREST.util.constants.Authority;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private Accountservice accountservice;

    @Override
    public void run(String... args) throws Exception {
         
        Account account01= new Account();
        Account account02= new Account();


        account01.setEmail("user@gmail.com");
        account01.setPassword("user");
        account01.setAuthorities(Authority.USER.toString());
        accountservice.save(account01);

         account02.setEmail("admin@gmail.com");
        account02.setPassword("admin");
        account02.setAuthorities(Authority.ADMIN.toString() + " " + Authority.USER.toString());
        accountservice.save(account02);

    }

}
