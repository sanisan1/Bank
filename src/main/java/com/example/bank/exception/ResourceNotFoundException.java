package com.example.bank.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, String fieldname, Object value) {
        super("Resource " + resourceName + " not found for " + fieldname + " " + value);

    }
}
