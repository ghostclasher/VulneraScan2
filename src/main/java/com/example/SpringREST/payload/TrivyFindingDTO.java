package com.example.SpringREST.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrivyFindingDTO {
    private String vulnerabilityId;
    private String pkgName;
    private String installedVersion;
    private String fixedVersion;
    private String severity;
    private String title;
    private String primaryUrl;
    private String description;
    private String target;
}

