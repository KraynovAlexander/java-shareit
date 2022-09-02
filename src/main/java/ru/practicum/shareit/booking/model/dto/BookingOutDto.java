package ru.practicum.shareit.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingOutDto {
    private Long id;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime start;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime end;
    private Item item;
    private User booker;
    private String status;
}
