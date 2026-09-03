package com.example.miniauth.service;

import com.example.miniauth.domain.Role;
import com.example.miniauth.domain.User;
import com.example.miniauth.dto.auth.LoginRequest;
import com.example.miniauth.dto.auth.MeResponse;
import com.example.miniauth.dto.auth.SignupRequest;
import com.example.miniauth.dto.auth.SignupResponse;
import com.example.miniauth.repository.RoleRepository;
import com.example.miniauth.repository.UserRepository;
import com.example.miniauth.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

import static org.springframework.util.StringUtils.truncate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER가 존재하지 않습니다."));
        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim()
        );
        user.addRole(userRole);

        return SignupResponse.from(userRepository.save(user));
    }

    public MeResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String ip = extractClientIp(httpRequest);
        String userAgent = truncate(httpRequest.getHeader("User-Agent"), 512);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            email,
                            request.password()
                    )
            );
        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(
                    email, ip, userAgent, resolveFailureReason(e)
            );
            throw e;
        }
        // 기존 세션 교체 및 SecurityContext 저장 로직
        loginAttemptService.recordSuccess(email, ip, userAgent);
        return MeResponse.from((CustomUserDetails) authentication.getPrincipal());
    }
    private String resolveFailureReason(AuthenticationException e) {
        if (e instanceof LockedException) {
            return "LOCKED";
        }
        if (e instanceof DisabledException) {
            return "DISABLED";
        }
        return "BAD_CREDENTIALS";
    }
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}