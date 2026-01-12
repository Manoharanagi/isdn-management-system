package com.isdn.controller;

import com.isdn.dto.request.LoginRequest;
import com.isdn.dto.request.RegisterRequest;
import com.isdn.dto.response.ApiResponse;
import com.isdn.dto.response.AuthResponse;
import com.isdn.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register - Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/auth/login - Login user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/test - Test authentication
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse> test() {
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Authentication is working!")
                .build();
        return ResponseEntity.ok(response);
    }
}