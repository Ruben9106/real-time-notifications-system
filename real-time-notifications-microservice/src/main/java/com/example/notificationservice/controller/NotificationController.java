package com.example.notificationservice.controller;

import com.example.notificationservice.HttpResponse.CustomApiResponse;
import com.example.notificationservice.HttpResponse.ResponseUtil;
import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-clients/v1.0/notifications")
@Tag(name = "Notifications", description = "Operations related to Notifications in the notification system")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

//    @Operation(summary = "Get user notifications", description = "Retrieve a list of notifications for a specific user")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
//                    content = @Content(schema = @Schema(implementation = Notification.class))),
//            @ApiResponse(responseCode = "404", description = "User not found")
//    })
//    @GetMapping("/users/{userId}")
//    public Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> getUserNotifications(@PathVariable String userId) {
//        return notificationService.getUserNotifications(userId);
//    }
    //    @DeleteMapping("{notificationId}")
    //    public Mono<ResponseEntity<CustomApiResponse<Void>>> deleteNotificationById(@PathVariable String notificationId) {
    //        return notificationService.deleteNotificationById(notificationId)
    //                .flatMap(successMessage -> ResponseUtil.createSuccessResponse(successMessage, (Void) null))
    //                .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND));
    //    }


            // Endpoint para obtener notificaciones no leídas en tiempo real para todos los usuarios
            @Operation(summary = "Stream unread notifications for all users", description = "Stream unread notifications in real time for all users in the system")
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully streaming unread notifications",
                            content = @Content(schema = @Schema(implementation = Notification.class)))
            })

            // Endpoint SSE para transmitir notificaciones no leídas en tiempo real de todos los usuarios existentes en bdd
            @GetMapping(value = "/users/unread-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
            public Flux<Notification> streamUnreadNotifications() {
                return notificationService.getUnreadNotificationStream();  // Flujo global de notificaciones no leídas
            }



    // Anotaciones para documentar el el edpoint de II-.  getNotificationsUserIdByMessage
    @Operation(summary = "Count userReferenceIds by message", description = "Count the number of unique userReferenceIds for each message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    // II. Verificar todos los userReferenceId que tiene el mismo mensaje o notificacions
    @GetMapping("/users/notifications")
    public Mono<ResponseEntity<CustomApiResponse<List<Notification>>>> getNotificationsUserIdforMessage(@RequestBody String message){
        return notificationService.getNotificationsByMessage(message);
    }


    // Anotaciones para documentar el el edpoint de I-. createNotification
    @Operation(summary = "Create a notification for a user", description = "Create a new notification for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification successfully created",
                    content = @Content(schema = @Schema(implementation = Notification.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    // I.Crear mensaje o notificacion al usuario
    @PostMapping("/users/{userId}")  // I. Metodo para crear la notificacion o el mensaje del usuario
    public Mono<ResponseEntity<CustomApiResponse<Notification>>> createNotification
    (@PathVariable String userId, @Valid @RequestBody NotificationRequest notificationRequest) {
        // 1.Creamos la  instancia de Notification  en el que setearemos el valor del mensaje
        Notification notification = new Notification();
        // 2. Recuperamos  solo el valor del campo "message de la clase notificationRequest"
        notification.setMessage(notificationRequest.getMessage());
        //3.  Llamamos al Servicio notificationService para crear la Notificación pasando el userId  y la instancia Notification
        return notificationService.createNotification(userId, notification)
                //4. con el ResponseUtil.createSuccessResponse => Nos indica si la creacion es exitosa y se envia la respuesta de mensaje en formato JSON
                .flatMap(createdNotification -> ResponseUtil.createSuccessResponse("Notificación creada con éxito", createdNotification))
                //5. ResponseUtil.createErrorResponse => Sí ocurre un error , envia el mensaje error con el status de respuesta http
                .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/{notificationId}/read-and-delete")
    @Operation(summary = "Mark a notification as read and delete it", description = "Marks a notification as read and then deletes it from both the notification collection and the user's notification list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification successfully marked as read and deleted"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public Mono<ResponseEntity<CustomApiResponse<Void>>> markNotificationAsReadAndDelete(@PathVariable String notificationId) {
        return notificationService.markNotificationAsReadAndDelete(notificationId)
                .flatMap(successMessage -> ResponseUtil.createSuccessResponse(successMessage, (Void) null))
                .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND));
    }
    /*
                    //Endpoint para obtener las notificaciones de un usuario especifico
                    @Operation(summary = "Get stream user notifications", description = "Stream notifications for a specific user in real time")
                    @ApiResponses(value = {
                            @ApiResponse(responseCode = "200", description = "Successfully streaming notifications",
                                    content = @Content(schema = @Schema(implementation = Notification.class))),
                            @ApiResponse(responseCode = "404", description = "Notifications for a user not found")
                    })
                    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
                    public Flux<Notification> streamNotifications(@PathVariable String userId) {
                        return notificationService.getNotificationsStream(userId);  // Flujo de notificaciones para un usuario específico
                    }  */

/*
                            @Operation(summary = "Create a notification for a user", description = "Create a new notification for a specific user")
                            @ApiResponses(value = {
                                    @ApiResponse(responseCode = "201", description = "Notification successfully created",
                                            content = @Content(schema = @Schema(implementation = Notification.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request data")
                            })
                            @PostMapping("/users/{userId}")
                            public Mono<ResponseEntity<CustomApiResponse<Notification>>> createNotification(@PathVariable String userId, @RequestBody Notification notification) {
                                return notificationService.createNotification(userId, notification)
                                        .flatMap(createdNotification -> ResponseUtil.createSuccessResponse("Notificación creada con éxito", createdNotification))
                                        .onErrorResume(e -> ResponseUtil.createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
                            }  */









}