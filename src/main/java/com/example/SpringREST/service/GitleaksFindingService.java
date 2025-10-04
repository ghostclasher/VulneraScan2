package com.example.SpringREST.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.SpringREST.model.GitleaksFinding;
import com.example.SpringREST.repository.GitleaksFindingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitleaksFindingService {
    private final GitleaksFindingRepository repo;

    public GitleaksFinding save(GitleaksFinding f) { return repo.save(f); }

    public List<GitleaksFinding> findByInputScanFileId(long id) {
        return repo.findByInputScanFile_id(id);
    }

    public void deleteAllForInput(long inputId) {
        findByInputScanFileId(inputId).forEach(repo::delete);
    }
}
