package com.example.notificationservice.controller;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;
import com.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class NotificationControllerTest {

    @MockBean
    private NotificationService notificationService;


    @InjectMocks
    private NotificationController notificationController;

    @Autowired
    private WebTestClient webTestClient;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void testGetUserNotifications() {
//        // Crear una notificación de prueba
//        Notification notification = new Notification("1", "user1", "Notification message", Instant.now(), false);
//
//        // Simular el comportamiento del servicio
//        when(notificationService.getUserNotifications(anyString())).thenReturn(
//                Mono.just(ResponseEntity.ok(new CustomApiResponse<>("success", "Notificaciones encontradas", List.of(notification),hashCode()))));
//
//        // Verificar que el controlador devuelve las notificaciones correctamente
//        StepVerifier.create(notificationController.getUserNotifications("user1"))
//                .expectNextMatches(response -> {
//                    CustomApiResponse<List<Notification>> customApiResponse = response.getBody();
//                    assert customApiResponse != null;
//                    assert customApiResponse.getData().size() == 1;
//                    return "Notification message".equals(customApiResponse.getData().get(0).getMessage());
//                })
//                .verifyComplete();
//    }

    @Test
    void testCreateNotification() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now(), false);

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
    void shouldMarkNotificationAsReadAndDelete() {
        // Simular la respuesta exitosa del servicio
        String notificationId = "171c72";
        String successMessage = "La notificación con ID: '" + notificationId + "' fue marcada como leída y eliminada con éxito.";

        when(notificationService.markNotificationAsReadAndDelete(notificationId))
                .thenReturn(Mono.just(successMessage));

        // Ejecutar la solicitud PUT y verificar la respuesta
        webTestClient.put()
                .uri("/api-clients/v1.0/notifications/{notificationId}/read-and-delete", notificationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomApiResponse.class)
                .value(response -> {
                    assertEquals("success", response.getStatus());
                    assertEquals(successMessage, response.getMessage());
                });

        // Verificar que el servicio fue llamado
        verify(notificationService).markNotificationAsReadAndDelete(notificationId);
    }

    @Test
    void shouldDeleteWhenNotificationAlreadyRead() {
        // Simular la respuesta exitosa del servicio cuando la notificación ya estaba leída
        String notificationId = "171c72";
        String successMessage = "La notificación con ID: '" + notificationId + "' ya estaba marcada como leída y ha sido eliminada.";

        when(notificationService.markNotificationAsReadAndDelete(notificationId))
                .thenReturn(Mono.just(successMessage));

        // Ejecutar la solicitud PUT y verificar la respuesta
        webTestClient.put()
                .uri("/api-clients/v1.0/notifications/{notificationId}/read-and-delete", notificationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomApiResponse.class)
                .value(response -> {
                    assertEquals("success", response.getStatus());
                    assertEquals(successMessage, response.getMessage());
                });

        // Verificar que el servicio fue llamado
        verify(notificationService).markNotificationAsReadAndDelete(notificationId);
    }

    @Test
    void shouldReturnNotFoundWhenNotificationDoesNotExist() {
        // Simular la respuesta de error del servicio cuando la notificación no existe
        String notificationId = "nonExistingId";
        String errorMessage = "No se encontró la notificación con el ID proporcionado.";

        when(notificationService.markNotificationAsReadAndDelete(notificationId))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        // Ejecutar la solicitud PUT y verificar la respuesta
        webTestClient.put()
                .uri("/api-clients/v1.0/notifications/{notificationId}/read-and-delete", notificationId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CustomApiResponse.class)
                .value(response -> {
                    assertEquals("error", response.getStatus());
                    assertEquals(errorMessage, response.getMessage());
                });

        // Verificar que el servicio fue llamado
        verify(notificationService).markNotificationAsReadAndDelete(notificationId);
    }


}