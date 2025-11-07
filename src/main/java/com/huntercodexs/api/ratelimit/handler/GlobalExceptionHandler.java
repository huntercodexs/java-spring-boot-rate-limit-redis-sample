package com.huntercodexs.api.ratelimit.handler;

import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Object> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        String errorMessage = "Limite de requisições excedido. Tente novamente mais tarde.";
        return new ResponseEntity<>(errorMessage, HttpStatus.TOO_MANY_REQUESTS);
    }
}
