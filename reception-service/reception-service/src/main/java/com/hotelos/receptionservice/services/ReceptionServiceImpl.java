package com.hotelos.receptionservice.services;

import com.hotelos.receptionservice.DTO.RoomVacatedEvent;
import com.hotelos.receptionservice.component.DashboardNotifier;
import com.hotelos.receptionservice.config.RabbitMQConfig;
import com.hotelos.receptionservice.entities.*;
import com.hotelos.receptionservice.enums.*;
import com.hotelos.receptionservice.exceptions.ResourceNotFoundException;
import com.hotelos.receptionservice.exceptions.RoomUnavailableException;
import com.hotelos.receptionservice.repositories.*;
import com.hotelos.receptionservice.util.BillingCalculator;
import com.hotelos.receptionservice.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReceptionServiceImpl implements ReceptionService {

    private static final Logger log = LoggerFactory.getLogger(ReceptionServiceImpl.class);

    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final BillingItemRepository billingItemRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final DashboardNotifier dashboardNotifier;

    // ============================ READ OPERATIONS ============================

    @Override
    @SuppressWarnings("unchecked")
    public List<Room> getAllRooms(Pageable pageable) {
        String key = "rooms:page_" + pageable.getPageNumber() + "_size_" + pageable.getPageSize();
        List<Room> cachedRooms = (List<Room>) redisTemplate.opsForValue().get(key);
        if (cachedRooms != null) {
            log.info("FROM REDIS !!! Xonalar keshdan olindi. Kalit: {}", key);
            return cachedRooms;
        }
        log.info("FROM DB !!! Keshda topilmadi, PostgreSQL'dan o'qilmoqda. Sahifa: {}", pageable.getPageNumber());
        List<Room> rooms = roomRepository.findAll(pageable).getContent();
        redisTemplate.opsForValue().set(key, rooms, 30, TimeUnit.HOURS);
        return rooms;
    }

    @Override
    public Room createRoom(Room room) {
        room.setStatus(RoomStatus.TOZA);
        Room savedRoom = roomRepository.save(room);
        clearRoomCache();
        return savedRoom;
    }

    // ============================ CHECK-IN ============================

    /**
     * Mehmonni ro'yxatga olish. Xona tayinlash algoritmi (TS-01) va parallel
     * so'rovlardan himoya (TS-06) shu yerda jamlangan.
     * REPEATABLE_READ izolyatsiya + repozitoriydagi PESSIMISTIC_WRITE qulfi birgalikda
     * bitta xonaning ikki mehmonga berilishini imkonsiz qiladi.
     */
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(value = "roomsCache", allEntries = true)
    public Booking checkIn(UUID guestId, Long roomTypeId, Integer preferredFloor, int durationDays) {
        validateCheckInRequest(guestId, roomTypeId, durationDays);

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Mehmon topilmadi: " + guestId));
        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new ResourceNotFoundException("Bunday xona turi mavjud emas: " + roomTypeId);
        }

        Room selectedRoom = findBestAvailableRoom(roomTypeId, preferredFloor);
        assignRoom(selectedRoom);

        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .guest(guest)
                .room(selectedRoom)
                .checkInDate(now)
                .expectedCheckOutDate(now.plusDays(durationDays))
                .status(BookingStatus.ACTIVE)
                .build();
        booking = bookingRepository.save(booking);

        clearRoomCache();
        dashboardNotifier.room(selectedRoom.getRoomNumber(), RoomStatus.BAND.name(),
                guest.getFirstName() + " " + guest.getLastName(), "Mehmon check-in qilindi");
        log.info("Check-in muvaffaqiyatli: mehmon {} -> xona {} ({}-qavat)",
                guest.getId(), selectedRoom.getRoomNumber(), selectedRoom.getFloorNumber());
        return booking;
    }

    /** Protsedural validatsiya yordamchisi (Extract Method). */
    private void validateCheckInRequest(UUID guestId, Long roomTypeId, int durationDays) {
        ValidationUtils.requireNonNull(guestId, "guestId");
        ValidationUtils.requireNonNull(roomTypeId, "roomTypeId");
        ValidationUtils.requirePositive(durationDays, "durationDays");
    }

    /**
     * Xona tayinlash mezonlari (Extract Method):
     *  1+2+3+5 repozitoriy so'rovida (tur, TOZA, eng uzoq toza, yaqinlik),
     *  4 (qavat afzalligi) shu yerda ikkilamchi filtr sifatida qo'llanadi.
     */
    private Room findBestAvailableRoom(Long roomTypeId, Integer preferredFloor) {
        List<Room> eligible = roomRepository.findEligibleRoomsForAssignment(roomTypeId, RoomStatus.TOZA);
        if (eligible.isEmpty()) {
            throw new RoomUnavailableException("Hozircha bu turdagi bo'sh va toza xona mavjud emas.");
        }
        if (preferredFloor != null) {
            List<Room> roomsOnPreferredFloor = eligible.stream()
                    .filter(room -> preferredFloor.equals(room.getFloorNumber()))
                    .toList();
            if (!roomsOnPreferredFloor.isEmpty()) {
                return roomsOnPreferredFloor.get(0); // ro'yxat allaqachon eng uzoq toza + yaqinlik bo'yicha saralangan
            }
        }
        return eligible.get(0);
    }

    /** Xonani band qilish (Extract Method). @Version optimistik blokirovkani ta'minlaydi. */
    private void assignRoom(Room room) {
        room.setStatus(RoomStatus.BAND);
        roomRepository.save(room);
    }

    // ============================ CHECK-OUT ============================

    @Override
    @Transactional
    public double checkOut(Long roomId) {
        Booking booking = bookingRepository.findByRoomIdAndStatus(roomId, BookingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Ushbu xonada faol yashovchi mehmon topilmadi."));
        Room room = booking.getRoom();

        // Billing algoritmi (protsedural BillingCalculator):
        long nights = BillingCalculator.calculateNights(booking.getCheckInDate(), LocalDateTime.now());
        BigDecimal roomCharge = BillingCalculator.calculateRoomCharge(room.getRoomType().getBasePrice(), nights);
        List<BillingItem> serviceItems = billingItemRepository.findByBookingId(booking.getId());
        BigDecimal serviceTotal = BillingCalculator.sumServiceCharges(serviceItems);
        BigDecimal grandTotal = BillingCalculator.calculateTotal(roomCharge, serviceTotal, BigDecimal.ZERO);

        // Xona to'lovini audit uchun BillingItem sifatida qoldiramiz.
        billingItemRepository.save(BillingItem.builder()
                .booking(booking)
                .description("Xona yashash to'lovi (" + nights + " kecha)")
                .amount(roomCharge)
                .build());

        booking.setActualCheckOutDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.COMPLETED);
        room.setStatus(RoomStatus.IFLOS);
        roomRepository.save(room);
        bookingRepository.save(booking);
        clearRoomCache();

        // "Xona bo'shatildi" hodisasini brokerga nashr etamiz -> Housekeeping tozalash navbatiga qo'shadi.
        RoomVacatedEvent event = RoomVacatedEvent.builder()
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .message("Xona bo'shatildi. Tozalash talab etiladi.")
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_ROOM_VACATED, event);
        dashboardNotifier.room(room.getRoomNumber(), RoomStatus.IFLOS.name(), null,
                "Check-out bajarildi, tozalash navbatiga qo'shildi");

        log.info("Check-out: xona {} | {} kecha | xona to'lovi {} | xizmatlar {} | jami {}",
                room.getRoomNumber(), nights, roomCharge, serviceTotal, grandTotal);
        return grandTotal.doubleValue();
    }

    // ============================ HELPERS ============================

    /** Redis'dagi xona keshini tozalaydi (Extract Method - takrorlanishni yo'qotish). */
    private void clearRoomCache() {
        try {
            Set<String> keys = redisTemplate.keys("rooms:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Keshni tozalashda xatolik: ", e);
        }
    }
}
