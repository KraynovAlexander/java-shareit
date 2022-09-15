package ru.practicum.shareit.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingInDto {
    private Long id;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime start;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private String status;
}
