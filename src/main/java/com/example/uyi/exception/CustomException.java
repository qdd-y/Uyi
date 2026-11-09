package com.example.uyi.exception;

import lombok.Getter;

/**
 * @Author: qdd
 * @Description: CustomException
 * @DateTime: 2025/10/28 10:00
 **/
@Getter
public class CustomException extends RuntimeException {
    private int code;
    private String message;

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}