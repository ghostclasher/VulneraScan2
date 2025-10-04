package com.example.SpringREST.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SpringREST.model.InputScanFile;

@Repository
public interface InputScanFileRepository extends JpaRepository<InputScanFile, Long> {

    // Custom query method to fetch all files for a given account
    List<InputScanFile> findByAccount_Id(long accountId);
}

