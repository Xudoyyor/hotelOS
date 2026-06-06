package com.hotelos.receptionservice.component;

import com.hotelos.receptionservice.entities.Guest;
import com.hotelos.receptionservice.entities.Room;
import com.hotelos.receptionservice.entities.RoomType;
import com.hotelos.receptionservice.enums.RoomStatus;
import com.hotelos.receptionservice.repositories.GuestRepository;
import com.hotelos.receptionservice.repositories.RoomRepository;
import com.hotelos.receptionservice.repositories.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sinov ma'lumotlari: GrandStay mehmonxonasining soddalashtirilgan modeli.
 * 3 ta xona turi va 3 qavatga taqsimlangan 12 ta xona. lastCleanedAt qiymatlari
 * turlicha berilgan, shunda "eng uzoq toza" (longest-clean-first) algoritmini
 * test stsenariylarida (TS-01) namoyish qilish mumkin.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    @Override
    public void run(String... args) {
        if (roomTypeRepository.count() > 0) {
            return;
        }

        RoomType single = roomTypeRepository.save(
                RoomType.builder().name("SINGLE").basePrice(BigDecimal.valueOf(100.0)).build());
        RoomType doubleRoom = roomTypeRepository.save(
                RoomType.builder().name("DOUBLE").basePrice(BigDecimal.valueOf(160.0)).build());
        RoomType lux = roomTypeRepository.save(
                RoomType.builder().name("LUX").basePrice(BigDecimal.valueOf(300.0)).build());

        LocalDateTime now = LocalDateTime.now();

        // 1-qavat
        saveRoom("101", 1, single, RoomStatus.TOZA, 1, now.minusDays(2));
        saveRoom("102", 1, doubleRoom, RoomStatus.TOZA, 2, now.minusDays(5));   // eng uzoq toza DOUBLE (1-qavat)
        saveRoom("107", 1, single, RoomStatus.IFLOS, 3, now.minusHours(3));
        saveRoom("108", 1, single, RoomStatus.TOZA, 4, now.minusHours(10));
        saveRoom("115", 1, doubleRoom, RoomStatus.TOZA, 5, now.minusDays(1));

        // 2-qavat
        saveRoom("201", 2, doubleRoom, RoomStatus.TOZA, 1, now.minusDays(3));
        saveRoom("204", 2, doubleRoom, RoomStatus.TOZA, 2, now.minusHours(6));
        saveRoom("205", 2, lux, RoomStatus.TOZA, 3, now.minusDays(4));

        // 3-qavat
        saveRoom("301", 3, doubleRoom, RoomStatus.TOZA, 1, now.minusDays(7));   // eng uzoq toza DOUBLE (3-qavat) -> TS-01
        saveRoom("302", 3, doubleRoom, RoomStatus.TOZA, 2, now.minusHours(2));
        saveRoom("305", 3, lux, RoomStatus.TOZA, 3, now.minusDays(1));
        saveRoom("310", 3, single, RoomStatus.TOZA, 4, now.minusDays(6));

        Guest testGuest = guestRepository.save(Guest.builder()
                .firstName("Eshmat")
                .lastName("Toshmatov")
                .documentNumber("AA1234567")
                .phoneNumber("+998901234567")
                .build());

        log.info("Sinov ma'lumotlari yuklandi. 12 ta xona, 3 tur. SINOV MEHMON ID-SI: {}", testGuest.getId());
    }

    private void saveRoom(String number, int floor, RoomType type, RoomStatus status,
                          int liftIndex, LocalDateTime lastCleanedAt) {
        roomRepository.save(Room.builder()
                .roomNumber(number)
                .floorNumber(floor)
                .roomType(type)
                .status(status)
                .liftIndex(liftIndex)
                .lastCleanedAt(lastCleanedAt)
                .build());
    }
}
