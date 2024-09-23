package com.example.notificationservice.service;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.HttpResponse.ResponseUtil;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }


    // Flujo de notificaciones para un usuario utilizando SSE
    public Flux<Notification> getNotificationsStream(String userId) {
        return Flux.interval(Duration.ofMinutes(1))  // Emitir un evento cada minuto
                .flatMap(tick -> notificationRepository.findByUserReferenceId(userId)
                        .doOnNext(notification -> System.out.println("Emitida notificación: " + notification.getMessage()))  // Log para ver qué se está emitiendo
                )
                .repeat();  // Mantener el flujo activo
    }

    // Eliminar una notificación por su mensaje
    public Mono<String> deleteNotificationFromUser(String userId, String notificationMessage) {
        return notificationRepository.findByUserReferenceId(userId)
                .filter(notification -> notification.getMessage().equals(notificationMessage))
                .singleOrEmpty() // Tomamos el primer elemento que coincida, si existe
                .flatMap(notification -> notificationRepository.deleteById(notification.getId())
                        .thenReturn("La notificación con mensaje: '" + notificationMessage + "' ha sido eliminada con éxito."))
                .switchIfEmpty(Mono.error(new RuntimeException("No se encontró la notificación con el mensaje: " + notificationMessage)));
    }

    // Crear una nueva notificación para un usuario
    public Mono<Notification> createNotification(String userId, Notification notification) {
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString().substring(0, 6));  // Generar un ID único
        }
        notification.setUserReferenceId(userId);  // Establece la referencia del usuario
        notification.setTimestamp(Instant.now());  // Establece la marca de tiempo actual
        return notificationRepository.save(notification);  // Guardar la notificación
    }

    // Obtener todas las notificaciones de un usuario por su ID
    public Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> getUserNotifications(String userId) {
        return notificationRepository.findByUserReferenceId(userId)
                .collectList()  // Recolectamos todas las notificaciones en una lista
                .flatMap(notifications -> {
                    if (notifications.isEmpty()) {
                        return ResponseUtil.createErrorResponse("No se encontraron notificaciones para el usuario", HttpStatus.NOT_FOUND);
                    } else {
                        return ResponseUtil.createSuccessResponse("Notificaciones encontradas para el usuario", notifications);
                    }
                });
    }
}
