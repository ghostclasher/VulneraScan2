package com.example.SpringREST.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GitleaksFinding {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String rule;         // rule id/name
    @Lob
    private String description;  // full description (LOB to avoid length issues)
    private String filePath;     // path inside scanned tree
    private Integer startLine;
    private Integer endLine;

    @Lob
    private String secretHash;   // hashed/redacted secret snippet (do NOT store raw secrets)

    private String commit;       // optional commit id if present
    private String entropy;      // optional

    @ManyToOne
    @JoinColumn(name = "input_scan_file_id", referencedColumnName = "id", nullable = false)
    private InputScanFile inputScanFile;
}

