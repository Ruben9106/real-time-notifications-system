package com.example.notificationservice.security;

import com.example.notificationservice.service.UserDetailsServiceConfig;
import com.example.notificationservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity   // habilitar la seguridad WEB
@EnableMethodSecurity
public class SecurityConfig {

    //
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceConfig userDetailsServiceConfig;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    //Definir condiciones, configuraremos el DelegatingFiltrerProxy  para que administre la seguridad  // El objeto HttpSecurity va pasando los filtros
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())//Vulnerabilidad web
                .httpBasic(Customizer.withDefaults())  // solo para logearse con usuario y contraseña
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Trabajar sin estado y no guardar la sesion en memoria y depende de la expiracionde token
                .authorizeHttpRequests(http -> {
                    //Configurar los edpoints publicos
                    http.requestMatchers(HttpMethod.POST, "/auth/login").permitAll();
                    http.requestMatchers(HttpMethod.GET,"/auth/**").permitAll();
                    //Configurar los edpoins privados de USERS
                    http.requestMatchers(HttpMethod.GET, "/api-clients/v1.0/users" ).hasAuthority("READ");
                    http.requestMatchers(HttpMethod.POST, "/api-clients/v1.0/users").hasAuthority("CREATE");
                    http.requestMatchers(HttpMethod.DELETE, "/api-clients/v1.0/users/{id}").hasAuthority("DELETE");
                    //Configurar los edpoins privados de NOTIFICATIONS
                    http.requestMatchers(HttpMethod.GET, " /api-clients/v1.0/notifications/users/notifications" ).hasAuthority("READ");
                    http.requestMatchers(HttpMethod.POST, "/api-clients/v1.0/notifications/users/{id}").hasAuthority("CREATE");
                    http.requestMatchers(HttpMethod.DELETE, "/api-clients/v1.0/notifications/{id}/notifications").hasAuthority("DELETE");

                    // Configurar el resto de Edpoint NO especificado
                    http.anyRequest().authenticated();
                } )
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class) //Necesitamos ejecutar este filtro  antes  de que se ejecute el filtro de aunteticacion
                .build();
    }


    // Definimos la autenticacion y puede tener diferentes providers
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    //Provedor de Autentication este se debe conectar  a la base de datos para poder tener a los usuarios
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        //Necesita un encripatdor y el componene para hacer llamado a la BD
        provider.setPasswordEncoder(passwordEncoder);  //Encripta y valida contraseñas
        provider.setUserDetailsService(userDetailsService); // Este userDetailsService se supone debe conectarse a BD pero ahorita es simulado
        return  provider;
    }

/*
    // Definimos datos en memoria simulando si los usarios se trajeran a BD
    @Bean
    public UserDetailsService userDetailsService(){

       List <UserDetails> userDetailsList = new ArrayList<>();

        userDetailsList.add(User.withUsername("Ruben")
                .password("1234")
                .roles("ADMIN")
                .authorities("READ","CREATE,DELETE")
                .build());

        userDetailsList.add(User.withUsername("Eduardo")
                .password("1234")
                .roles("USER")
                .authorities("READ")
                .build());

        return new InMemoryUserDetailsManager(userDetailsList);
    } */

/*
    //DEfinimos PaswworEcode
    @Bean
    public PasswordEncoder passwordEncoder() {

        //Para produccion encriptar contraseñas
    //return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();

    } */
}
