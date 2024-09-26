package com.example.notificationservice.service;


import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    // Sink para manejar múltiples suscriptores y emitir eventos de forma reactiva
    private final Sinks.Many<Notification> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    @Autowired
    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }
    // Eliminar una notificación por su mensaje
//    public Mono<String> deleteNotificationById(String notificationId) {
//        return notificationRepository.findById(notificationId)
//                .filter(Notification::isRead)  // Solo eliminar si la notificación ha sido leída
//                .flatMap(notification -> notificationRepository.deleteById(notificationId)
//                        .then(Mono.just("La notificación con ID: '" + notificationId + "' ha sido eliminada con éxito.")))
//                .switchIfEmpty(Mono.error(new RuntimeException("No se encontró la notificación leída con el ID proporcionado.")));
//    }

// Obtener todas las notificaciones de un usuario por su ID
//    public Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> getUserNotifications(String userId) {
//        return notificationRepository.findByUserReferenceId(userId)
//                .collectList()  // Recolectamos todas las notificaciones en una lista
//                .flatMap(notifications -> {
//                    if (notifications.isEmpty()) {
//                        return ResponseUtil.createErrorResponse("No se encontraron notificaciones para el usuario", HttpStatus.NOT_FOUND);
//                    } else {
//                        return ResponseUtil.createSuccessResponse("Notificaciones encontradas para el usuario", notifications);
//                    }
//                });
//    }

    // Flujo de notificaciones no leídas globalmente
    public Flux<Notification> getUnreadNotificationStream() {
        return notificationSink.asFlux();  // Retorna el flujo de notificaciones desde el Sink
    }

    @Scheduled(fixedRate = 60000)
    public void checkForUnreadNotifications() {
        System.out.println("\n\n ================== Buscando notificaciones no leídas ================== \n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'a las' HH:mm")
                .withZone(ZoneId.systemDefault());  // Zona horaria del sistema

        // Obtener todas las notificaciones no leídas
        Flux<Notification> unreadNotifications = notificationRepository.findAll()
                .filter(notification -> !notification.isRead());  // Filtrar solo las no leídas

        unreadNotifications
                .filter(notification -> notification.getUserReferenceId() != null)  // Filtrar notificaciones sin userReferenceId
                .groupBy(Notification::getUserReferenceId)  // Agrupar las notificaciones por usuario
                .flatMap(groupedFlux -> groupedFlux.collectList()
                        .flatMap(notifications -> {
                            // Buscar el usuario por su ID
                            return userRepository.findById(groupedFlux.key())
                                    .map(user -> {
                                        String userName = user.getName();
                                        notifications.forEach(notification -> {
                                            String formattedDate = formatter.format(notification.getTimestamp());
                                            System.out.println("Notificación para " + userName + ": "
                                                    + notification.getMessage() + " con la fecha " + formattedDate);
                                        });

                                        // Emitir las notificaciones agrupadas por nombre de usuario
                                        notifications.forEach(notificationSink::tryEmitNext);

                                        return notifications;
                                    })
                                    .onErrorResume(e -> {
                                        System.err.println("Error al buscar el usuario para el ID: " + groupedFlux.key());
                                        return Mono.empty();
                                    });
                        }))
                .subscribe();
    }



            // Flujo de notificaciones para un usuario específico utilizando SSE
            public Flux<Notification> getNotificationsStream(String userId) {
                return Flux.interval(Duration.ofMinutes(1))  // Emitir un evento cada minuto
                        .flatMap(tick -> notificationRepository.findByUserReferenceId(userId)  // Buscar notificaciones por ID de usuario
                                .doOnNext(notification -> System.out.println("Emitida notificación: " + notification.getMessage())))
                        .repeat();  // Mantener el flujo activo
            }



                    public Mono<String> markNotificationAsReadAndDelete(String notificationId) {
                        // Encontrar la notificación por ID
                        return notificationRepository.findById(notificationId)
                                .flatMap(notification -> {
                                    if (!notification.isRead()) {
                                        // Marcar como leída
                                        notification.setRead(true);
                                        return notificationRepository.save(notification) // Guardar la notificación con el estado actualizado
                                                .then(userRepository.findById(notification.getUserReferenceId()) // Encontrar al usuario dueño de la notificación
                                                        .flatMap(user -> {
                                                            // Remover la notificación de la lista de notificaciones del usuario
                                                            user.getNotifications().remove(notification.getMessage());
                                                            return userRepository.save(user) // Guardar los cambios en el usuario
                                                                    .then(notificationRepository.deleteById(notificationId)) // Eliminar la notificación de la colección de notificaciones
                                                                    .then(Mono.just("La notificación con ID: '" + notificationId + "' fue marcada como leída y eliminada con éxito."));
                                                        })
                                                );
                                    } else {
                                        // Ya está leída, solo la eliminamos
                                        return userRepository.findById(notification.getUserReferenceId())
                                                .flatMap(user -> {
                                                    // Remover la notificación de la lista de notificaciones del usuario
                                                    user.getNotifications().remove(notification.getMessage());
                                                    return userRepository.save(user) // Guardar los cambios en el usuario
                                                            .then(notificationRepository.deleteById(notificationId)) // Eliminar la notificación
                                                            .then(Mono.just("La notificación con ID: '" + notificationId + "' ya estaba marcada como leída y ha sido eliminada."));
                                                });
                                    }
                                })
                                .switchIfEmpty(Mono.error(new RuntimeException("No se encontró la notificación con el ID proporcionado.")));
                    }



                                        // Crear una nueva notificación para un usuario
                                        public Mono<Notification> createNotification(String userId, Notification notification) {
                                            if (notification.getId() == null || notification.getId().isEmpty()) {
                                                notification.setId(UUID.randomUUID().toString().substring(0, 6));  // Generar un ID único
                                            }
                                            notification.setUserReferenceId(userId);  // Establecer la referencia del usuario
                                            notification.setTimestamp(Instant.now());  // Establecer la marca de tiempo actual
                                            notification.setRead(false);  // La notificación es nueva, por lo tanto no está leída

                                            // Guardar la notificación
                                            return notificationRepository.save(notification)
                                                    .flatMap(savedNotification -> {
                                                        // Buscar el usuario por su ID
                                                        return userRepository.findById(userId)
                                                                .flatMap(user -> {
                                                                    // Agregar el ID de la nueva notificación a la lista de notificaciones del usuario
                                                                    user.getNotifications().add(savedNotification.getMessage());
                                                                    // Guardar el usuario actualizado
                                                                    return userRepository.save(user)
                                                                            .thenReturn(savedNotification);  // Devolver la notificación guardada
                                                                });
                                                    });
                                        }

//
}
