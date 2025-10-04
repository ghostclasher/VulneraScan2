package com.example.SpringREST.repository;

import com.example.SpringREST.model.SemgrepScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemgrepScanResultRepository extends JpaRepository<SemgrepScanResult, Long> {
    List<SemgrepScanResult> findByInputScanFile_Id(Long inputScanFileId);
}

