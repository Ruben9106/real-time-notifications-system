package com.example.notificationservice.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.notificationservice.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
//Ejecuata filtro por cada request

public class JwtTokenValidator extends OncePerRequestFilter {

    // 1.Inyectamos por cunstructor
    // @Autowired
    private JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // Agregando loggers
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);

    // 2.Para conceder acceso al usuario
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Obtencion del Token del header del request
        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        //Validacion del token (JWT)
        if (jwtToken != null) {  // Corroborar que venga el token // Damos acceso a ese Usuario solo si el t

            jwtToken = jwtToken.substring(7); // extraer el token  a partir de la poscion 7

            // Decodificación y Validación del Token
            try {
                DecodedJWT decodedJWT = jwtUtils.valdiateToken(jwtToken); // Validar el token //solo si el token decodificado es valido , se valida
                // recuperamos las credenciale como string (Recuperamos permisos)// Extraemos a los usuarios y Autorizaciones  de jwtUtils
                String username = jwtUtils.extracUsername(decodedJWT); // Extraer el nombre de usuario
                String stringAuthorities = jwtUtils.getSpecificClaim(decodedJWT, "authorities").asString(); // authorities : Nombre del claim que quiero recuperar

                // SETEAMOS AL al SECURITY CONTEXT HOLDER convertimos
// Dame los permisos READ,DELETE,ETC separado por comas  15748  y yo te los convierto a una lista de permisos
                Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(stringAuthorities);
                SecurityContext context = SecurityContextHolder.getContext(); // Seteamos en SECURITY CONTEXT HOLDER extrayendo el contexto de springsecurity
                // Se delcara el objeto autentication para insertarlo en holder text,
                // El principa sera el usuario, la contrseña no es necesaria por seguridad y lo autorities(los permisos)
                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

                //Llamamos al context para enviarle la autenticacion del ususario
                context.setAuthentication(authentication);
                // Le seteamos el contexto
                SecurityContextHolder.setContext(context);

                // Log para depuración
                logger.info("Token válido para el usuario: {}", username);
            } catch (JWTVerificationException e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid or expired JWT token");
                logger.error("Token inválido: {}", e.getMessage());
                return;
            }
        } else {
            logger.warn("No se encontró el token en la solicitud");
        }
        // Si no enviamos token
        filterChain.doFilter(request, response);

    }

}
