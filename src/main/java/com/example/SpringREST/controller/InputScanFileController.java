package com.example.SpringREST.controller;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.model.InputScanFile;
import com.example.SpringREST.service.Accountservice;
import com.example.SpringREST.service.InputScanFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/scanfiles")
@Slf4j
public class InputScanFileController {

    private static final String UPLOAD_DIR = "uploaded_code_files";

    @Autowired
    private Accountservice accountService;

    @Autowired
    private InputScanFileService inputScanFileService;

    // 🔹 API to upload code files or zip archives
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload code files or zip archive for scanning")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<Map<String, List<String>>> uploadFiles(
            @RequestPart(required = true) MultipartFile[] files,
            Authentication authentication) {

        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);

        if (optionalAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Account account = optionalAccount.get();

        List<String> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        Arrays.asList(files).forEach(file -> {
            try {
                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.isEmpty()) {
                    errorFiles.add("Unknown file");
                    return;
                }

                if (fileName.endsWith(".zip")) {
                    // handle zip archive
                    String zipFolder = "zip_" + RandomStringUtils.randomAlphanumeric(6);
                    Path extractPath = Paths.get(UPLOAD_DIR, zipFolder);
                    Files.createDirectories(extractPath);

                    try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                        ZipEntry zipEntry;
                        while ((zipEntry = zis.getNextEntry()) != null) {
                            if (!zipEntry.isDirectory()) {
                                String extractedName = zipEntry.getName();
                                Path extractedFilePath = extractPath.resolve(extractedName);
                                Files.createDirectories(extractedFilePath.getParent());

                                Files.copy(zis, extractedFilePath, StandardCopyOption.REPLACE_EXISTING);

                                // save entry in DB
                                InputScanFile inputScanFile = new InputScanFile();
                                inputScanFile.setAccount(account);
                                inputScanFile.setOriginalFileName(extractedName);
                                inputScanFile.setStoredFileName(extractedName);
                                inputScanFile.setFilePath(extractedFilePath.toString());
                                inputScanFile.setFileSize(Files.size(extractedFilePath));
                                inputScanFile.setUploadDate(LocalDateTime.now());
                                inputScanFile.setStatus("UPLOADED");
                                inputScanFileService.save(inputScanFile);

                                successFiles.add(extractedName);
                            }
                        }
                    }

                } else {
                    // handle single code file
                    String randomPrefix = RandomStringUtils.randomAlphanumeric(8);
                    String storedName = randomPrefix + "_" + fileName;
                    Path targetPath = Paths.get(UPLOAD_DIR, storedName);
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                    InputScanFile inputScanFile = new InputScanFile();
                    inputScanFile.setAccount(account);
                    inputScanFile.setOriginalFileName(fileName);
                    inputScanFile.setStoredFileName(storedName);
                    inputScanFile.setFilePath(targetPath.toString());
                    inputScanFile.setFileSize(file.getSize());
                    inputScanFile.setUploadDate(LocalDateTime.now());
                    inputScanFile.setStatus("UPLOADED");
                    inputScanFileService.save(inputScanFile);

                    successFiles.add(fileName);
                }

            } catch (Exception e) {
                log.error("Error uploading file: " + file.getOriginalFilename(), e);
                errorFiles.add(file.getOriginalFilename());
            }
        });

        Map<String, List<String>> result = new HashMap<>();
        result.put("SUCCESS", successFiles);
        result.put("ERRORS", errorFiles);

        return ResponseEntity.ok(result);
    }

    // 🔹 API to list uploaded files of logged-in account
    @GetMapping("/my-uploads")
    @Operation(summary = "Get all uploaded files for logged-in account")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<List<InputScanFile>> getMyUploadedFiles(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);

        if (optionalAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Account account = optionalAccount.get();

        List<InputScanFile> uploadedFiles = inputScanFileService.findByAccount_Id(account.getId());
        return ResponseEntity.ok(uploadedFiles);
    }

    // 🔹 API to delete an uploaded file by ID
    @DeleteMapping("/delete/{fileId}")
    @Operation(summary = "Delete an uploaded file by ID")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<String> deleteUploadedFile(@PathVariable long fileId, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);

        if (optionalAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Account account = optionalAccount.get();

        Optional<InputScanFile> optionalFile = inputScanFileService.findById(fileId);
        if (optionalFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }

        InputScanFile file = optionalFile.get();

        // ensure file belongs to this account
        if (file.getAccount().getId() != account.getId()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not allowed");
        }

        try {
            // delete file from disk
            Path filePath = Paths.get(file.getFilePath());
            if (Files.exists(filePath)) {
                if (Files.isDirectory(filePath)) {
                    FileSystemUtils.deleteRecursively(filePath);
                } else {
                    Files.delete(filePath);
                }
            }

            // delete DB entry
            inputScanFileService.delete(file);

            return ResponseEntity.ok("File deleted successfully");
        } catch (IOException e) {
            log.error("Error deleting file: " + file.getFilePath(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file");
        }
    }
}


