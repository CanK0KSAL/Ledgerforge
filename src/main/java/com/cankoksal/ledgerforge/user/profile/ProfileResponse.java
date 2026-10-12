package com.cankoksal.ledgerforge.user.profile;

import com.cankoksal.ledgerforge.user.UserRole;

import java.util.UUID;

public record ProfileResponse (
    UUID id,
    String email,
    UserRole role
){

}
