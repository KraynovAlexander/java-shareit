package ru.practicum.shareit.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@Builder
@AllArgsConstructor
public class BookingShortDto {
    private Long id;
    @ManyToOne
    @JoinColumn(name = "booker_id")
    private Long bookerId;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime start;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime end;
}
