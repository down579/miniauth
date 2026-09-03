package com.example.miniauth.controller;

import com.example.miniauth.dto.auth.LoginRequest;
import com.example.miniauth.dto.auth.MeResponse;
import com.example.miniauth.dto.auth.SignupRequest;
import com.example.miniauth.dto.auth.SignupResponse;
import com.example.miniauth.security.CustomUserDetails;
import com.example.miniauth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public MeResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return authService.login(request, httpRequest, httpResponse);
    }

    @GetMapping("/me")
    public MeResponse me(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return MeResponse.from(principal);
    }
}