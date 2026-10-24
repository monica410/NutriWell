package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.SignupRequest;
import com.nutriwell.backend.model.User;
import com.nutriwell.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User signup(SignupRequest request) {
        String username = clean(request.username());
        String email = clean(request.email()).toLowerCase();
        String password = request.password() == null ? "" : request.password();

        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must contain at least 3 characters.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new UserAlreadyExistsException("Username or email is already registered.");
        }

        return userRepository.save(username, email, passwordEncoder.encode(password));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
