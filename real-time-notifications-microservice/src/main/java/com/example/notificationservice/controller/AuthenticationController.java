package com.example.notificationservice.controller;

import com.example.notificationservice.dto.AuthLoginRequestDto;
import com.example.notificationservice.dto.AuthResponseDto;
import com.example.notificationservice.service.UserDetailJwtImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private UserDetailJwtImpl userDetailJwtImpl;

    //1. Metodo para crear usuario y otro para inciair sesion

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthLoginRequestDto userRequest){
        return new ResponseEntity<>(this.userDetailJwtImpl.logingUser(userRequest), HttpStatus.OK);
    }

    @GetMapping("/test")
    public String holaMundo() {
        return "Hello Word";
    }
}
