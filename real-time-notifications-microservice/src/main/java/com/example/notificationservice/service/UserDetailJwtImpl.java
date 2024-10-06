package com.example.notificationservice.service;

import com.example.notificationservice.dto.AuthLoginRequestDto;
import com.example.notificationservice.dto.AuthResponseDto;
import com.example.notificationservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailJwtImpl {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsServiceConfig userDetailsServiceConfig;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    // Metodo para logearme con usuario y pasword para generar el token de accesos
    public AuthResponseDto logingUser(AuthLoginRequestDto authLoginRequestDto){

        //Recuperamos los valores de username y password
        String username = authLoginRequestDto.getUsername();
        String password = authLoginRequestDto.getPassword();

        // Autenticamos el usuario y password
        Authentication authentication = this.authenticate(username , password); // Autentication de recuperar el usuario y paswword

        // Si las credenciales del usaurio son correctas se envia para Guarda el ContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //Generamos el TOKEN
        String accessToken = jwtUtils.creatToken(authentication);
        // Devolvemos la respuesta estructurada en la clase  AuthResponseDto
        AuthResponseDto authResponseDto = new AuthResponseDto(username, "User logged succesfuly", accessToken, true );
        return authResponseDto;

    }

    // Metodo que se encarga de que las credenciales sean correctas
    public Authentication authenticate(String username , String password ){

        // Se envia el usuario que por el moemnto es simulado, pero debera buscarlo en la BD
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (username == null || password  == null) {
            throw new BadCredentialsException("Invalid username or password");
        }


        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

}


