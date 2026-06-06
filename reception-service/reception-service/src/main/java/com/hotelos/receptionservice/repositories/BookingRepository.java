package com.hotelos.receptionservice.repositories;

import com.hotelos.receptionservice.entities.Booking;
import com.hotelos.receptionservice.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByRoomIdAndStatus(Long roomId, BookingStatus status);

    /** Room-service to'lovini faol bandlovga (booking) bog'lash uchun xona raqami bo'yicha qidiradi. */
    Optional<Booking> findByRoomRoomNumberAndStatus(String roomNumber, BookingStatus status);
}
