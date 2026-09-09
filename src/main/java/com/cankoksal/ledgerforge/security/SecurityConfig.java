package com.cankoksal.ledgerforge.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService
    ) {
        // JwtService'i kullanarak JwtAuthenticationFilter oluştur ve döndür
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http.csrf(csrf -> csrf.disable());
        // Authentication cookie/session ile değil Bearer JWT ile taşındığı için
        // bu API'de CSRF korumasını kullanmıyoruz.

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // Kullanıcının login durumunu server session'ında tutmuyoruz.
                // Her request kendi JWT'sini getirip yeniden doğrulanacak.
        );

        http.authorizeHttpRequests(auth -> auth //HTTP endpoint'lerinin erişim kurallarını tanımlayacağım.
                .requestMatchers( //şu adresleri seç.
                        "/api/auth/register",
                        "/api/auth/login"
                ).permitAll() // authentication olmasa da geçebilir.
                .anyRequest().authenticated()
                // .anyRequest() yukarıda özel olarak söylemediğim geri kalan her şey.
                // .authenticated() bunlara girmek için SecurityContext'te authenticated kullanıcı bulunmalı.
        );

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                )
        );


        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
                // Bu yönetilen filter security chain'in NERESİNDE çalışsın?
                // JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter'dan önce çalışsın.
        );

        return http.build();
        //yukarıda tarif ettiğim bütün kurallardan gerçek SecurityFilterChaini oluştur
    }

}

// STATELESS
//→ seni session'da hatırlamasını önlemek.

// /login + /register
//→ public

// geri kalan
//→ authenticated