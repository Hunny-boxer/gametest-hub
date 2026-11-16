
package com.gametesthub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SessionSecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {

        HttpSessionSecurityContextRepository repository =
                new HttpSessionSecurityContextRepository();

        // Do not expose session identifiers in URLs.
        repository.setDisableUrlRewriting(true);

        return repository;
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {

        // Rotate the session ID after successful authentication.
        return new ChangeSessionIdAuthenticationStrategy();
    }
}
