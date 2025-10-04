package com.example.SpringREST.service;

import com.example.SpringREST.model.SemgrepScanResult;
import com.example.SpringREST.repository.SemgrepScanResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemgrepScanResultService {

    @Autowired
    private SemgrepScanResultRepository semgrepScanResultRepository;

    public SemgrepScanResult save(SemgrepScanResult scanResult) {
        return semgrepScanResultRepository.save(scanResult);
    }

    public List<SemgrepScanResult> findByInputScanFile(Long inputScanFileId) {
        return semgrepScanResultRepository.findByInputScanFile_Id(inputScanFileId);
    }
}
