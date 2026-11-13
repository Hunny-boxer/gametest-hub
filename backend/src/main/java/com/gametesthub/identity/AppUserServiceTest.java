
package com.gametesthub.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void shouldCreateUserWithNormalizedFields() {

        when(appUserRepository.existsByEmailIgnoreCase(
                "test@example.com"
        )).thenReturn(false);

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = appUserService.createUser(
                " test@example.com ",
                " Hunny Boxer "
        );

        assertEquals("test@example.com", result.getEmail());
        assertEquals("Hunny Boxer", result.getDisplayName());
        assertTrue(result.isActive());

        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        when(appUserRepository.existsByEmailIgnoreCase(
                "test@example.com"
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> appUserService.createUser(
                        "test@example.com",
                        "Hunny Boxer"
                )
        );

        verify(appUserRepository, never())
                .save(any(AppUser.class));
    }

    @Test
    void shouldFindUserById() {

        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser(
                "test@example.com",
                "Hunny Boxer"
        );

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Optional<AppUser> result =
                appUserService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldFindUserByNormalizedEmail() {

        AppUser user = new AppUser(
                "test@example.com",
                "Hunny Boxer"
        );

        when(appUserRepository.findByEmailIgnoreCase(
                "test@example.com"
        )).thenReturn(Optional.of(user));

        Optional<AppUser> result =
                appUserService.findByEmail(
                        " TEST@example.com "
                );

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }
}
