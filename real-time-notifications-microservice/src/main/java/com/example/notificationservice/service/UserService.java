    package com.example.notificationservice.service;

    import com.example.notificationservice.entity.Notification;
    import com.example.notificationservice.entity.User;
    import com.example.notificationservice.repository.NotificationRepository;
    import com.example.notificationservice.repository.UserRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;

    import java.util.List;
    import java.util.UUID;
    import java.util.stream.Collectors;

    @Service
    public class UserService {

        private final UserRepository userRepository;
        private final NotificationRepository notificationRepository;

        private static final Logger logger = LoggerFactory.getLogger(UserService.class);


        @Autowired
        public UserService(UserRepository userRepository, NotificationRepository notificationRepository) {
            this.userRepository = userRepository;
            this.notificationRepository = notificationRepository;
        }

        public Flux<User> getAllUsersWithNotificationMessages() {
            return userRepository.findAll()  // Obtener todos los usuarios
                    .flatMap(user -> {
                        // Buscar las notificaciones del usuario usando su userReferenceId
                        return notificationRepository.findByUserReferenceId(user.getId())
                                .collectList()  // Recoger las notificaciones en una lista
                                .map(notifications -> {
                                    // Convertir las notificaciones a mensajes y asignarlas al usuario
                                    List<String> notificationMessages = notifications.stream()
                                            .map(Notification::getMessage)
                                            .collect(Collectors.toList());
                                    user.setNotifications(notificationMessages);  // Asignar los mensajes de notificación al usuario
                                    return user;
                                })
                                .onErrorResume(e -> {
                                    // Manejo de errores
                                    logger.error("Error al obtener notificaciones para el usuario {}: {}", user.getId(), e.getMessage());
                                    return Mono.just(user);  // Devuelve el usuario sin notificaciones en caso de error
                                });
                                });
        }

        //Guardar un usuario
        public <S extends User> Mono<S> saveUser(S user) {
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(UUID.randomUUID().toString().substring(0,6));  // Generar un ID único para el usuario
            }
            return userRepository.save(user)
                    .doOnSuccess(savedUser -> logger.info("User saved: {}", savedUser))
                    .doOnError(error -> logger.error("Error saving user: {}", error.getMessage()))
                    .onErrorResume(e -> Mono.error(new RuntimeException("Error al guardar el usuario: " + e.getMessage())));
        }

        //eliminar un usuario por ID
        public Mono<String> deleteUserById(String id) {
            return userRepository.findById(id)
                    .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado con ID: " + id)))
                    .flatMap(user -> userRepository.deleteById(id)
                            .thenReturn("Usuario con ID: " + id + " ha sido eliminado con éxito.")
                    );
        }

    }