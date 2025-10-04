package com.example.SpringREST.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SpringREST.model.Album;
import com.example.SpringREST.repository.AlbumRepository;

@Service
public class AlbumService {
  
    @Autowired
    private AlbumRepository albumRepository;

    public Album save(Album album){
        return albumRepository.save(album);
    }
     public List<Album> findByAccountId(long account_id) {
        return albumRepository.findByAccountId(account_id);
     }

     public Optional<Album> findById(long id) {
        return albumRepository.findById(id);
     }
    
}
