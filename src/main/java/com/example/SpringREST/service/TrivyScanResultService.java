package com.example.SpringREST.service;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.SpringREST.model.TrivyScanResult;
import com.example.SpringREST.repository.TrivyScanResultRepository;

@Service
@RequiredArgsConstructor
public class TrivyScanResultService {

    private final TrivyScanResultRepository repo;

    public TrivyScanResult save(TrivyScanResult r) {
        return repo.save(r);
    }

    public List<TrivyScanResult> findByInputScanFileId(long id) {
        return repo.findByInputScanFile_id(id);
    }

    public void deleteAllForInput(long inputId) {
        findByInputScanFileId(inputId).forEach(repo::delete);
    }
}

