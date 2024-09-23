package com.example.notificationservice.controller;

import com.example.notificationservice.HttpResponse.CustomApiResponse; // Cambio de nombre de ApiResponse a CustomApiResponse
import com.example.notificationservice.HttpResponse.ResponseUtil;
import com.example.notificationservice.entity.User;
import com.example.notificationservice.service.NotificationService;
import com.example.notificationservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api-clients/v1.0/users")
@Tag(name = "Users", description = "Operations related to User management in the notification system")
public class UserController {
    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get all users with their notifications", description = "Retrieve a list of users along with their notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    @GetMapping
    public Mono<ResponseEntity<CustomApiResponse<List<User>>>> getAllUsers() {
        return userService.getAllUsersWithNotificationMessages()
                .collectList()
                .flatMap(users -> {
                    if (users.isEmpty()) {
                        return ResponseUtil.createErrorResponse("No se encontraron usuarios.", HttpStatus.NOT_FOUND);
                    }
                    return ResponseUtil.createSuccessResponse("Usuarios encontrados", users);
                });
    }

    @Operation(summary = "Save a new user", description = "Create and save a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully saved",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "500", description = "Error saving the user")
    })
    @PostMapping
    public Mono<ResponseEntity<CustomApiResponse<User>>> saveUser(@RequestBody User user) {
        return userService.saveUser(user)
                .flatMap(savedUser -> ResponseUtil.createSuccessResponse("Usuario guardado con éxito.", savedUser))
                .onErrorResume(e -> ResponseUtil.createErrorResponse("Error al guardar el usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Operation(summary = "Delete a user by ID", description = "Delete a user from the system by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<CustomApiResponse<Void>>> deleteUser(@PathVariable String id) {
        return userService.deleteUserById(id)
                .flatMap(successMessage -> ResponseUtil.createSuccessResponse(successMessage, (Void) null))
                .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND));
    }
}