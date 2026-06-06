package com.hotelos.receptionservice.listeners;

import com.hotelos.receptionservice.DTO.CleaningStatusEvent;
import com.hotelos.receptionservice.config.RabbitMQConfig;
import com.hotelos.receptionservice.entities.Room;
import com.hotelos.receptionservice.enums.RoomStatus;
import com.hotelos.receptionservice.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * HODISAGA ASOSLANGAN paradigma - Subscriber.
 * Housekeeping servisi nashr etgan tozalash holati hodisalarini tinglaydi va
 * xonaning haqiqiy holatini sinxronlaydi (TS-03):
 *  - TOZALANMOQDA -> xona TOZALANMOQDA
 *  - TOZA         -> xona TOZA + lastCleanedAt yangilanadi (rotatsiya uchun muhim)
 */
@Component
@RequiredArgsConstructor
public class RoomSyncListener {

    private static final Logger log = LoggerFactory.getLogger(RoomSyncListener.class);
    private final RoomRepository roomRepository;

    @RabbitListener(queues = RabbitMQConfig.CLEANING_SYNC_QUEUE)
    @Transactional
    public void onCleaningStatus(CleaningStatusEvent event) {
        Room room = roomRepository.findById(event.getRoomId()).orElse(null);
        if (room == null) {
            log.warn("Tozalash hodisasi keldi, lekin xona topilmadi. roomId={}", event.getRoomId());
            return;
        }
        switch (event.getStatus()) {
            case "TOZALANMOQDA" -> room.setStatus(RoomStatus.TOZALANMOQDA);
            case "TOZA" -> {
                room.setStatus(RoomStatus.TOZA);
                room.setLastCleanedAt(LocalDateTime.now());
            }
            default -> log.warn("Noma'lum tozalash holati: {}", event.getStatus());
        }
        roomRepository.save(room);
        log.info("Xona {} holati sinxronlandi -> {}", room.getRoomNumber(), room.getStatus());
    }
}
