package com.example.notificationservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.notificationservice.exceptions.NotificationExceptions;

@RestControllerAdvice
public class NotificationExceptionsHandler {
    @ExceptionHandler(NotificationExceptions.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleNotificationException(NotificationExceptions e) {
        return e.getMessage();
    }
}
