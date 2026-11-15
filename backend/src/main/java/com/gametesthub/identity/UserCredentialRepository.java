
package com.gametesthub.identity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialRepository
        extends JpaRepository<UserCredential, UUID> {
}
