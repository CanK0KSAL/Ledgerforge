package com.cankoksal.ledgerforge.security;

import com.cankoksal.ledgerforge.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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


        SecretKey signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
        // Tokenı imzalamak için kullanacağımız özel anahtarı hazırlıyor.

        return Jwts.builder() // Yeni JWT yapmaya başla
                .subject(user.getId().toString())// Bu token hangi kullanıcıya ait?
                .issuedAt(Date.from(now)) // Ne zaman üretildi?
                .expiration(Date.from(expiration)) // Biteceği tarih.
                .signWith(signingKey) // Bizim gizli anahtarımızla mühürle
                .compact(); // Jwt Stringine dönüştür.


        // Bu return Token oluşturma, token oluşturuyor.
    }

}