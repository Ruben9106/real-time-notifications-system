package com.example.notificationservice.controller;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
class UserControllerTest {
    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;  // Agrega esto para simular NotificationService

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new UserController(userService, notificationService)).build();
    }

    @Test
    void testGetAllUsers() {
        // Crea datos de ejemplo
        User user1 = new User();
        user1.setId("1");
        user1.setName("User 1");
        user1.setNotifications(Arrays.asList("notif1", "notif2"));

        User user2 = new User();
        user2.setId("2");
        user2.setName("User 2");
        user2.setNotifications(Collections.emptyList());

        // Simula el servicio
        when(userService.getAllUsersWithNotificationMessages()).thenReturn(Flux.just(user1, user2));

        // Ejecuta la petición GET
        webTestClient.get().uri("/api-clients/v1.0/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomApiResponse.class)
                .consumeWith(response -> {
                    CustomApiResponse<?> customApiResponse = response.getResponseBody();
                    assert customApiResponse != null;
                    assert customApiResponse.getStatus().equals("success");
                    assert customApiResponse.getMessage().equals("Usuarios encontrados");
                });
    }

    @Test
    void testSaveUser() {
        // Crea un usuario de ejemplo
        User user = new User();
        user.setId("1");
        user.setName("Test User");

        // Simula el servicio
        when(userService.saveUser(any(User.class))).thenReturn(Mono.just(user));

        // Ejecuta la petición POST
        webTestClient.post().uri("/api-clients/v1.0/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomApiResponse.class)
                .consumeWith(response -> {
                    CustomApiResponse<?> customApiResponse = response.getResponseBody();
                    assert customApiResponse != null;
                    assert customApiResponse.getStatus().equals("success");
                    assert customApiResponse.getMessage().equals("Usuario guardado con éxito.");
                });
    }

    @Test
    void testDeleteUser() {
        // Simula el servicio
        when(userService.deleteUserById(anyString())).thenReturn(Mono.just("Usuario con ID: 1 ha sido eliminado con éxito."));

        // Ejecuta la petición DELETE
        webTestClient.delete().uri("/api-clients/v1.0/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomApiResponse.class)
                .consumeWith(response -> {
                    CustomApiResponse<?> customApiResponse = response.getResponseBody();
                    assert customApiResponse != null;
                    assert customApiResponse.getStatus().equals("success");
                    assert customApiResponse.getMessage().equals("Usuario con ID: 1 ha sido eliminado con éxito.");
                });
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Simula el servicio cuando el usuario no existe
        when(userService.deleteUserById(anyString())).thenReturn(Mono.error(new RuntimeException("Usuario no encontrado con ID: 1")));

        // Ejecuta la petición DELETE
        webTestClient.delete().uri("/api-clients/v1.0/users/1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CustomApiResponse.class)
                .consumeWith(response -> {
                    CustomApiResponse<?> customApiResponse = response.getResponseBody();
                    assert customApiResponse != null;
                    assert customApiResponse.getStatus().equals("error");
                    assert customApiResponse.getMessage().equals("Usuario no encontrado con ID: 1");
                });
    }

}