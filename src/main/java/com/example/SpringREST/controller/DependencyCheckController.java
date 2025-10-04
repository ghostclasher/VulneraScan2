package com.example.SpringREST.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.model.InputScanFile;
import com.example.SpringREST.service.InputScanFileService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/dependency-check")
@RequiredArgsConstructor
@Slf4j
public class DependencyCheckController {

    private final InputScanFileService inputScanFileService;

    private static final String DC_BIN_PATH =
            "C:\\Users\\udayg\\Downloads\\dependency-check-12.1.3-release\\dependency-check\\bin\\dependency-check.bat";
    private static final String REPORT_DIR = "C:\\Users\\udayg\\dependency-check-report";

    @PostMapping("/scan/{fileId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<Resource> scanFile(
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            final String currentUserEmail = authentication.getName();

            // Fetch uploaded file record
            Optional<InputScanFile> opt = inputScanFileService.findById(fileId);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            InputScanFile inputFile = opt.get();

            // Ownership check
            Account owner = inputFile.getAccount();
            if (owner == null || !currentUserEmail.equals(owner.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // Verify file exists
            Path path = Path.of(inputFile.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Handle ZIP files
            Path scanTarget;
            if (path.getFileName().toString().toLowerCase().endsWith(".zip")) {
                // Extract first file from ZIP for scanning (you can modify for multiple)
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
                    ZipEntry entry;
                    if ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                        scanTarget = Files.createTempFile("dependency_", "_" + entry.getName());
                        Files.copy(zis, scanTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                }
            } else {
                scanTarget = path;
            }

            // Run Dependency-Check CLI
            ProcessBuilder pb = new ProcessBuilder(
                    DC_BIN_PATH,
                    "--scan", scanTarget.toString(),
                    "--format", "HTML",
                    "--out", REPORT_DIR
            );
            Process process = pb.start();

            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = outReader.readLine()) != null) log.info(line);
                while ((line = errReader.readLine()) != null) log.warn(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            // Return report as download
            Path reportFile = Path.of(REPORT_DIR, "dependency-check-report.html");
            if (!Files.exists(reportFile)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            Resource resource = new UrlResource(reportFile.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + reportFile.getFileName() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            log.error("Error running Dependency-Check scan", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
