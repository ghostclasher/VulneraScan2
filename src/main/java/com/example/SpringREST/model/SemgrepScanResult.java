package com.example.SpringREST.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SemgrepScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String ruleId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String message; // allows long Semgrep messages

    private String severity;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String filePath; // optional: for long file paths

    private int startLine;
    private int endLine;

    @ManyToOne
    @JoinColumn(name = "input_scan_file_id", referencedColumnName = "id", nullable = false)
    private InputScanFile inputScanFile;
}

