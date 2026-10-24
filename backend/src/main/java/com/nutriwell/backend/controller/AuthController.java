package com.nutriwell.backend.controller;

import com.nutriwell.backend.dto.SignupRequest;
import com.nutriwell.backend.dto.SignupResponse;
import com.nutriwell.backend.model.User;
import com.nutriwell.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            User user = userService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new SignupResponse(user.userId(), user.username(), user.email(), "Account created successfully.")
            );
        } catch (UserService.UserAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", exception.getMessage()));
        }
    }
}
