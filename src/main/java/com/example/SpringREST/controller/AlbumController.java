package com.example.SpringREST.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.model.Album;
import com.example.SpringREST.model.Photo;
import com.example.SpringREST.payload.Album.AlbumPayloadDTO;
import com.example.SpringREST.payload.Album.AlbumViewDTO;
import com.example.SpringREST.payload.Album.PhotoDTO;
import com.example.SpringREST.payload.Album.PhotoPayloadDTO;
import com.example.SpringREST.payload.Album.PhotoViewDTO;
import com.example.SpringREST.service.Accountservice;
import com.example.SpringREST.service.AlbumService;
import com.example.SpringREST.service.PhotoService;
import com.example.SpringREST.util.AppUtils.AppUtil;
import com.example.SpringREST.util.constants.AlbumError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/album")
@Tag(name = "Album Controller", description = "Controller for Album and photo management")
@Slf4j
public class AlbumController {

    static final String PHOTOS_FOLDER_NAME = "photos";
    static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    static final int THUMBNAIL_WIDTH = 300;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private AlbumService albumservice;

    @Autowired
    private Accountservice accountservice;

    // API To add album in account

    @PostMapping(value = "/albums/add", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "please add valid name and description")
    @ApiResponse(responseCode = "201", description = "album added successfully")
    @Operation(summary = "Add Album")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<AlbumViewDTO> addAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO,
            Authentication authentication) {
        try {
            Album album = new Album();
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            String email = authentication.getName();
            Optional<Account> optionalaccount = accountservice.findByEmail(email);
            Account account = optionalaccount.get();
            album.setAccount(account);
            album = albumservice.save(album);
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(),null);
            return ResponseEntity.ok(albumViewDTO);
        } catch (Exception e) {
            log.debug(AlbumError.ALBUM_ADD_ERROR.toString() + ":" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // API  to get all albums of an account
    @GetMapping("/albums")
    @ApiResponse(responseCode = "200", description = "Albums retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Toke Missing")
    @Operation(summary = "Get Albums")
    @SecurityRequirement(name = "restful-demo-api")
    public List<AlbumViewDTO> getAlbums(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountservice.findByEmail(email);
        Account account = optionalAccount.get();
        List<AlbumViewDTO> albums = new ArrayList<>();
        for (Album album : albumservice.findByAccountId(account.getId())) {
            List<PhotoDTO>photos=new ArrayList<>();
            for(Photo photo:photoService.findByAlbum_id(album.getId())){
              String link="/"+album.getId()+"/photos/"+photo.getId()+"/download-photo";
              photos.add(new PhotoDTO(photo.getId(),photo.getName(), photo.getDescription(), photo.getFileName(),link));
            }
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(),photos);
            albums.add(albumViewDTO);
             
        }

        return albums;
    }


       // API  to get specific  album by album_id of an account
    @GetMapping("/albums/{album_id}")
    @ApiResponse(responseCode = "200", description = "Albums retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Toke Missing")
    @Operation(summary = "Get Album by album_id")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<AlbumViewDTO> getAlbumById(@PathVariable("album_id") long album_id,Authentication authentication) {
         String email=authentication.getName();
         Optional<Account>optionalaccount=accountservice.findByEmail(email);
         Account account=optionalaccount.get();
         Album album;
         Optional<Album>optionalalbum=albumservice.findById(album_id);
         if(optionalalbum.isPresent()){
            album=optionalalbum.get();
         }
         else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
         }
         if(account.getId()!=album.getAccount().getId()){
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
         }
         List<PhotoDTO>photos=new ArrayList<>();
         for(Photo photo:photoService.findByAlbum_id(album_id)){
            String link="/"+album.getId()+"/photos/"+photo.getId()+"/download-photo";
              photos.add(new PhotoDTO(photo.getId(),photo.getName(), photo.getDescription(), photo.getFileName(),link));
         }
         AlbumViewDTO albumviewDTO=new AlbumViewDTO(album.getId(),album.getName(),album.getDescription(),photos);
         return ResponseEntity.ok(albumviewDTO);
    }


    // API TO UPDATE ALBUM USING ALBUM_ID
     @PostMapping(value = "/albums/{album_id}/update", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "please add valid name and description")
    @ApiResponse(responseCode = "201", description = "album added successfully")
    @Operation(summary = "update Album using album_id")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<AlbumViewDTO> updateAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO,
            Authentication authentication,@PathVariable("album_id")long album_id) {
        try {
             
            String email = authentication.getName();
            Optional<Account> optionalaccount = accountservice.findByEmail(email);
            Account account = optionalaccount.get();
            Album album ;
            Optional<Album>optionalalbum=albumservice.findById(album_id);
            if(optionalalbum.isPresent()){
                album=optionalalbum.get();
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            if(album.getAccount().getId()!=account.getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            album.setAccount(account);
               List<PhotoDTO>photos=new ArrayList<>();
         for(Photo photo:photoService.findByAlbum_id(album_id)){
            String link="/"+album.getId()+"/photos/"+photo.getId()+"/download-photo";
              photos.add(new PhotoDTO(photo.getId(),photo.getName(), photo.getDescription(), photo.getFileName(),link));
         }
         album = albumservice.save(album);
         AlbumViewDTO albumviewDTO=new AlbumViewDTO(album.getId(),album.getName(),album.getDescription(),photos);
         return ResponseEntity.ok(albumviewDTO);
           
        } catch (Exception e) {
            log.debug(AlbumError.ALBUM_ADD_ERROR.toString() + ":" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



    // API TO UPDATE PHOTO USING ALBUM_ID AND PHOTO_ID
     @PutMapping("/{album_id}/photos/{photo_id}/update-photo")
     @ResponseStatus(HttpStatus.CREATED)
     @Operation(summary = "update photos in album")
     @SecurityRequirement(name = "restful-demo-api")
     public ResponseEntity<PhotoViewDTO>updatePhoto( @Valid @RequestBody PhotoPayloadDTO photoPayloadDTO,@PathVariable("album_id"
     )long album_id,@PathVariable("photo_id")long photo_id,Authentication authentication){
          try {
             
            String email = authentication.getName();
            Optional<Account> optionalaccount = accountservice.findByEmail(email);
            Account account = optionalaccount.get();
            Album album ;
            Optional<Album>optionalalbum=albumservice.findById(album_id);
            if(optionalalbum.isPresent()){
                album=optionalalbum.get();
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            if(album.getAccount().getId()!=account.getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
             Optional<Photo>optionalphoto=photoService.findById(photo_id);
              if(optionalphoto.isPresent()){
                Photo photo=optionalphoto.get();
                if(photo.getAlbum().getId()!=album_id){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                photo.setName(photoPayloadDTO.getName());
                photo.setDescription(photoPayloadDTO.getDescription());
                photoService.save(photo);
                PhotoViewDTO photoViewDTO=new PhotoViewDTO(photo.getId(), photo.getName(), photo.getDescription());
                return ResponseEntity.ok(photoViewDTO);
              }
              else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
              }
         
           
        } catch (Exception e) {
            log.debug(AlbumError.ALBUM_ADD_ERROR.toString() + ":" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

     }



    //  API TO DELETE PHOTO FROM GIVEN ALBUM
    @DeleteMapping(value = "/{album_id}/photos/{photo_id}/delete-photo")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "delete photos in album")
     @SecurityRequirement(name = "restful-demo-api")
     public ResponseEntity<String>delete_photo(@PathVariable("album_id")long album_id,@PathVariable("photo_id") long photo_id,Authentication authentication){
        try {
             
            String email = authentication.getName();
            Optional<Account> optionalaccount = accountservice.findByEmail(email);
            Account account = optionalaccount.get();
            Album album ;
            Optional<Album>optionalalbum=albumservice.findById(album_id);
            if(optionalalbum.isPresent()){
                album=optionalalbum.get();
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            if(album.getAccount().getId()!=account.getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
             Optional<Photo>optionalphoto=photoService.findById(photo_id);
              if(optionalphoto.isPresent()){
                Photo photo=optionalphoto.get();
                if(photo.getAlbum().getId()!=album_id){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                 AppUtil.delete_photo_from_path(photo.getFileName(),PHOTOS_FOLDER_NAME,album_id);
                 AppUtil.delete_photo_from_path(photo.getFileName(),THUMBNAIL_FOLDER_NAME,album_id);
                 photoService.delete(photo);
                 return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
              }
              else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
              }
         
           
        } catch (Exception e) {
            log.debug(AlbumError.ALBUM_ADD_ERROR.toString() + ":" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
     }



    // API TO UPLOAD PHOTOS IN ALBUM.
    @PostMapping(value = "/{album_id}/upload-photos", consumes = { "multipart/form-data" })
    @Operation(summary = "upload photos in album")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<List<HashMap<String, List<String>>>> uploadphotos(
            @RequestPart(required = true) MultipartFile[] files, @PathVariable long album_id,
            Authentication authentication) {
        // fetch account of looged in user
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountservice.findByEmail(email);
        Account account = optionalAccount.get();
        // fetch album from album id
        Optional<Album> optionalAlbum = albumservice.findById(album_id);
        Album album;
        if (optionalAlbum.isPresent()) {
            album = optionalAlbum.get();
            if (album.getAccount().getId() != account.getId()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<String> filenameswithSuccess = new ArrayList<>();
        List<String> filenameswithError = new ArrayList<>();
        Arrays.asList(files).stream().forEach(file -> {
            String contentType = file.getContentType();
            if (contentType.equals("image/jpg") ||
                    contentType.equals("image/jpeg") ||
                    contentType.equals("image/png") ||
                    contentType.equals("image/gif")) {
                filenameswithSuccess.add(file.getOriginalFilename());
                int length = 10;
                boolean useLetters = true;
                boolean useNumbers = true;
                try {
                    String fileName = file.getOriginalFilename();
                    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
                    String final_photo_name = generatedString + fileName;
                    String absolute_fileLocation = AppUtil.get_photo_upload_path(final_photo_name, album_id,
                            PHOTOS_FOLDER_NAME);
                    Path path = Paths.get(absolute_fileLocation);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    Photo photo = new Photo();
                    photo.setAlbum(album);
                    photo.setName(fileName);
                    photo.setOriginalFileName(fileName);
                    photo.setFileName(fileName);
                    photoService.save(photo);

                    BufferedImage thumbImg = AppUtil.getThumbnail(file, THUMBNAIL_WIDTH);
                    File thumbnail_location = new File(
                            AppUtil.get_photo_upload_path(final_photo_name, album_id, THUMBNAIL_FOLDER_NAME));
                    ImageIO.write(thumbImg, file.getContentType().split("/")[1], thumbnail_location);
                } catch (Exception e) {
                    log.debug(AlbumError.PHOTO_UPLOAD_ERROR.toString() + ": " + e.getMessage());
                    filenameswithError.add(file.getOriginalFilename());
                }

            } else {
                filenameswithError.add(file.getOriginalFilename());
            }

        });

        HashMap<String, List<String>> result = new HashMap<>();
        result.put("SUCCESS", filenameswithSuccess);
        result.put("ERRORS", filenameswithError);

        List<HashMap<String, List<String>>> response = new ArrayList<>();
        response.add(result);

        return ResponseEntity.ok(response);
    }

    // API to download photo from album
    @GetMapping("/{album_id}/photos/{photo_id}/download-photo")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<?> downloadPhoto(@PathVariable("album_id") long album_id,
            @PathVariable("photo_id") long photo_id, Authentication authentication) {
        return downloadFile(album_id, photo_id, PHOTOS_FOLDER_NAME, authentication);
    }

    // API to download thumbnail from album
    @GetMapping("/{album_id}/photos/{photo_id}/download-thumbnail")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<?> downloadThumbnail(@PathVariable("album_id") long album_id,
            @PathVariable("photo_id") long photo_id, Authentication authentication) {
        return downloadFile(album_id, photo_id, THUMBNAIL_FOLDER_NAME, authentication);
    }

    public ResponseEntity<?> downloadFile(long album_id, long photo_id, String folder_name,
            Authentication authentication) {

        String email = authentication.getName();
        Optional<Account> optionalAccount = accountservice.findByEmail(email);
        Account account = optionalAccount.get();

        Optional<Album> optionalalbum = albumservice.findById(album_id);
        Album album;
        if (optionalalbum.isPresent()) {
            album = optionalalbum.get();
            if (account.getId() != album.getAccount().getId()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // now acces the photo after checking if logged in user is owner of album also
        Optional<Photo> optionalPhoto = photoService.findById(photo_id);
        if (optionalPhoto.isPresent()) {
            Photo photo = optionalPhoto.get();
            Resource resource = null;
            try {
                resource = AppUtil.getFileAsResource(album_id, PHOTOS_FOLDER_NAME, photo.getFileName());
            } catch (IOException e) {
                return ResponseEntity.internalServerError().build();
            }

            if (resource == null) {
                return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
            }

            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + photo.getOriginalFileName() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }

}
