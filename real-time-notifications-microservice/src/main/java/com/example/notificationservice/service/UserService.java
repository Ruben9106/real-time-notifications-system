    package com.example.notificationservice.service;

    import com.example.notificationservice.entity.Notification;
    import com.example.notificationservice.entity.User;
    import com.example.notificationservice.repository.NotificationRepository;
    import com.example.notificationservice.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;

    import java.util.List;
    import java.util.UUID;

    @Service
    public class UserService {

        private final UserRepository userRepository;
        private final NotificationRepository notificationRepository;

        @Autowired
        public UserService(UserRepository userRepository, NotificationRepository notificationRepository) {
            this.userRepository = userRepository;
            this.notificationRepository = notificationRepository;
        }

        public Flux<User> getAllUsersWithNotificationMessages() {
            return userRepository.findAll()
                    .flatMap(user -> {
                        List<String> notificationIds = user.getNotifications();
                        if (!notificationIds.isEmpty()) {
                            return Mono.just(user); // Devolver el usuario con las notificaciones (mensajes) ya incluidas
                        }
                        return Mono.just(user); // Devolver el usuario sin hacer procesamiento adicional
                    });
        }

        //Guardar un usuario
        public <S extends User> Mono<S> saveUser(S user) {
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(UUID.randomUUID().toString().substring(0,6));  // Generar un ID único para el usuario
            }
            return userRepository.save(user)
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