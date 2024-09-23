package com.example.servicionotificaciones;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import ch.qos.logback.core.net.SyslogOutputStream;

@SpringBootApplication
public class notificationServiceApplication {
    public static void main(String[] args) {

        String uri = "mongodb://localhost:27017";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("notificacionesTesting");
            MongoCollection<Document> collectionUsu = database.getCollection("usuario_Tabla");
            MongoCollection<Document> collectionNoti = database.getCollection("usuario_Notificacion");
            System.out.println("Mongo Connection created successfully");
        } catch (Exception e) {
            System.out.println(e);
        }
        SpringApplication.run(notificationServiceApplication.class, args);
    }
}

