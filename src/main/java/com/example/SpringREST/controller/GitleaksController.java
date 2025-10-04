package com.example.SpringREST.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.model.GitleaksFinding;
import com.example.SpringREST.model.InputScanFile;
import com.example.SpringREST.payload.GitleaksFindingDTO;
import com.example.SpringREST.payload.ScanOutput.GitleaksScanResultDTO;
import com.example.SpringREST.service.GitleaksFindingService;
import com.example.SpringREST.service.InputScanFileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/gitleaks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gitleaks", description = "Gitleaks secret scanning endpoints")
public class GitleaksController {

    private final InputScanFileService inputScanFileService;
    private final GitleaksFindingService gitleaksFindingService;
    private static final int SCAN_TIMEOUT_MINUTES = 2;
    private static final String GITLEAKS_PATH = "C:\\Users\\udayg\\scoop\\apps\\gitleaks\\current\\gitleaks.exe";

    @Value("${gitleaks.binary.path}")
    private String gitleaksBinaryPath;

    @PostMapping("/scan/{fileId}")
    @Operation(summary = "Run Gitleaks scan on uploaded file", description = "Scans file for secrets using Gitleaks")
    @SecurityRequirement(name = "restful-demo-api")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scan completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<?> scanWithGitleaks(
            @PathVariable Long fileId,
            Authentication authentication) {

        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            final String currentUserEmail = authentication.getName();

            // File existence check
            InputScanFile inputFile = validateAndGetInputFile(fileId, currentUserEmail);
            if (inputFile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            Path tempScanDir = null;
            try {
                // Create and prepare temp directory
                tempScanDir = createAndPrepareTempDirectory(inputFile);
                
                // Run Gitleaks scan
                GitleaksScanResultDTO scanResult = runGitleaksScan(tempScanDir, inputFile);
                return ResponseEntity.ok(scanResult);

            } finally {
                cleanupTempDirectory(tempScanDir);
            }

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error during Gitleaks scan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Gitleaks scan: " + e.getMessage());
        }
    }

    private InputScanFile validateAndGetInputFile(Long fileId, String currentUserEmail) {
        Optional<InputScanFile> opt = inputScanFileService.findById(fileId);
        if (opt.isEmpty()) {
            return null;
        }

        InputScanFile inputFile = opt.get();
        Account owner = inputFile.getAccount();
        if (owner == null || !currentUserEmail.equals(owner.getEmail())) {
            throw new SecurityException("You are not allowed to scan this file");
        }

        Path stored = Path.of(inputFile.getFilePath());
        if (!Files.exists(stored)) {
            throw new IllegalStateException("Stored file not found: " + stored);
        }

        return inputFile;
    }

    private Path createAndPrepareTempDirectory(InputScanFile inputFile) throws IOException {
        Path tempScanDir = Files.createTempDirectory("gitleaks_scan_");
        Path stored = Path.of(inputFile.getFilePath());

        // Create a proper Java file with .java extension
        Path targetFile = tempScanDir.resolve("Test.java");
        Files.copy(stored, targetFile);

        log.info("Created temp scan directory at: {}", tempScanDir);
        log.info("Copied file to: {}", targetFile);

        return tempScanDir;
    }

    private void extractZipFile(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path target = targetDir.resolve(entry.getName()).normalize();
                    if (!target.startsWith(targetDir)) {
                        throw new SecurityException("Zip slip attempt detected");
                    }
                    if (target.getParent() != null) {
                        Files.createDirectories(target.getParent());
                    }
                    Files.copy(zis, target);
                }
            }
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(src -> {
            try {
                Path dest = target.resolve(source.relativize(src));
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(src, dest);
                }
            } catch (IOException e) {
                log.warn("Error copying file: {}", e.getMessage());
            }
        });
    }

    private String captureProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip log lines that start with timestamp
                if (!line.matches("\\d{1,2}:\\d{2}(AM|PM)\\s+(INF|WRN|ERR).*")) {
                    output.append(line).append("\n");
                }
            }
        }
        return output.toString().trim();
    }

    private GitleaksScanResultDTO runGitleaksScan(Path scanDir, InputScanFile inputFile) throws Exception {
        // Clear previous findings
        gitleaksFindingService.deleteAllForInput(inputFile.getId());

        ProcessBuilder pb = new ProcessBuilder(
                gitleaksBinaryPath,
                "detect",
                "--source", scanDir.toString(),
                "--report-format", "json",
                "--no-banner",
                "--no-color",
                "--verbose",
                "--no-git"  // Add this flag to scan without git repository
        );
        pb.redirectErrorStream(true);

        // Add detailed logging
        log.info("Scanning directory: {}", scanDir);
        log.info("GitLeaks command: {}", String.join(" ", pb.command()));
        
        Process process = pb.start();
        String output = captureProcessOutput(process);
        log.info("GitLeaks raw output: {}", output);

        if (!process.waitFor(SCAN_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new RuntimeException("GitLeaks scan timed out after " + SCAN_TIMEOUT_MINUTES + " minutes");
        }

        int exitCode = process.exitValue();
        // GitLeaks returns 1 when findings exist, which is not an error
        if (exitCode > 1) {
            throw new RuntimeException("GitLeaks scan failed with exit code " + exitCode);
        }

        List<GitleaksFindingDTO> findings = processGitleaksOutput(output, inputFile);
        
        GitleaksScanResultDTO response = new GitleaksScanResultDTO();
        response.setScannedFile(inputFile.getFilePath());
        response.setFindings(findings);
        return response;
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir != null) {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                log.warn("Failed to delete: {}", p);
                            }
                        });
            } catch (IOException e) {
                log.warn("Failed to cleanup temp dir: {}", e.getMessage());
            }
        }
    }

    private static String safeText(JsonNode n, String key) {
        JsonNode v = n.path(key);
        return v.isMissingNode() ? null : v.asText(null);
    }

    private static int safeInt(JsonNode n, String key, int def) {
        JsonNode v = n.path(key);
        return v.isMissingNode() ? def : (v.isInt() ? v.asInt(def) : def);
    }

    private List<GitleaksFindingDTO> processGitleaksOutput(String output, InputScanFile inputFile) {
        List<GitleaksFindingDTO> findings = new ArrayList<>();
        
        try {
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("Finding:")) {
                    GitleaksFinding finding = new GitleaksFinding();
                    
                    // Parse the finding block (4 lines: Finding, Secret, RuleID, File)
                    String secret = null;
                    String ruleId = null;
                    String file = null;
                    String entropy = null;
                    int lineNumber = -1;
                    
                    // Look ahead for related lines
                    for (int i = 0; i < 6 && i < lines.length; i++) {
                        String currentLine = lines[i].trim();
                        if (currentLine.startsWith("Secret:")) {
                            secret = currentLine.substring("Secret:".length()).trim();
                        } else if (currentLine.startsWith("RuleID:")) {
                            ruleId = currentLine.substring("RuleID:".length()).trim();
                        } else if (currentLine.startsWith("File:")) {
                            file = currentLine.substring("File:".length()).trim();
                        } else if (currentLine.startsWith("Line:")) {
                            lineNumber = Integer.parseInt(currentLine.substring("Line:".length()).trim());
                        } else if (currentLine.startsWith("Entropy:")) {
                            entropy = currentLine.substring("Entropy:".length()).trim();
                        }
                    }
                    
                    // Set finding properties
                    finding.setRule(ruleId);
                    finding.setDescription("Secret found: " + ruleId);
                    finding.setFilePath(file);
                    finding.setStartLine(lineNumber);
                    finding.setEndLine(lineNumber);
                    finding.setSecretHash(hashSha256(secret));
                    finding.setEntropy(entropy);
                    finding.setInputScanFile(inputFile);
                    
                    // Save and add to results
                    GitleaksFinding saved = gitleaksFindingService.save(finding);
                    findings.add(mapToDTO(saved));
                }
            }
            
            return findings;
        } catch (Exception e) {
            log.error("Failed to process Gitleaks output: {}", e.getMessage());
            throw new RuntimeException("Failed to process Gitleaks output: " + e.getMessage());
        }
    }

    private String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to hash secret", e);
            return null;
        }
    }

    private GitleaksFindingDTO mapToDTO(GitleaksFinding finding) {
        GitleaksFindingDTO dto = new GitleaksFindingDTO();
        dto.setId(finding.getId());
        dto.setRule(finding.getRule());
        dto.setDescription(finding.getDescription());
        dto.setFilePath(finding.getFilePath());
        dto.setStartLine(finding.getStartLine());
        dto.setEndLine(finding.getEndLine());
        dto.setSecretHash(finding.getSecretHash());
        dto.setEntropy(finding.getEntropy());
        return dto;
    }
}