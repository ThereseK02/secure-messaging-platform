package com.securemessaging.controller;

import com.securemessaging.dto.LoginRequest;
import com.securemessaging.dto.LoginResponse;
import com.securemessaging.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {

        authService.register(
                request.username(),
                request.password()
        );

        return ResponseEntity.ok("Registration successful");
    }
}
