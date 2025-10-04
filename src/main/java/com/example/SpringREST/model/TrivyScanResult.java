package com.example.SpringREST.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrivyScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String vulnerabilityId;   // e.g. CVE-2021-44228 or GHSA...
    private String pkgName;
    private String installedVersion;
    private String fixedVersion;
    private String severity;
    private String title;
    private String primaryUrl;

    @Lob
    private String description;

    private String target; // target scanned (file path / artifact)
    private Integer startLine; // optional (if Trivy reports it)
    private Integer endLine;

    @ManyToOne
    @JoinColumn(name = "input_scan_file_id", referencedColumnName = "id", nullable = false)
    private InputScanFile inputScanFile;
}
