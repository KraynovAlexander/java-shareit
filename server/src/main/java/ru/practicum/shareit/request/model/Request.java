package ru.practicum.shareit.request.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_request")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "request_description", length = 128, nullable = false)
    private String description;

    @Column(name = "creation_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd, hh:mm:ss")
    private LocalDateTime creationTime;
}
