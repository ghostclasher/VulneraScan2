package com.example.SpringREST.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.repository.AccountRepository;
import com.example.SpringREST.util.constants.Authority;
 @Service
public class Accountservice implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account save(Account account) {
        
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        if(account.getAuthorities() ==null){
            account.setAuthorities(Authority.USER.toString());
        }
        return accountRepository.save(account);
    }


    public List<Account> findAll(){
        return accountRepository.findAll();
    }

    public Optional<Account> findByEmail(String email){
return accountRepository.findByEmail(email);
    }

      public Optional<Account> findByID(long id){
return accountRepository.findById(id);
    }
         public void deleteByID(long id){
           accountRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Account> optionalaccount= accountRepository.findByEmail(email);
        if(optionalaccount.isPresent()){
            Account found= optionalaccount.get();
            List<GrantedAuthority> grantedauthority=new ArrayList<>();
            grantedauthority.add(new SimpleGrantedAuthority(found.getAuthorities()));
            return new User(found.getEmail(), found.getPassword(), grantedauthority);
        }
        else{
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
    }

    
}
