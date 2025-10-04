package com.example.SpringREST.payload.ScanOutput;

import java.util.List;
import com.example.SpringREST.payload.TrivyFindingDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrivyScanResultDTO {
    private String scannedFile;
    private List<TrivyFindingDTO> findings;
}

