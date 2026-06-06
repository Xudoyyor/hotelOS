package com.hotelos.receptionservice.services;
import com.hotelos.receptionservice.entities.Booking;
import com.hotelos.receptionservice.entities.Room;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReceptionService {
    Booking checkIn(UUID guestId, Long roomTypeId, Integer preferredFloor, int durationDays);
    double checkOut(Long roomId);

    List<Room> getAllRooms(Pageable pageable);

    Room createRoom(Room room);
}
