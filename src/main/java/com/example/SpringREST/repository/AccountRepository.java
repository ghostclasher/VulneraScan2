package com.example.SpringREST.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SpringREST.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long>{
    // method findByEmail(String email);
    Optional<Account> findByEmail(String email);
    
}
