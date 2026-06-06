package com.hotelos.receptionservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Markaziy xabar brokeri (Message Broker) konfiguratsiyasi.
 * Butun HotelOS tizimi yagona TOPIC exchange ({@code hotel.exchange}) atrofida quriladi.
 * Har bir servis o'ziga kerakli routing key'larni o'z navbatlariga bog'lab oladi (Pub/Sub).
 *
 * Reception servisi:
 *  - NASHR ETADI:  room.vacated (check-out), dashboard.update
 *  - OBUNA BO'LADI: room.cleaning.started / room.cleaning.completed (xona holatini sinxronlash),
 *                   order.charge (room-service to'lovlarini hisobga qo'shish)
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "hotel.exchange";

    // --- Routing keys (Event Registry) ---
    public static final String RK_ROOM_VACATED = "room.vacated";
    public static final String RK_CLEANING_STARTED = "room.cleaning.started";
    public static final String RK_CLEANING_COMPLETED = "room.cleaning.completed";
    public static final String RK_ORDER_CHARGE = "order.charge";
    public static final String RK_DASHBOARD = "dashboard.update";

    // --- Bu servis iste'mol qiladigan navbatlar ---
    public static final String CLEANING_SYNC_QUEUE = "reception.roomsync.queue";
    public static final String BILLING_QUEUE = "reception.billing.queue";

    @Bean
    public TopicExchange hotelExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue cleaningSyncQueue() {
        return new Queue(CLEANING_SYNC_QUEUE, true);
    }

    @Bean
    public Queue billingQueue() {
        return new Queue(BILLING_QUEUE, true);
    }

    @Bean
    public Binding cleaningStartedBinding(Queue cleaningSyncQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(cleaningSyncQueue).to(hotelExchange).with(RK_CLEANING_STARTED);
    }

    @Bean
    public Binding cleaningCompletedBinding(Queue cleaningSyncQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(cleaningSyncQueue).to(hotelExchange).with(RK_CLEANING_COMPLETED);
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(billingQueue).to(hotelExchange).with(RK_ORDER_CHARGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
