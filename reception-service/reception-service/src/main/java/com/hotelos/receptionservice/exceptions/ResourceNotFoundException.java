package com.hotelos.receptionservice.exceptions;

/** So'ralgan resurs (mehmon, xona turi, bandlov) topilmaganda tashlanadi -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
