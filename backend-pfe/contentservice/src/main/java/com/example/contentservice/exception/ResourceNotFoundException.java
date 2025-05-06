package com.example.contentservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public class DuplicateProgressException extends RuntimeException {
        public DuplicateProgressException(String message) {
            super(message);
        }
    }

}
