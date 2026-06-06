package com.hotelos.receptionservice.DTO;

import lombok.*;

import java.io.Serializable;

/**
 * Housekeeping servisidan keladigan tozalash holati hodisasi.
 * status qiymatlari: TOZALANMOQDA, TOZA.
 * Reception servisi xonaning haqiqiy holatini shu hodisaga qarab yangilaydi (TS-03).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleaningStatusEvent implements Serializable {
    private Long roomId;
    private String roomNumber;
    private String status;
}
