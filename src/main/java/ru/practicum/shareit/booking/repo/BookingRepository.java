package ru.practicum.shareit.booking.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

import java.util.Optional;

@EnableJpaRepositories
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Slice<Booking> getAllByBookerId(long bookerId, Pageable pageable);

    Slice<Booking> getAllByBookerIdAndStartAfter(long bookerId, LocalDateTime localDateTime, Pageable pageable);

    Slice<Booking> getAllByBookerIdAndEndBefore(long bookerId, LocalDateTime localDateTime, Pageable pageable);

    Slice<Booking> getAllByBookerIdAndStatus(long bookerId, Status status, Pageable pageable);

    Optional<Booking> getTopByItem_IdAndBooker_IdOrderByEndAsc(long itemId, long bookerId);

    Optional<Booking> getTopByItem_IdAndEndBeforeOrderByStartDesc(long itemId, LocalDateTime localDateTime);

    Optional<Booking> getTopByItem_IdAndStartAfterOrderByStartDesc(long itemId, LocalDateTime localDateTime);

    @Query(value = "select b from Booking b where b.booker.id = :bookerId and :dateTime between b.start and b.end")
    Slice<Booking> getAllCurrentByBookerId(@Param("bookerId") long bookerId, @Param("dateTime") LocalDateTime dateTime,
                                           Pageable pageable);

    @Query(value = "select b from Booking b left join Item i on b.item.id = i.id where i.owner.id = :ownerId")
    Slice<Booking> getAllByOwnerId(@Param("ownerId") long ownerId, Pageable pageable);

    @Query(value = "select b from Booking b left join Item i on b.item.id = i.id where i.owner.id = :ownerId " +
            "and (:dateTime between b.start and b.end)")
    Slice<Booking> getAllCurrentByOwnerId(@Param("ownerId") long ownerId, @Param("dateTime") LocalDateTime dateTime,
                                          Pageable pageable);

    @Query(value = "select b from Booking b left join Item i on b.item.id = i.id where i.owner.id = :ownerId " +
            "and b.end < :dateTime")
    Slice<Booking> getAllPastByOwnerId(@Param("ownerId") long ownerId, @Param("dateTime") LocalDateTime dateTime,
                                       Pageable pageable);

    @Query(value = "select b from Booking b left join Item i on b.item.id = i.id where i.owner.id = :ownerId " +
            "and b.start > :dateTime")
    Slice<Booking> getAllFutureByOwnerId(@Param("ownerId") long ownerId, @Param("dateTime") LocalDateTime dateTime,
                                         Pageable pageable);

    @Query(value = "select b from Booking b left join Item i on b.item.id = i.id where i.owner.id = :ownerId " +
            "and b.status = :status")
    Slice<Booking> getAllByOwnerIdAndStatus(@Param("ownerId") long ownerId, @Param("status") Status status,
                                            Pageable pageable);
}
