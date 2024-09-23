package com.example.notificationservice.controller;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.HttpResponse.ResponseUtil;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api-clients/v1.0/notifications")
@Tag(name = "Notifications", description = "Operations related to Notifications in the notification system")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Operation(summary = "Get user notifications", description = "Retrieve a list of notifications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
                    content = @Content(schema = @Schema(implementation = Notification.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> getUserNotifications(@PathVariable String userId) {
        return notificationService.getUserNotifications(userId);
    }

    @Operation(summary = "Stream user notifications", description = "Stream notifications for a specific user in real time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Streaming notifications",
                    content = @Content(schema = @Schema(implementation = Notification.class)))
    })
    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> streamNotifications(@PathVariable String userId) {
        return notificationService.getNotificationsStream(userId);
    }

    @Operation(summary = "Create a notification for a user", description = "Create a new notification for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification successfully created",
                    content = @Content(schema = @Schema(implementation = Notification.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/users/{userId}")
    public Mono<ResponseEntity<CustomApiResponse<Notification>>> createNotification(@PathVariable String userId, @RequestBody Notification notification) {
        return notificationService.createNotification(userId, notification)
                .flatMap(createdNotification -> ResponseUtil.createSuccessResponse("Notificación creada con éxito", createdNotification))
                .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @Operation(summary = "Delete a user's notification", description = "Delete a specific notification for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/users/{userId}/notifications")
    public Mono<ResponseEntity<CustomApiResponse<Void>>> deleteNotification(@PathVariable String userId, @RequestBody String notificationMessage) {
        return notificationService.deleteNotificationFromUser(userId, notificationMessage)
                .flatMap(successMessage -> ResponseUtil.createSuccessResponse(successMessage, (Void) null))
                .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND));
    }

}