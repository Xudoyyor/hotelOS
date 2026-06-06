package com.hotelos.receptionservice.controllers;

import com.hotelos.receptionservice.entities.Booking;
import com.hotelos.receptionservice.entities.Room;
import com.hotelos.receptionservice.services.ReceptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
@Validated
public class ReceptionController {

    private final ReceptionService receptionService;

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(receptionService.getAllRooms(pageable));
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room) {
        return ResponseEntity.ok(receptionService.createRoom(room));
    }

    /**
     * Check-in. Kiritilgan ma'lumotlar Bean Validation orqali tekshiriladi (TS-08):
     * durationDays kamida 1, guestId/roomTypeId/preferredFloor null bo'lmasligi shart.
     */
    @PostMapping("/check-in")
    public ResponseEntity<Booking> checkIn(
            @RequestParam @NotNull UUID guestId,
            @RequestParam @NotNull Long roomTypeId,
            @RequestParam @NotNull Integer preferredFloor,
            @RequestParam @Min(value = 1, message = "durationDays kamida 1 bo'lishi kerak.") int durationDays) {

        Booking booking = receptionService.checkIn(guestId, roomTypeId, preferredFloor, durationDays);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @PostMapping("/check-out/{roomId}")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Long roomId) {
        double totalBill = receptionService.checkOut(roomId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Check-out muvaffaqiyatli bajarildi.");
        response.put("roomId", roomId);
        response.put("totalAmountDue", totalBill);
        return ResponseEntity.ok(response);
    }
}
