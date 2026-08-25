package com.cankoksal.ledgerforge.auth;

import com.cankoksal.ledgerforge.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(
                request.email(),
                request.password()
        );

        RegisterResponse response = RegisterResponse.of(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String token = authService.login(
                request.email(),
                request.password()
                // Request'ten email ve password'u alıp AuthService'e gönderiyoruz.
                // AuthService kullanıcıyı doğruluyor.
                // Doğruysa JwtService ile token üretiyor.
                // Geri gelen JWT String burada token değişkenine giriyor.
        );
        LoginResponse response = new LoginResponse(
                token,
                "Bearer"
        );
        // Client'a direkt String vermek yerine LoginResponse oluşturuyoruz.
        // accessToken = üretilen JWT
        // tokenType = Bearer
        //
        // Sonuç JSON'da kabaca:
        // {
        //   "accessToken": "eyJ...",
        //   "tokenType": "Bearer"
        // }


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

}