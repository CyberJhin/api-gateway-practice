package org.example.userservice.service;

import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements CustomUserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // Метод для поиска пользователя по UUID
    public User findByUuid(String uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + uuid));
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDetailsImpl.build(user);
    }

    @Override
    public UserDetails loadUserByUuid(String uuid) throws UsernameNotFoundException {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDetailsImpl.build(user);
    }
}

