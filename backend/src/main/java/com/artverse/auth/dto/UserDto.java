package com.artverse.auth.dto;

import com.artverse.user.Role;
import com.artverse.user.User;

import java.util.Set;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String displayName,
        String avatarUrl,
        Set<Role> roles,
        boolean emailVerified
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRoles(),
                user.isEmailVerified()
        );
    }
}
