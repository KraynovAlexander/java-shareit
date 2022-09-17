package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ru.practicum.shareit.booking.model.dto.BookingShortDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDtoWithBookings {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private Boolean available;
    private Long itemRequestId;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
}
