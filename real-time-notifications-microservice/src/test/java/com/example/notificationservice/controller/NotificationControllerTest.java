package com.example.notificationservice.controller;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserNotifications() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now());
        when(notificationService.getUserNotifications(anyString())).thenReturn(
                Mono.just(ResponseEntity.ok(new CustomApiResponse<>("success", "Notificaciones encontradas", List.of(notification)))));

        StepVerifier.create(notificationController.getUserNotifications("user1"))
                .expectNextMatches(response -> {
                    CustomApiResponse<List<Notification>> customApiResponse = response.getBody();
                    assert customApiResponse != null;
                    return customApiResponse.getData().size() == 1 && "Notification message".equals(customApiResponse.getData().get(0).getMessage());
                })
                .verifyComplete();
    }

    @Test
    void testCreateNotification() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now());

        // Cambiar para que todos los parámetros usen matchers
        when(notificationService.createNotification(anyString(), any(Notification.class))).thenReturn(Mono.just(notification));

        StepVerifier.create(notificationController.createNotification("user1", notification))
                .expectNextMatches(response -> {
                    CustomApiResponse<Notification> customApiResponse = response.getBody();
                    assert customApiResponse != null;
                    return "Notification message".equals(customApiResponse.getData().getMessage());
                })
                .verifyComplete();
    }

    @Test
    void testDeleteNotification() {
        when(notificationService.deleteNotificationFromUser(anyString(), anyString())).thenReturn(
                Mono.just("La notificación ha sido eliminada con éxito"));

        StepVerifier.create(notificationController.deleteNotification("user1", "Notification message"))
                .expectNextMatches(response -> Objects.requireNonNull(response.getBody()).getMessage().contains("eliminada con éxito"))
                .verifyComplete();
    }
}