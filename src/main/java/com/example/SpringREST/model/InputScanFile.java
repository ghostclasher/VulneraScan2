package com.example.SpringREST.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InputScanFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private long fileSize;

    private LocalDateTime uploadDate;
    private String status;          // e.g. "UPLOADED", "SCANNED", "FAILED"
    private String scanResultPath;  // path to JSON or report file

    // Many InputScanFile entries belong to one Account
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}


