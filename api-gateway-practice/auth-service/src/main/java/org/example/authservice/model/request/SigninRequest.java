package org.example.authservice.model.request;

import lombok.Data;

@Data
public class SigninRequest {
    private String username;
    private String password;
}
