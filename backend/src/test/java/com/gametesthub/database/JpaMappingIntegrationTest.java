
package com.gametesthub.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManagerFactory;

import com.gametesthub.identity.AppUser;
import com.gametesthub.project.Project;
import com.gametesthub.workspace.Workspace;
import com.gametesthub.workspace.WorkspaceMember;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@Testcontainers
class JpaMappingIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("gametesthub_jpa_test")
                    .withUsername("gametesthub")
                    .withPassword("test-password");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
    }

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName(
            "Hibernate validates all four entities against PostgreSQL schema"
    )
    void shouldValidateAllEntityMappings() {

        assertNotNull(entityManagerFactory);

        assertNotNull(
                entityManagerFactory.getMetamodel().entity(AppUser.class)
        );

        assertNotNull(
                entityManagerFactory.getMetamodel().entity(Workspace.class)
        );

        assertNotNull(
                entityManagerFactory.getMetamodel().entity(
                        WorkspaceMember.class
                )
        );

        assertNotNull(
                entityManagerFactory.getMetamodel().entity(Project.class)
        );
    }
}
