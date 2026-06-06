package com.hotelos.receptionservice.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Markazlashtirilgan xatoliklarni boshqaruvchi (Global Exception Handler).
 * Foydalanuvchiga hech qachon xom stek izi (raw stack trace) ko'rsatmaydi (xavfsizlik talabi);
 * faqat tushunarli va xavfsiz JSON xabar qaytaradi. Xatolik ichki log'ga yoziladi.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object> build(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(RoomUnavailableException.class)
    public ResponseEntity<Object> handleRoomUnavailable(RoomUnavailableException ex) {
        return build(HttpStatus.CONFLICT, "Room Unavailable", ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class,
            MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleValidation(Exception ex) {
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException manve && manve.getBindingResult().getFieldError() != null) {
            message = manve.getBindingResult().getFieldError().getDefaultMessage();
        }
        return build(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleConcurrency(OptimisticLockingFailureException ex) {
        log.warn("Bir vaqtning o'zida xonani band qilishga urinish aniqlandi: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Concurrent Modification",
                "Xona ayni damda boshqa so'rov tomonidan band qilindi. Iltimos, qайta urinib ko'ring.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        log.error("Kutilmagan ichki xatolik: ", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Tizimda texnik xatolik yuz berdi. Iltimos, keyinroq urinib ko'ring.");
    }
}
