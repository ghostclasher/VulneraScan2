package com.example.SpringREST.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PasswordDTO {
      
      @Size(min = 4 ,max=20)
     @Schema(description = " new password of the account", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newpassword;
}
