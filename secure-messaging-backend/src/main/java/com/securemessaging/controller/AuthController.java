package com.securemessaging.controller;

import com.securemessaging.dto.ChangePasswordRequest;
import com.securemessaging.dto.LoginRequest;
import com.securemessaging.dto.LoginResponse;
import com.securemessaging.dto.RegistrationRequest;
import com.securemessaging.service.AuthService;
import org.springframework.security.core.context.SecurityContextHolder;

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

        try {
            String token = authService.login(
                    request.username(),
                    request.password()
            );

            return ResponseEntity.ok(
                    new LoginResponse(token)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "error",
                            "Invalid username or password"
                    )
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest request) {

        try {
            authService.register(
                    request.username(),
                    request.email(),
                    request.password(),
                    request.invitationToken()
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

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request) {

        String currentUsername =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        try {
            authService.changePassword(
                    currentUsername,
                    request.currentPassword(),
                    request.newPassword()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Password changed successfully"
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            e.getMessage()
                    )
            );
        }
    }

}
