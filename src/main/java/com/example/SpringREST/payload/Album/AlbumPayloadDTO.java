package com.example.SpringREST.payload.Album;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class AlbumPayloadDTO {


    @NotBlank
    @Schema(description = "Name of album" , example = "Travel",requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @NotBlank
    @Schema(description = "description of album" , example="description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
}
