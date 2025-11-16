package com.example.devop.demo.shared.exception;

import com.example.devop.demo.shared.enums.ErrorCode;

import java.util.List;

public class ValidationException extends RuntimeException{
    private final List<String> errors;

    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public ValidationException(List<String> errors, ErrorCode errorCode) {
        super(String.join(", ", errors)); // fallback message
        this.errors = errors;
        this.errorCode = errorCode;
    }

    public List<String> getErrors() {
        return errors;
    }
}
