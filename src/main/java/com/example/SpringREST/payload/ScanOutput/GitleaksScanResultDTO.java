package com.example.SpringREST.payload.ScanOutput;

import java.util.List;
import com.example.SpringREST.payload.GitleaksFindingDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitleaksScanResultDTO {
    private String scannedFile;          // path of the uploaded file (or zip)
    private List<GitleaksFindingDTO> findings;
}
