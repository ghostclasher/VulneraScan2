package com.example.SpringREST.payload.Album;

import java.util.List;

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
public class AlbumViewDTO {
    
    private long id;
    
    @NotBlank
    @Schema(description = "Name of album", example = "Travel" , requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Description of album", example = "Description" , requiredMode = Schema.RequiredMode.REQUIRED)
     private String description;
    
     private List<PhotoDTO> photos;
}
