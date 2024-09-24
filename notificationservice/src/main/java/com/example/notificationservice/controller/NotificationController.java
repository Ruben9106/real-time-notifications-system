package com.example.notificationservice.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.codec.ServerSentEvent;

import com.example.notificationservice.dto.NotificationDto;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.utils.DateUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public Mono<Notification> createNotification(@RequestBody NotificationDto notificationDto) {
        Notification notification = new Notification(UUID.randomUUID().toString(), notificationDto.getUserId(), notificationDto.getMessage(), DateUtils.now());
        return notificationService.createNotification(notification);
    }

    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Notification>> streamNotifications(@PathVariable String userId) {
        return notificationService.getNotificationsForUser(userId)
        .map(notification -> ServerSentEvent.builder(notification).build());
    }

    @GetMapping("/all")
    public Flux<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }
    
    
}
