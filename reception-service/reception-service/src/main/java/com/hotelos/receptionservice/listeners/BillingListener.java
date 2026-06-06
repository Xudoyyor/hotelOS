package com.hotelos.receptionservice.listeners;

import com.hotelos.receptionservice.DTO.OrderChargeEvent;
import com.hotelos.receptionservice.config.RabbitMQConfig;
import com.hotelos.receptionservice.entities.BillingItem;
import com.hotelos.receptionservice.enums.BookingStatus;
import com.hotelos.receptionservice.repositories.BillingItemRepository;
import com.hotelos.receptionservice.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * HODISAGA ASOSLANGAN paradigma - Subscriber.
 * Room-service'dan kelgan har bir taom/ichimlik to'lovini mehmonning faol hisobiga
 * (booking) BillingItem sifatida qo'shadi. Check-out vaqtida bu to'lovlar umumiy
 * hisobga jamlanadi (TS-04 -> TS-02).
 */
@Component
@RequiredArgsConstructor
public class BillingListener {

    private static final Logger log = LoggerFactory.getLogger(BillingListener.class);
    private final BookingRepository bookingRepository;
    private final BillingItemRepository billingItemRepository;

    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    @Transactional
    public void onOrderCharge(OrderChargeEvent event) {
        bookingRepository.findByRoomRoomNumberAndStatus(event.getRoomNumber(), BookingStatus.ACTIVE)
                .ifPresentOrElse(booking -> {
                    billingItemRepository.save(BillingItem.builder()
                            .booking(booking)
                            .description("Xona xizmati: " + event.getDescription())
                            .amount(event.getAmount())
                            .build());
                    log.info("Room-service to'lovi hisobga qo'shildi: xona {} +{}",
                            event.getRoomNumber(), event.getAmount());
                }, () -> log.warn("To'lov keldi, lekin xonada faol mehmon yo'q: {}", event.getRoomNumber()));
    }
}
