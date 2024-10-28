package org.example.authservice.model.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String email;
}
