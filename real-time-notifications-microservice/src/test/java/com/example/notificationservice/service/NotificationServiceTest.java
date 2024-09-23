package com.example.notificationservice.service;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import static org.mockito.Mockito.*;


public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserNotifications() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now());
        when(notificationRepository.findByUserReferenceId(anyString())).thenReturn(Flux.just(notification));

        Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> result = notificationService.getUserNotifications("user1");

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    CustomApiResponse<List<Notification>> customApiResponse = response.getBody();
                    assert customApiResponse != null;
                    return "success".equals(customApiResponse.getStatus()) && customApiResponse.getData().size() == 1;
                })
                .verifyComplete();
    }

    @Test
    void testCreateNotification() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now());
        when(notificationRepository.save(notification)).thenReturn(Mono.just(notification));

        Mono<Notification> result = notificationService.createNotification("user1", notification);

        StepVerifier.create(result)
                .expectNextMatches(savedNotification -> "Notification message".equals(savedNotification.getMessage()))
                .verifyComplete();
    }

    @Test
    void testDeleteNotificationFromUser() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now());
        when(notificationRepository.findByUserReferenceId(anyString())).thenReturn(Flux.just(notification));
        when(notificationRepository.deleteById(anyString())).thenReturn(Mono.empty());

        Mono<String> result = notificationService.deleteNotificationFromUser("user1", "Notification message");

        StepVerifier.create(result)
                .expectNextMatches(successMessage -> successMessage.contains("eliminada con éxito"))
                .verifyComplete();
    }


}