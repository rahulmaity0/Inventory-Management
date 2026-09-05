package com.rahul.inventorybilling.controller;

import com.rahul.inventorybilling.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Catches exceptions thrown anywhere in the app and turns them into a tidy
 * JSON error instead of a stack trace.
 *
 * @RestControllerAdvice means "apply to every controller".
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Thrown by the services when a business rule is broken. */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                exception.getMessage());
    }

    /** Thrown by Spring when a @Valid request body fails its annotations. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        // Several fields may be wrong at once. Report the first one.
        String message = "Validation failed";
        if (!fieldErrors.isEmpty()) {
            FieldError firstError = fieldErrors.get(0);
            message = firstError.getField() + " " + firstError.getDefaultMessage();
        }

        return new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message);
    }
}
