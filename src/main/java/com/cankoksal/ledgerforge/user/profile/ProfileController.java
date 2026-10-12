package com.cankoksal.ledgerforge.user.profile;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ProfileResponse getMyProfile(Authentication authentication) {

        UUID userId = UUID.fromString(
                authentication.getPrincipal().toString()
        );

        return profileService.getMyProfile(userId);
    }
}
