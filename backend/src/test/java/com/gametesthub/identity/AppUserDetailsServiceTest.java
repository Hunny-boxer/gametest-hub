
package com.gametesthub.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private UserCredentialRepository credentialRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void shouldLoadActiveUserWithStoredPasswordHash() {

        String email = "qa@example.com";
        String passwordHash = "$2a$12$examplePasswordHash";
        UUID userId = UUID.randomUUID();

        AppUser appUser = Mockito.mock(AppUser.class);
        UserCredential credential = Mockito.mock(UserCredential.class);

        when(appUserService.findByEmail(email))
                .thenReturn(Optional.of(appUser));

        when(appUser.getId()).thenReturn(userId);
        when(appUser.getEmail()).thenReturn(email);
        when(appUser.isActive()).thenReturn(true);

        when(credentialRepository.findById(userId))
                .thenReturn(Optional.of(credential));

        when(credential.getPasswordHash())
                .thenReturn(passwordHash);

        UserDetails userDetails =
                appUserDetailsService.loadUserByUsername(email);

        assertEquals(email, userDetails.getUsername());

        assertEquals(
                passwordHash,
                userDetails.getPassword()
        );

        assertTrue(userDetails.isEnabled());

        assertTrue(
                userDetails.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("AUTHENTICATED_USER")
                        )
        );
    }

    @Test
    void shouldRejectUnknownUser() {

        String email = "unknown@example.com";

        when(appUserService.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername(email)
        );

        verifyNoInteractions(credentialRepository);
    }

    @Test
    void shouldRejectUserWithoutCredentials() {

        String email = "qa@example.com";
        UUID userId = UUID.randomUUID();

        AppUser appUser = Mockito.mock(AppUser.class);

        when(appUserService.findByEmail(email))
                .thenReturn(Optional.of(appUser));

        when(appUser.getId()).thenReturn(userId);

        when(credentialRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername(email)
        );
    }

    @Test
    void shouldDisableInactiveUser() {

        String email = "inactive@example.com";
        UUID userId = UUID.randomUUID();

        AppUser appUser = Mockito.mock(AppUser.class);
        UserCredential credential = Mockito.mock(UserCredential.class);

        when(appUserService.findByEmail(email))
                .thenReturn(Optional.of(appUser));

        when(appUser.getId()).thenReturn(userId);
        when(appUser.getEmail()).thenReturn(email);
        when(appUser.isActive()).thenReturn(false);

        when(credentialRepository.findById(userId))
                .thenReturn(Optional.of(credential));

        when(credential.getPasswordHash())
                .thenReturn("$2a$12$examplePasswordHash");

        UserDetails userDetails =
                appUserDetailsService.loadUserByUsername(email);

        assertFalse(userDetails.isEnabled());
    }
}
