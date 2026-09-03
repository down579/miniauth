package com.example.miniauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleConflict(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }
    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLocked(LockedException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "계정이 잠겼습니다. 잠시 후 다시 시도해 주세요."
        );
    }
    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled(DisabledException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "사용할 수 없는 계정입니다."
        );
    }
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthFailure(AuthenticationException e) {
        // 이메일 존재 여부를 노출하지 않도록 메시지를 통일
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 올바르지 않습니다."
        );
    }
}