package com.example.SpringREST.payload.Album;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PhotoPayloadDTO {
    @NotBlank
    @Schema(description = "Photo Name",example = "Selfie",requiredMode = RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "description of photo",example = "description",requiredMode = RequiredMode.REQUIRED)
    private String description;
}
