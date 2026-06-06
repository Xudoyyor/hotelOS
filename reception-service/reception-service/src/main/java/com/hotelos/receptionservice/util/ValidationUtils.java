package com.hotelos.receptionservice.util;

/**
 * PROTSEDURAL PARADIGMA misoli - kiritilgan ma'lumotlarni tekshiruvchi (Input Validation)
 * statik yordamchi metodlar to'plami. Holatni saqlamaydi.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /** Qiymat musbat ekanligini tekshiradi. */
    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " musbat (0 dan katta) bo'lishi kerak. Kiritilgan: " + value);
        }
    }

    /** Qiymat null emasligini tekshiradi. */
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " bo'sh (null) bo'lishi mumkin emas.");
        }
    }

    /** Xona raqami formatini tekshiradi (aniq 3 ta raqam). */
    public static boolean isValidRoomNumber(String roomNumber) {
        return roomNumber != null && roomNumber.matches("\\d{3}");
    }
}
