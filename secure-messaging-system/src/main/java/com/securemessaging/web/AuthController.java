package com.securemessaging.web;

import com.securemessaging.dto.AuthResponse;
import com.securemessaging.dto.LoginRequest;
import com.securemessaging.security.JwtService;
import com.securemessaging.service.DatabaseUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final DatabaseUserService userService;
    private final JwtService jwtService = new JwtService();

    public AuthController(DatabaseUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            boolean valid = userService.validateLogin(
                    request.username(),
                    request.password()
            );

            if (!valid) {
                return ResponseEntity.status(401).body(
                        Map.of("error", "Invalid username or password")
                );
            }

            String token = jwtService.generateToken(request.username());

            return ResponseEntity.ok(
                    new AuthResponse(request.username(), token)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(
                Map.of("status", "Logged out successfully. Please clear the JWT token on the frontend.")
        );
    }
}