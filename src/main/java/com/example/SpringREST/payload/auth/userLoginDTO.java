package com.example.SpringREST.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class userLoginDTO {
   @Email
    @Schema(description = "Email addressof the account", example = "uday@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Size(min = 4 ,max=20)
     @Schema(description = "password of the account", example = "pass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    
}
