package com.artverse.auth;

import com.artverse.auth.dto.AuthResponse;
import com.artverse.auth.dto.LoginRequest;
import com.artverse.auth.dto.RegisterRequest;
import com.artverse.auth.dto.UserDto;
import com.artverse.common.ApiException;
import com.artverse.security.CustomUserDetailsService;
import com.artverse.security.JwtService;
import com.artverse.user.User;
import com.artverse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("EMAIL_TAKEN", "An account with this email already exists");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password"));

        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
        }

        if (!"refresh".equals(jwtService.extractTokenType(refreshToken))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Token is not a refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "User no longer exists"));

        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, UserDto.from(user));
    }
}
