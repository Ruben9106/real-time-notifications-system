package com.example.notificationservice.util;

/**
 * Clase para trabajar con JWT
 */

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    //1.Necesitamos un user generaton
// Este valor security.jwt.key.private se toma del properties
    @Value("${security.jwt.key.private}")
    private String privateKey;

   @Value("${security.jwt.key.generator}")
    private String  userGenerator;

   // 2. Metodo de utileria que generara o crear  un token
    public String creatToken(Authentication authentication){

        //Definimos algoritmo de Encriptacion
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        //Extraer el usaurio que se debe autenticar
        String username = authentication.getPrincipal().toString(); // se guarda en el principal
        //Devuelve las auotorizaciones que tiene el usuario
        String authorities = authentication.getAuthorities()
                .stream()// Como devuelve una lista lo conveertimos en STREAM por lo que Devuelve las autorizaciones como STREAM
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // Devuelve "READ,WRITE"

        // Generamos el Token  (Codificacion)
        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)//Ususario que va generar token que solo conoce el backend
                .withSubject(username) // Al usaurio que se le va generar el token
                .withClaim("authorities", authorities) // aqui se generan los permisos que tiene el usuario (Payload)
                .withIssuedAt(new Date()) //Fecha en la que se genera el token
                .withExpiresAt(new Date(System.currentTimeMillis() + 1800000)) //Fecha o tiempo en la que expira el token
                .withJWTId(UUID.randomUUID().toString()) // Generar un token random
                .withNotBefore(new Date(System.currentTimeMillis())) // a partir de que momento el token es valido
                .sign(algorithm); // Firma : algoritmo de encriptacion

        return jwtToken;
    }


    // 3. Metodo para validar el token , devolvera el token decodificado
    public DecodedJWT valdiateToken(String token){
        try {
    //Decodificacion de TOKEN
            //Algoritmo con el que se encripta
            Algorithm algorithm = Algorithm.HMAC256(this.privateKey); // se necesita el algoritmo por el cual se encripto y la clave privada
            // Verificado si token es valido
            JWTVerifier verificar = JWT.require(algorithm) // requiere el algoritmo por el cual se encripto
                    .withIssuer(this.userGenerator)  // Descifra  solicitando el usaurio que genero el token
                    .build();

            //  Si el token es valido, devuelve el token decodificado
            DecodedJWT decodedJWT = verificar.verify(token);
            return  decodedJWT;

        } catch (JWTVerificationException exception){ // si no es valido el token  entra en la excepcion
            throw new JWTVerificationException("Token Invalido, not Authorized");
        }
    }


    // 4. Metodo para extraer el usuario que viene dentro del token (Parametro de entrada DecodedJWT:Decodifica)
    public String extracUsername(DecodedJWT decodedJWT){
        return decodedJWT.getSubject().toString(); // estoy obteniendo mi usaurio que viene dentro del token
    }

    // 5. Extraemos un Claim especifico
    public Claim getSpecificClaim(DecodedJWT decodedJWT, String clainName){ //
        return decodedJWT.getClaim(clainName); // extrae un claim (Payaload) se ocupa el nombre del claim
    }

    //6. Extraemos todos los claims
    public Map<String , Claim> returnAllClaims(DecodedJWT decodedJWT){
        return decodedJWT.getClaims(); // devuelce un Mapa con todo lo que tengas en el claim
    }

    // 2.Necesitamos la clave privada


}

