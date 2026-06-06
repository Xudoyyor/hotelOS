package com.hotelos.receptionservice.util;

import com.hotelos.receptionservice.entities.BillingItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * PROTSEDURAL PARADIGMA misoli.
 * Holatni saqlamaydigan (stateless), faqat kiritilgan qiymatlar ustida chiziqli
 * matematik amallarni bajaradigan utilit sinf. Hisob-kitob mantiqi obyekt holatidan
 * mustaqil bo'lgani uchun testlash oson va qayta ishlatish mumkin.
 */
public final class BillingCalculator {

    private BillingCalculator() {
        // Utilit sinf - instansiya yaratish taqiqlanadi.
    }

    /**
     * Haqiqiy yashagan kechalar sonini hisoblaydi. Erta check-out bo'lsa ham
     * kamida 1 kecha hisoblanadi (TS-02).
     */
    public static long calculateNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        return Math.max(1, nights);
    }

    /** Xona yashash to'lovi = bir kechalik narx * kechalar soni. */
    public static BigDecimal calculateRoomCharge(BigDecimal basePrice, long nights) {
        return basePrice.multiply(BigDecimal.valueOf(nights));
    }

    /** Barcha qo'shimcha xizmat (room-service) to'lovlari yig'indisi. */
    public static BigDecimal sumServiceCharges(List<BillingItem> items) {
        return items.stream()
                .map(BillingItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Yakuniy hisob = (xona to'lovi + xizmatlar) - chegirma.
     * discountRate 0.0 dan 1.0 gacha (masalan 0.10 = 10% chegirma).
     */
    public static BigDecimal calculateTotal(BigDecimal roomCharge, BigDecimal serviceTotal, BigDecimal discountRate) {
        BigDecimal subtotal = roomCharge.add(serviceTotal);
        BigDecimal discount = subtotal.multiply(discountRate == null ? BigDecimal.ZERO : discountRate);
        return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}
