package com.example.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.events.NotificationEventPublisher;
import com.example.notificationservice.exceptions.NotificationExceptions;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.utils.DateUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/**
 * Creación de Notificación:
Cuando se llama al método createNotification en NotificationService, se guarda la notificación en la base de datos usando notificationRepository.save(notification).
Después de que la notificación se guarda exitosamente (doOnSuccess), se llama al método publishEvent del NotificationEventPublisher.
Publicación del Evento:
El NotificationEventPublisher crea un nuevo NotificationEvent con el userId y el message de la notificación.
Luego, publica este evento usando publisher.publishEvent(event).
Escucha del Evento:
El NotificationEventListener escucha los eventos de tipo NotificationEvent.
Cuando se recibe un evento, el método handleNotificationEvent se ejecuta y realiza la lógica necesaria (en este caso, simplemente imprime un mensaje en la consola).
 */
@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationEventPublisher eventPublisher;

    public Mono<Notification> createNotification(Notification notification) {
        if (notification == null) throw NotificationExceptions.nullPointerError();//Se lanza una excepción si la notificación es nula
        notification.setTimestamp(DateUtils.now());//Se establece la fecha y hora actual en la notificación
        return notificationRepository.save(notification)
                .doOnSuccess( savedNotification -> 
                eventPublisher.publishEvent(savedNotification.getUserId(), savedNotification.getMessage()))
                .onErrorMap(e -> NotificationExceptions.errorCreatingNotification(e.getMessage()));
    }

    public Flux<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserId(userId)
        .switchIfEmpty(Mono.error(NotificationExceptions.notificationNotFound(userId)));
    }

    public Flux<Notification> getAllNotifications(){
        return notificationRepository.findAll();
    }
}
