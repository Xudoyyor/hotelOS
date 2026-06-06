package com.hotelos.receptionservice.DTO;
import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomVacatedEvent implements Serializable {
    private Long roomId;
    private String roomNumber;
    private String message;
}
