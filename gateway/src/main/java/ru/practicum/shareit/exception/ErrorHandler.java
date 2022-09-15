package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String,String>> handleNoAccessRightsException(final InvalidRequestException e) {
        Map<String,String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(final ConstraintViolationException e) {
        return e.getMessage().contains("Unknown state: UNSUPPORTED_STATUS") ?
                new ResponseEntity<>(Collections.singletonMap("error", "Unknown state: UNSUPPORTED_STATUS"),
                        HttpStatus.INTERNAL_SERVER_ERROR) :
                new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ErrorResponse(Objects.requireNonNull(e.getFieldError()).getDefaultMessage());
    }
}
