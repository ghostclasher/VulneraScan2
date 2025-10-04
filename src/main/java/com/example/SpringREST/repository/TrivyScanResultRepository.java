package com.example.SpringREST.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SpringREST.model.TrivyScanResult;

@Repository
public interface TrivyScanResultRepository extends JpaRepository<TrivyScanResult, Long> {
    List<TrivyScanResult> findByInputScanFile_id(long fileId);
}
