package com.example.SpringREST.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SpringREST.model.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album,Long>{
    public List<Album> findByAccountId(long account_id);
    
}
