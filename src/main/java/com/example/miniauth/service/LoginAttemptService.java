package com.example.miniauth.service;

import com.example.miniauth.domain.LoginAttempt;
import com.example.miniauth.domain.User;
import com.example.miniauth.repository.LoginAttemptRepository;
import com.example.miniauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String email, String ip, String userAgent) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.recordLoginSuccess();
        }
        loginAttemptRepository.save(
                LoginAttempt.success(email, user, ip, userAgent)
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            String email,
            String ip,
            String userAgent,
            String reason
    ) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && "BAD_CREDENTIALS".equals(reason)) {
            user.recordLoginFailure();
        }
        loginAttemptRepository.save(
                LoginAttempt.failure(email, user, ip, userAgent, reason)
        );
    }
}