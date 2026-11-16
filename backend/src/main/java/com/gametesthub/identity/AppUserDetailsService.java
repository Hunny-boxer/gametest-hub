
package com.gametesthub.identity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private static final String INVALID_CREDENTIALS =
            "Invalid email or password.";

    private final AppUserService appUserService;
    private final UserCredentialRepository credentialRepository;

    public AppUserDetailsService(
            AppUserService appUserService,
            UserCredentialRepository credentialRepository
    ) {
        this.appUserService = appUserService;
        this.credentialRepository = credentialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {

        // Find the application account by email.
        AppUser appUser = appUserService.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                INVALID_CREDENTIALS
                        )
                );

        // Retrieve the separately stored password hash.
        UserCredential credential = credentialRepository
                .findById(appUser.getId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                INVALID_CREDENTIALS
                        )
                );

        // Provide account details to Spring Security.
        return User.withUsername(appUser.getEmail())
                .password(credential.getPasswordHash())
                .disabled(!appUser.isActive())
                .authorities("AUTHENTICATED_USER")
                .build();
    }
}
