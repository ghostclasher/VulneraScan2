package com.example.SpringREST.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Represents a finding from a Gitleaks scan")
public class GitleaksFindingDTO {
    
    @Schema(description = "Unique identifier of the finding")
    private Long id;
    
    @Schema(description = "The rule that triggered this finding")
    private String rule;
    
    @Schema(description = "Description of the finding")
    private String description;
    
    @Schema(description = "Path to the file containing the finding")
    private String filePath;
    
    @Schema(description = "Starting line number where the finding was detected")
    private Integer startLine;
    
    @Schema(description = "Ending line number where the finding was detected")
    private Integer endLine;
    
    @Schema(description = "Hash of the detected secret (never the actual secret)")
    private String secretHash;
    
    @Schema(description = "Git commit hash if available")
    private String commit;
    
    @Schema(description = "Entropy value of the detected secret if available")
    private String entropy;
}