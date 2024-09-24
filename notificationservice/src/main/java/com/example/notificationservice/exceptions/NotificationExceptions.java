package com.example.notificationservice.exceptions;

public class NotificationExceptions extends RuntimeException {

    public NotificationExceptions(String message) {
        super(message);
    }

    public static NotificationExceptions notificationNotFound(String id) {
        return new NotificationExceptions("No se pudo encontrar la notificación con el ID: " + id);
    }

    public static NotificationExceptions errorCreatingNotification(String message) {
        return new NotificationExceptions("Hubo un error al crear la notificación: " + message);
    }

    public static NotificationExceptions nullPointerError() {
        return new NotificationExceptions("Se ha producido un error de puntero nulo.");
    }

    public static NotificationExceptions illegalArgumentError(String argumentName) {
        return new NotificationExceptions("Argumento ilegal: " + argumentName);
    }

    public static NotificationExceptions databaseError() {
        return new NotificationExceptions("Se ha producido un error en la base de datos.");
    }

    public static NotificationExceptions networkError() {
        return new NotificationExceptions("Se ha producido un error de red.");
    }
    
}
