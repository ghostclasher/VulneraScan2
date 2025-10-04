package com.example.SpringREST.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SpringREST.model.InputScanFile;
import com.example.SpringREST.repository.InputScanFileRepository;

@Service
public class InputScanFileService {

    @Autowired
    private InputScanFileRepository inputScanFileRepository;

    public InputScanFile save(InputScanFile inputScanFile) {
        return inputScanFileRepository.save(inputScanFile);
    }

    public Optional<InputScanFile> findById(long id) {
        return inputScanFileRepository.findById(id);
    }

    public List<InputScanFile> findByAccount_Id(long accountId) {
        return inputScanFileRepository.findByAccount_Id(accountId);
    }

    public void delete(InputScanFile inputScanFile) {
        inputScanFileRepository.delete(inputScanFile);
    }
}

