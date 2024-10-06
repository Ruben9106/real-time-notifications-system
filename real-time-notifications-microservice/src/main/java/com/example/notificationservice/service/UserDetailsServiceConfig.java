/**
 * Este UserDetailServiceConfig es para definir usuarios en memoria y simulamos como si
 * se trajera los usuarios de BD
 */
package com.example.notificationservice.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> userDetailsList = new ArrayList<>();

        userDetailsList.add(User.withUsername("Ruben")
                .password(passwordEncoder().encode("1234"))
                .roles("ADMIN")
                .authorities("READ", "CREATE", "DELETE")
                .build());

        userDetailsList.add(User.withUsername("Eduardo")
                .password(passwordEncoder().encode("1234"))
                .roles("USER")
                .authorities("READ")
                .build());

        return new InMemoryUserDetailsManager(userDetailsList);
    }

    //
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCryptPasswordEncoder Este encripta las contraseñas
    }

    /*
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Codificar una contraseña
        String rawPassword = "1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Verificar la contraseña
        boolean isPasswordMatch = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("Password matches: " + isPasswordMatch);
    }  */
}
