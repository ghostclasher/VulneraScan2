package com.example.SpringREST.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SemgrepFindingDTO {
    private String ruleId;
    private String message;
    private String severity;
    private String filePath;
    private int startLine;
    private int endLine;
}
