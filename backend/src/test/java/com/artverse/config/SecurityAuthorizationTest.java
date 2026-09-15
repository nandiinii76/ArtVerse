package com.artverse.config;

import com.artverse.admin.AdminController;
import com.artverse.artwork.ArtworkRepository;
import com.artverse.security.JwtAuthenticationFilter;
import com.artverse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "artverse.cors.allowed-origins=http://localhost:5173")
class SecurityAuthorizationTest {
    @Autowired MockMvc mvc;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean UserDetailsService userDetailsService;
    @MockBean UserRepository users;
    @MockBean ArtworkRepository artworks;

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
}
