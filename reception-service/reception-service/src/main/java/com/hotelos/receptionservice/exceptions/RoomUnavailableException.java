package com.hotelos.receptionservice.exceptions;

/** Mos keladigan bo'sh/toza xona qolmaganda tashlanadi -> HTTP 409 (TS-07). */
public class RoomUnavailableException extends RuntimeException {
    public RoomUnavailableException(String message) {
        super(message);
    }
}
