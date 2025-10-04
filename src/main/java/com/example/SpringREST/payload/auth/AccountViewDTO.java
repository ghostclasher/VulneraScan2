package com.example.SpringREST.payload.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountViewDTO {
 
    private long id;

    private String email;

    private String authorities;
}
// This class is used to view account details without exposing sensitive information like password.
