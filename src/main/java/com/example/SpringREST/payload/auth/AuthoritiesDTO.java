package com.example.SpringREST.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthoritiesDTO {
     @Schema(description = "Authorites  of the account", requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorities;
}
