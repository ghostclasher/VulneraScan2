package com.example.SpringREST.payload.Album;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDTO {
    
    private long id;

    private String name;

    private String description;

    private String filename;

    private String download_link;
}
