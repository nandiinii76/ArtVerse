package com.artverse.auth;

import com.artverse.auth.dto.AuthResponse;
import com.artverse.auth.dto.RegisterRequest;
import com.artverse.security.CustomUserDetailsService;
import com.artverse.security.JwtService;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock CustomUserDetailsService userDetailsService;
    @InjectMocks AuthService service;

    @Test
    void registerCreatesUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("nandi@example.com", "StrongPassword123!", "Nandini");
        when(users.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = service.register(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("nandi@example.com", response.user().email());
        verify(users).save(argThat(user ->
                "nandi@example.com".equals(user.getEmail())
                        && "encoded-password".equals(user.getPassword())
                        && "Nandini".equals(user.getDisplayName())));
    }

    @Test
    void registerRejectsExistingEmail() {
        RegisterRequest request = new RegisterRequest("nandi@example.com", "StrongPassword123!", "Nandini");
        when(users.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThrows(com.artverse.common.ApiException.class, () -> service.register(request));
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
}
