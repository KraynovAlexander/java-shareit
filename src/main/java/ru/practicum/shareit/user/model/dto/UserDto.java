package ru.practicum.shareit.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    @NotNull(message = "Адрес электронной почты не может быть нулевым")
    @NotBlank(message = "Электронное письмо не может быть пустым")
    @Email(message = "Адрес электронной почты неверен")
    private String email;
}