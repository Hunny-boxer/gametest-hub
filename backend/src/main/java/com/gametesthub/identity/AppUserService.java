
package com.gametesthub.identity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Service
@Validated
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser createUser(
            @NotBlank
            @Email
            @Size(max = 320)
            String email,

            @NotBlank
            @Size(max = 120)
            String displayName
    ) {

        String normalizedEmail = email.trim();
        String normalizedName = displayName.trim();

        if (appUserRepository.existsByEmailIgnoreCase(
                normalizedEmail
        )) {
            throw new IllegalStateException(
                    "A user with this email already exists."
            );
        }

        AppUser user = new AppUser(
                normalizedEmail,
                normalizedName
        );

        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findById(UUID userId) {
        return appUserRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(
                email.trim()
        );
    }
}
