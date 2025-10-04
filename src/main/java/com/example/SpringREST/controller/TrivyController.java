package com.example.SpringREST.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.model.InputScanFile;
import com.example.SpringREST.model.TrivyScanResult;
import com.example.SpringREST.payload.TrivyFindingDTO;
import com.example.SpringREST.payload.ScanOutput.TrivyScanResultDTO;
import com.example.SpringREST.service.InputScanFileService;
import com.example.SpringREST.service.TrivyScanResultService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/trivy")
@RequiredArgsConstructor
@Slf4j
public class TrivyController {

    private final InputScanFileService inputScanFileService;
    private final TrivyScanResultService trivyScanResultService;

    // If trivy isn't in PATH, set absolute path here (e.g. "C:\\Users\\udayg\\scoop\\apps\\trivy\\current\\trivy.exe")
    private static final String TRIVY_BINARY = "trivy"; 

    @PostMapping("/scan/{fileId}")
    @Operation(summary = "Run Trivy on an uploaded file/folder (owned by the logged-in user)")
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

            Optional<InputScanFile> opt = inputScanFileService.findById(fileId);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }
            InputScanFile inputFile = opt.get();

            Account owner = inputFile.getAccount();
            if (owner == null || !currentUserEmail.equals(owner.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to scan this file");
            }

            Path stored = Path.of(inputFile.getFilePath());
            if (!Files.exists(stored)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stored file not found: " + stored);
            }

            // Prepare scan targets (extract zip if needed)
            List<Path> toScan = new ArrayList<>();
            Path tempDir = Files.createTempDirectory("trivy_scan_" + UUID.randomUUID().toString());
            try {
                if (stored.getFileName().toString().toLowerCase().endsWith(".zip")) {
                    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(stored))) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (!entry.isDirectory()) {
                                Path target = tempDir.resolve(entry.getName()).normalize();
                                if (target.getParent() != null) Files.createDirectories(target.getParent());
                                Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                    toScan.add(tempDir);
                } else if (Files.isDirectory(stored)) {
                    // If it's a directory, scan it directly (or copy if isolation needed)
                    toScan.add(stored);
                } else {
                    // Single file -> copy into temp dir so Trivy can inspect it
                    Path dest = tempDir.resolve(stored.getFileName().toString());
                    Files.copy(stored, dest, StandardCopyOption.REPLACE_EXISTING);
                    toScan.add(tempDir);
                }

                // optional: remove old trivy findings for this input
                trivyScanResultService.deleteAllForInput(inputFile.getId());

                ObjectMapper mapper = new ObjectMapper();
                TrivyScanResultDTO response = new TrivyScanResultDTO();
                response.setScannedFile(inputFile.getFilePath());
                List<TrivyFindingDTO> findingsDto = new ArrayList<>();

                for (Path scanPath : toScan) {
                    List<String> cmd = new ArrayList<>();
                    cmd.add(TRIVY_BINARY);
                    cmd.add("fs");
                    cmd.add("--format");
                    cmd.add("json");
                    cmd.add("--quiet"); // suppress extra logs
                    cmd.add(scanPath.toString());

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.redirectErrorStream(true);
                    Process p = pb.start();

                    StringBuilder jsonOut = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            jsonOut.append(line);
                        }
                    }

                    boolean finished = p.waitFor(3, TimeUnit.MINUTES); // tune timeout as needed
                    if (!finished) {
                        p.destroyForcibly();
                        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Trivy timed out");
                    }
                    int exit = p.exitValue();
                    if (exit > 1) {
                        log.warn("Trivy failed exit {} output: {}", exit, jsonOut.toString());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Trivy failed (exit " + exit + "). Check server logs.");
                    }

                    if (jsonOut.length() == 0) {
                        // no JSON produced (no vulns and quiet suppressed output)
                        continue;
                    }

                    JsonNode root = mapper.readTree(jsonOut.toString());
                    // Trivy JSON has "Results": [ { "Target": "...", "Type": "...", "Vulnerabilities": [ ... ] }, ... ]
                    for (JsonNode resultNode : root.path("Results")) {
                        String target = resultNode.path("Target").asText(null);
                        for (JsonNode vuln : resultNode.path("Vulnerabilities")) {
                            String vulnId = vuln.path("VulnerabilityID").asText(null);
                            String pkgName = vuln.path("PkgName").asText(null);
                            String installed = vuln.path("InstalledVersion").asText(null);
                            String fixed = vuln.path("FixedVersion").asText(null);
                            String severity = vuln.path("Severity").asText(null);
                            String title = vuln.path("Title").asText(null);
                            String description = vuln.path("Description").asText(null);
                            String primaryUrl = vuln.path("PrimaryURL").asText(null);

                            TrivyScanResult ent = new TrivyScanResult();
                            ent.setVulnerabilityId(vulnId);
                            ent.setPkgName(pkgName);
                            ent.setInstalledVersion(installed);
                            ent.setFixedVersion(fixed);
                            ent.setSeverity(severity);
                            ent.setTitle(title);
                            ent.setDescription(description);
                            ent.setPrimaryUrl(primaryUrl);
                            ent.setTarget(target);
                            ent.setInputScanFile(inputFile);

                            trivyScanResultService.save(ent);

                            TrivyFindingDTO dto = new TrivyFindingDTO();
                            dto.setVulnerabilityId(vulnId);
                            dto.setPkgName(pkgName);
                            dto.setInstalledVersion(installed);
                            dto.setFixedVersion(fixed);
                            dto.setSeverity(severity);
                            dto.setTitle(title);
                            dto.setDescription(description);
                            dto.setPrimaryUrl(primaryUrl);
                            dto.setTarget(target);
                            findingsDto.add(dto);
                        }
                    }
                }

                response.setFindings(findingsDto);
                return ResponseEntity.ok(response);

            } finally {
                // cleanup
                try {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.deleteIfExists(p); } catch (Exception ex) { /* ignore */ }
                            });
                } catch (Exception e) {
                    log.warn("Failed cleaning temp dir: {}", e.getMessage());
                }
            }

        } catch (Exception ex) {
            log.error("Error running Trivy scan", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Trivy scan: " + ex.getMessage());
        }
    }
}

