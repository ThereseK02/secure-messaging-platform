package com.securemessaging.service;

import com.securemessaging.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final DatabaseUserService databaseUserService;
    private final JwtUtil jwtUtil;

    public AuthService(DatabaseUserService databaseUserService,
                       JwtUtil jwtUtil) {
        this.databaseUserService = databaseUserService;
        this.jwtUtil = jwtUtil;
    }
    public void register(String username, String password) {
        try {
                databaseUserService.register(username, password);
        } catch (Exception e) {
                throw new RuntimeException("Registration failed: " + e.getMessage());
            }
        }
    public String login(String username, String password) {

        try {
            boolean valid = databaseUserService.validateLogin(username, password);

            if (!valid) {
                throw new RuntimeException("Invalid username or password");
            }

            return jwtUtil.generateToken(username);

        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
}