package com.example.SpringREST.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.model.InputScanFile;
import com.example.SpringREST.model.SemgrepScanResult;
import com.example.SpringREST.payload.SemgrepFindingDTO;
import com.example.SpringREST.payload.ScanOutput.SemgrepScanResultDTO;
import com.example.SpringREST.service.InputScanFileService;
import com.example.SpringREST.service.SemgrepScanResultService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/semgrep")
@RequiredArgsConstructor
@Slf4j
public class SemgrepController {

    private final InputScanFileService inputScanFileService;
    private final SemgrepScanResultService semgrepScanResultService;

    @PostMapping("/scan/{fileId}")
    @Operation(summary = "Run Semgrep on an uploaded file (owned by the logged-in user)")
    @SecurityRequirement(name = "restful-demo-api")
    public ResponseEntity<?> scanFile(
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            final String currentUserEmail = authentication.getName();

            // 1) Fetch uploaded file record
            Optional<InputScanFile> opt = inputScanFileService.findById(fileId);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }
            InputScanFile inputFile = opt.get();

            // 2) Ownership check
            Account owner = inputFile.getAccount();
            if (owner == null || !currentUserEmail.equals(owner.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to scan this file");
            }

            // 3) Verify file exists
            Path path = Path.of(inputFile.getFilePath());
            if (!Files.exists(path) || Files.isDirectory(path)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Stored file not found or is a directory: " + inputFile.getFilePath());
            }

            // 4) Handle ZIP files
            List<Path> filesToScan = new ArrayList<>();
            if (path.getFileName().toString().toLowerCase().endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            Path tempFile = Files.createTempFile("semgrep_", "_" + entry.getName());
                            Files.copy(zis, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            filesToScan.add(tempFile);
                        }
                    }
                }
            } else {
                filesToScan.add(path);
            }

            // 5) Run Semgrep for each file using inbuilt rules
            SemgrepScanResultDTO response = new SemgrepScanResultDTO();
            response.setScannedFile(inputFile.getFilePath());
            List<SemgrepFindingDTO> findings = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            for (Path scanPath : filesToScan) {
                ProcessBuilder pb = new ProcessBuilder(
                        "semgrep",
                        "--config", "auto",   // ✅ Use inbuilt rules instead of custom YAML
                        "--json",
                        "--quiet",
                        scanPath.toString()
                );
                Process process = pb.start();

                // Read stdout
                StringBuilder jsonOut = new StringBuilder();
                try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = out.readLine()) != null) {
                        jsonOut.append(line);
                    }
                }

                // Read stderr for logging
                StringBuilder errOut = new StringBuilder();
                try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = err.readLine()) != null) {
                        errOut.append(line).append('\n');
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0 && exitCode != 2) { // exit code 2 = findings found
                    log.warn("Semgrep exited with code {}. Stderr: {}", exitCode, errOut);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Semgrep failed (exit " + exitCode + "). Check server logs.");
                }

                // Parse JSON
                JsonNode root = mapper.readTree(jsonOut.toString());
                for (JsonNode finding : root.path("results")) {
                    String ruleId   = finding.path("check_id").asText(null);
                    String message  = finding.path("extra").path("message").asText(null);
                    String severity = finding.path("extra").path("severity").asText(null);
                    String filePath = finding.path("path").asText(null);
                    int startLine   = finding.path("start").path("line").asInt(-1);
                    int endLine     = finding.path("end").path("line").asInt(-1);

                    SemgrepScanResult entity = new SemgrepScanResult();
                    entity.setRuleId(ruleId);
                    entity.setMessage(message);
                    entity.setSeverity(severity);
                    entity.setFilePath(filePath);
                    entity.setStartLine(startLine);
                    entity.setEndLine(endLine);
                    entity.setInputScanFile(inputFile);
                    semgrepScanResultService.save(entity);

                    SemgrepFindingDTO dto = new SemgrepFindingDTO();
                    dto.setRuleId(ruleId);
                    dto.setMessage(message);
                    dto.setSeverity(severity);
                    dto.setFilePath(filePath);
                    dto.setStartLine(startLine);
                    dto.setEndLine(endLine);
                    findings.add(dto);
                }
            }

            response.setFindings(findings);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error running Semgrep scan", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Semgrep scan: " + ex.getMessage());
        }
    }
}


