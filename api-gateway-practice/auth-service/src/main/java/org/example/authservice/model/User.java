package org.example.authservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column
    private String username;
    @Column
    private String email;
    @Column
    private String password;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

}
