package ru.practicum.shareit.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import javax.persistence.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", length = 128, nullable = false)
    private String name;

    @NotNull(message = "Адрес электронной почты не может быть нулевым")
    @NotBlank(message = "Электронное письмо не может быть пустым")
    @Email(message = "Адрес электронной почты неверен")
    @Column(length = 128, unique = true)
    private String email;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }
}