package com.example.uyi.exception;

import com.example.uyi.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: qdd
 * @Description: GlobalExceptionHandler
 * @DateTime: 2025/10/28 09:55
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Result handleCustomException(CustomException e) {
        log.error("CustomException: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(e.getCode(), e.getMessage())).getBody();
    }
}