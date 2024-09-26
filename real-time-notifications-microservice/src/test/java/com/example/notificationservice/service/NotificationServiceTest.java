package com.example.notificationservice.service;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;
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
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;


public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void testGetUserNotifications() {
//        Notification notification = new Notification("1", "user1", "Notification message", Instant.now(), false);
//        when(notificationRepository.findByUserReferenceId(anyString())).thenReturn(Flux.just(notification));
//
//        Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> result = notificationService.getNotificationsStream("user1");
//
//        StepVerifier.create(result)
//                .expectNextMatches(response -> {
//                    CustomApiResponse<List<Notification>> customApiResponse = response.getBody();
//                    assert customApiResponse != null;
//                    return "success".equals(customApiResponse.getStatus()) && customApiResponse.getData().size() == 1;
//                })
//                .verifyComplete();
//    }

    @Test
    void testCreateNotification() {
        Notification notification = new Notification("1", "user1", "Notification message", Instant.now(), false);
        when(notificationRepository.save(notification)).thenReturn(Mono.just(notification));

        Mono<Notification> result = notificationService.createNotification("user1", notification);

        StepVerifier.create(result)
                .expectNextMatches(savedNotification -> "Notification message".equals(savedNotification.getMessage()))
                .verifyComplete();
    }

    @Test
    void shouldMarkNotificationAsReadAndDeleteWhenNotRead() {
        // Datos simulados
        String notificationId = "171c72";
        String userId = "2810ae";

        Notification notification = new Notification(notificationId, userId, "Prueba de leído y eliminado", Instant.now(), false);
        User user = new User(userId, "Cena John", "cena.john@example.com", Arrays.asList(notification.getMessage()));

        // Configuramos el comportamiento de los mocks
        when(notificationRepository.findById(notificationId)).thenReturn(Mono.just(notification));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(notificationRepository.save(notification)).thenReturn(Mono.just(notification));
        when(userRepository.save(user)).thenReturn(Mono.just(user));
        when(notificationRepository.deleteById(notificationId)).thenReturn(Mono.empty());

        // Llamar al método bajo prueba
        StepVerifier.create(notificationService.markNotificationAsReadAndDelete(notificationId))
                .expectNext("La notificación con ID: '" + notificationId + "' fue marcada como leída y eliminada con éxito.")
                .verifyComplete();

        // Verificar que se llamaron los métodos necesarios
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification); // Se guarda después de marcar como leída
        verify(userRepository).findById(userId);
        verify(userRepository).save(user); // Se guarda el usuario con la notificación eliminada
        verify(notificationRepository).deleteById(notificationId); // Se elimina la notificación
    }

    @Test
    void shouldDeleteWhenNotificationAlreadyRead() {
        // Datos simulados
        String notificationId = "171c72";
        String userId = "2810ae";

        Notification notification = new Notification(notificationId, userId, "Prueba ya leída", Instant.now(), true);
        User user = new User(userId, "Cena John", "cena.john@example.com", Arrays.asList(notification.getMessage()));

        // Configuramos el comportamiento de los mocks
        when(notificationRepository.findById(notificationId)).thenReturn(Mono.just(notification));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(userRepository.save(user)).thenReturn(Mono.just(user));
        when(notificationRepository.deleteById(notificationId)).thenReturn(Mono.empty());

        // Llamar al método bajo prueba
        StepVerifier.create(notificationService.markNotificationAsReadAndDelete(notificationId))
                .expectNext("La notificación con ID: '" + notificationId + "' ya estaba marcada como leída y ha sido eliminada.")
                .verifyComplete();

        // Verificar que se llamaron los métodos necesarios
        verify(notificationRepository).findById(notificationId);
        verify(userRepository).findById(userId);
        verify(userRepository).save(user); // Se guarda el usuario con la notificación eliminada
        verify(notificationRepository).deleteById(notificationId); // Se elimina la notificación
    }

    @Test
    void shouldReturnErrorWhenNotificationNotFound() {
        // Configuramos el comportamiento del mock para que no encuentre la notificación
        String notificationId = "nonExistingId";
        when(notificationRepository.findById(notificationId)).thenReturn(Mono.empty());

        // Llamar al método bajo prueba
        StepVerifier.create(notificationService.markNotificationAsReadAndDelete(notificationId))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("No se encontró la notificación con el ID proporcionado."))
                .verify();

        // Verificar que se llamó al metodo findById
        verify(notificationRepository).findById(notificationId);
    }


}