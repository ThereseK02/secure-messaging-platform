package com.securemessaging.controller;

import com.securemessaging.dto.LoginRequest;
import com.securemessaging.dto.LoginResponse;
import com.securemessaging.dto.RegistrationRequest;
import com.securemessaging.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping({"/users", "/api/users"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.username(),
                request.password()
        );

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest request) {

        try {
            authService.register(
                    request.username(),
                    request.email(),
                    request.password()
            );

            return ResponseEntity.ok(
                    "Registration successful"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}
