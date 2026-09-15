package com.artverse.config;

import com.artverse.admin.AdminController;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.security.CustomUserDetailsService;
import com.artverse.security.JwtService;
import com.artverse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityAuthorizationTest.TestSecurityConfig.class)
@TestPropertySource(properties = "artverse.cors.allowed-origins=http://localhost:5173")
class SecurityAuthorizationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ArtworkRepository artworks;

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotAccessAdminApi() throws Exception {
        mvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminApi() throws Exception {
        when(users.count()).thenReturn(1L);
        when(users.findAll()).thenReturn(java.util.List.of());
        when(artworks.count()).thenReturn(0L);

        mvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        UserRepository users() {
            return mock(UserRepository.class);
        }

        @Bean
        ArtworkRepository artworks() {
            return mock(ArtworkRepository.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean
        UserDetailsService userDetailsService() {
            return username -> {
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);
            };
        }

        @Bean
        org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated())
                    .httpBasic(withDefaults());
            return http.build();
        }
    }
}
