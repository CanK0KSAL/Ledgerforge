package com.cankoksal.ledgerforge.security;

import com.cankoksal.ledgerforge.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {

        Instant now = Instant.now();
        // Saati şu anı alıyor

        Instant expiration =
                now.plusMillis(expirationMs);
        // Bitiş zamanına değiniyor, application.properties'de yazdığımız
        // app.jwt.expiration-ms=900000'a sebep üstte ki anlık saatin üzerine bitişi hesaplıyor
        // 900 saniye = 15 dakika.


        return Jwts.builder() // Yeni JWT yapmaya başla
                .subject(user.getId().toString())// Bu token hangi kullanıcıya ait?
                .issuedAt(Date.from(now)) // Ne zaman üretildi?
                .expiration(Date.from(expiration)) // Biteceği tarih.
                .signWith(getSigningKey()) // Bizim gizli anahtarımızla mühürle
                .compact(); // Jwt Stringine dönüştür.


        // Bu return Token oluşturma, token oluşturuyor.
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }// Secret'ı token imzalama ve doğrulamada kullanacağımız key'e çeviriyor.
    public String extractSubject(String token) {
        Claims claims = Jwts.parser() // Gelen JWT’yi okumaya/kontrol etmeye hazırlan
                .verifyWith(getSigningKey())//bizim key ile signature doğrula
                .build() //parser hazır
                .parseSignedClaims(token) //Doğrulanmış claims'i getir
                .getPayload(); //  Doğrulanmış token'ın claims/payload kısmını al

        return claims.getSubject();// Doğrulanmış token'ın hangi kullanıcıya ait olduğunu döndürüyor.
    }
    public boolean isTokenValid(String token) {
        try {
            extractSubject(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}