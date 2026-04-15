package com.organizaai.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;


@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secretKey;

    // Gera um token JWT válido por 15 minutos.
    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(getSignInKey())
                .compact();
    }

    // Extrai o e-mail (subject) de dentro do token.
    public String extrairEmail(String token) {
        return extrairTodasClaims(token).getSubject();
    }

    // Verifica se o token é válido.
    public boolean isTokenValido(String token) {
        try {
            extrairTodasClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Qualquer problema com o token (expirado, malformado, assinatura errada) cai aqui.
            // Dica: Se quiser, você pode adicionar um log.error() aqui no futuro para debug.
            return false;
        }
    }

    // Extrai o payload (Claims) completo do token
    private Claims extrairTodasClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token) // Se estiver expirado, lança ExpiredJwtException aqui
                .getPayload();
    }

    // Gera o objeto SecretKey exigido pelo JJWT
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Date extrairDataExpiracao(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}