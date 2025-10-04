package com.example.SpringREST.payload.ScanOutput;

import java.util.List;

import com.example.SpringREST.payload.SemgrepFindingDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SemgrepScanResultDTO {
    private String scannedFile;
    private List<SemgrepFindingDTO> findings;
}
