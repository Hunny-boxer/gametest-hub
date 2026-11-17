
package com.gametesthub.identity.api;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gametesthub.config.SessionSecurityConfig;
import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserService;
import com.gametesthub.identity.AuthenticationService;
import com.gametesthub.identity.LoginService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = {
        AuthenticationController.class,
        RegistrationController.class,
        CurrentUserController.class,
        LogoutController.class
})
@Import({
        SessionSecurityConfig.class,
        AuthenticationHttpTest.TestSecurityConfiguration.class
})
class AuthenticationHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private AppUserService appUserService;

    @Test
    void shouldRejectLoginWithoutCsrfToken() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "qa@example.com",
                                    "password": "SecurePassword@2026"
                                }
                                """)
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(loginService);
    }

    @Test
    void shouldCreateSessionPersistLoginAndAllowLogout()
            throws Exception {

        String email = "qa@example.com";
        String password = "SecurePassword@2026";
        String displayName = "QA Engineer";

        Authentication authenticatedUser =
                UsernamePasswordAuthenticationToken.authenticated(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "AUTHENTICATED_USER"
                                )
                        )
                );

        when(loginService.login(email, password))
                .thenReturn(authenticatedUser);

        MockHttpSession initialSession = new MockHttpSession();
        String originalSessionId = initialSession.getId();

        MvcResult loginResult = mockMvc.perform(
                post("/api/v1/auth/login")
                        .session(initialSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "qa@example.com",
                                    "password": "SecurePassword@2026"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        MockHttpSession authenticatedSession =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(authenticatedSession);

        assertNotEquals(
                originalSessionId,
                authenticatedSession.getId(),
                "Session ID should change after authentication."
        );

        UUID userId = UUID.randomUUID();

        AppUser user = org.mockito.Mockito.mock(AppUser.class);

        when(appUserService.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(true);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        when(user.getDisplayName()).thenReturn(displayName);

        // Reuse the same authenticated session to load the user profile.
        mockMvc.perform(
                get("/api/v1/me")
                        .session(authenticatedSession)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value(displayName));

        // Log out using the authenticated session.
        mockMvc.perform(
                post("/api/v1/auth/logout")
                        .session(authenticatedSession)
                        .with(csrf())
        )
                .andExpect(status().isNoContent());

        assertTrue(
                authenticatedSession.isInvalid(),
                "Logout should invalidate the HTTP session."
        );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {

        String email = "qa@example.com";
        String password = "IncorrectPassword@2026";

        when(loginService.login(email, password))
                .thenThrow(
                        new BadCredentialsException(
                                "Invalid email or password."
                        )
                );

        MvcResult result = mockMvc.perform(
                post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "qa@example.com",
                                    "password": "IncorrectPassword@2026"
                                }
                                """)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid email or password.")
                )
                .andReturn();

        assertTrue(
                result.getRequest().getSession(false) == null,
                "Invalid credentials must not create an authenticated session."
        );
    }

    @Test
    void shouldRejectRegistrationWithoutCsrfToken()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "newqa@example.com",
                                    "displayName": "New QA",
                                    "password": "SecurePassword@2026"
                                }
                                """)
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldRejectProfileRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/me")
        )
                .andExpect(status().isForbidden());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain authenticationHttpTestSecurityFilterChain(
                HttpSecurity http,
                SecurityContextRepository securityContextRepository
        ) throws Exception {

            http
                    .csrf(csrf -> csrf
                            .csrfTokenRepository(
                                    CookieCsrfTokenRepository.withHttpOnlyFalse()
                            )
                    )

                    .formLogin(AbstractHttpConfigurer::disable)

                    .httpBasic(AbstractHttpConfigurer::disable)

                    .sessionManagement(session ->
                            session.sessionCreationPolicy(
                                    SessionCreationPolicy.IF_REQUIRED
                            )
                    )

                    .securityContext(context -> context
                            .securityContextRepository(
                                    securityContextRepository
                            )
                            .requireExplicitSave(true)
                    )

                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(
                                    "/api/v1/auth/csrf",
                                    "/api/v1/auth/login",
                                    "/api/v1/auth/register"
                            ).permitAll()

                            .anyRequest().authenticated()
                    );

            return http.build();
        }
    }
}
