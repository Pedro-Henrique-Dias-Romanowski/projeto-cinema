package com.romanowski.pedro.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${spring.security.jwt.secret}")
    private String JWT_SECRET;


    public String verificarToken(String token){
        DecodedJWT decodedJWT;
        try{
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("Cinecom")
                    .build();

            decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch(JWTVerificationException e){
            // todo lancar uma exceção correta, fazer isso depois que criar a exception handler a a exceção personalizada
            throw new RuntimeException("Token JWT invalido ou expirado", e);
        }
    }
}
