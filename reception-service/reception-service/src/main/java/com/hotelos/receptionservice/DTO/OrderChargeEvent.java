package com.hotelos.receptionservice.DTO;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Room-service'dan keladigan to'lov hodisasi.
 * Reception servisi buni qabul qilib, mehmonning faol hisobiga (booking) qo'shadi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderChargeEvent implements Serializable {
    private String roomNumber;
    private String description;
    private BigDecimal amount;
}
