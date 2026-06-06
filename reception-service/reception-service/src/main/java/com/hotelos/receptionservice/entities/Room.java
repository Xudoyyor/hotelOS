package com.hotelos.receptionservice.entities;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hotelos.receptionservice.enums.RoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Xona domen modeli (OOP - inkapsulyatsiya).
 * Xona raqami, qavati, turi, joriy holati va eng oxirgi tozalangan vaqtini saqlaydi.
 * {@code version} maydoni optimistik blokirovka (Optimistic Locking) uchun ishlatiladi:
 * bir vaqtning o'zida ikki check-in bitta xonani band qilishga urinsa, ikkinchisi
 * {@code OptimisticLockException} oladi (TS-06 - race condition himoyasi).
 */
@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "\\d{3}", message = "Xona raqami aniq 3 ta raqamdan iborat bo'lishi kerak (masalan: 204).")
    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @NotNull(message = "Qavat raqami ko'rsatilishi shart.")
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    @Column(name = "lift_index", nullable = false)
    private Integer liftIndex;

    /** Xona eng oxirgi marta tozalangan vaqt. Xona tayinlash algoritmida "eng uzoq toza" mezoni uchun ishlatiladi. */
    @Column(name = "last_cleaned_at")
    private LocalDateTime lastCleanedAt;

    /** Optimistik blokirovka versiyasi (concurrency control). */
    @Version
    @Column(name = "version")
    private Long version;
}
