package com.example.SpringREST.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SpringREST.model.GitleaksFinding;

@Repository
public interface GitleaksFindingRepository extends JpaRepository<GitleaksFinding, Long> {
    List<GitleaksFinding> findByInputScanFile_id(long fileId);
}

