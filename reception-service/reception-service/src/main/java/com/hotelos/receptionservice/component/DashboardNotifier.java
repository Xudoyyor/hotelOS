package com.hotelos.receptionservice.component;

import com.hotelos.receptionservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Operatsiyalar paneli (Live Dashboard) uchun strukturalangan yangilanish hodisalarini
 * markaziy brokerga nashr etadi (HODISAGA ASOSLANGAN paradigma - Publisher).
 * Maxfiylik: tarmoqqa faqat operatsion ma'lumot (ism, xona holati) yuboriladi,
 * pasport raqami yoki to'liq karta ma'lumotlari HECH QACHON yuborilmaydi.
 */
@Component
@RequiredArgsConstructor
public class DashboardNotifier {

    private final RabbitTemplate rabbitTemplate;

    public void room(String roomNumber, String status, String guestName, String detail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ROOM");
        payload.put("roomNumber", roomNumber);
        payload.put("status", status);
        if (guestName != null) {
            payload.put("guestName", guestName);
        }
        payload.put("detail", detail);
        payload.put("timestamp", LocalDateTime.now().toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_DASHBOARD, payload);
    }
}
