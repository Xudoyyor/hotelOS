package com.hotelos.receptionservice.repositories;

import com.hotelos.receptionservice.entities.Room;
import com.hotelos.receptionservice.enums.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {

    List<Room> findByRoomTypeIdAndStatus(Long roomTypeId, RoomStatus status);

    Page<Room> findByStatus(String status, Pageable pageable);

    /**
     * Xona tayinlash algoritmining yadrosi (TS-01).
     * Mezonlar tartibi:
     *  1) Xona turi mos (roomType.id = :typeId)
     *  2) Holati faqat TOZA (:status)
     *  3) Eng uzoq toza turgan xona birinchi (lastCleanedAt ASC, hech tozalanmaganlar NULLS FIRST)
     *  5) Yaqinlik / proksimallik liftga (liftIndex ASC) - yakuniy tiebreaker
     * (Qavat afzalligi - 4-mezon - servis qatlamida ikkilamchi filtr sifatida qo'llanadi.)
     *
     * PESSIMISTIC_WRITE qulfi parallel check-in so'rovlarini serializatsiya qiladi,
     * shu bilan bitta xona ikki mehmonga berilishining oldi olinadi (TS-06).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.roomType.id = :typeId AND r.status = :status " +
           "ORDER BY r.lastCleanedAt ASC NULLS FIRST, r.liftIndex ASC")
    List<Room> findEligibleRoomsForAssignment(@Param("typeId") Long typeId,
                                              @Param("status") RoomStatus status);
}
