
package com.gametesthub.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserRepository;
import com.gametesthub.project.Project;
import com.gametesthub.project.ProjectRepository;
import com.gametesthub.workspace.Workspace;
import com.gametesthub.workspace.WorkspaceMember;
import com.gametesthub.workspace.WorkspaceMemberRepository;
import com.gametesthub.workspace.WorkspaceRepository;

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
class JpaPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("gametesthub_persistence_test")
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
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName(
            "Persist and retrieve users, workspaces, members and projects"
    )
    void shouldPersistAllFoundationEntities() {

        // 1. Create and persist a user.
        AppUser user = appUserRepository.saveAndFlush(
                new AppUser(
                        "qa@example.com",
                        "QA Engineer"
                )
        );

        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());

        // 2. Create a workspace owned by that user.
        Workspace workspace = workspaceRepository.saveAndFlush(
                new Workspace(
                        "Gaming QA",
                        "gaming-qa",
                        user
                )
        );

        assertNotNull(workspace.getId());
        assertNotNull(workspace.getCreatedAt());

        // 3. Add the user as the workspace owner.
        WorkspaceMember member = memberRepository.saveAndFlush(
                new WorkspaceMember(
                        workspace,
                        user,
                        WorkspaceMember.Role.OWNER
                )
        );

        WorkspaceMember.WorkspaceMemberId memberId =
                new WorkspaceMember.WorkspaceMemberId(
                        workspace.getId(),
                        user.getId()
                );

        assertNotNull(member.getJoinedAt());

        // 4. Create a project inside the workspace.
        Project project = projectRepository.saveAndFlush(
                new Project(
                        workspace,
                        "GameTest Hub",
                        "GTH",
                        "Gaming QA management platform",
                        user
                )
        );

        assertNotNull(project.getId());
        assertNotNull(project.getCreatedAt());

        // 5. Verify the saved user.
        assertTrue(
                appUserRepository.findByEmailIgnoreCase(
                        "QA@example.com"
                ).isPresent()
        );

        // 6. Verify the saved workspace.
        assertEquals(
                "gaming-qa",
                workspaceRepository.findBySlug("gaming-qa")
                        .orElseThrow()
                        .getSlug()
        );

        // 7. Verify the composite membership identifier and role.
        WorkspaceMember savedMember = memberRepository
                .findById(memberId)
                .orElseThrow();

        assertEquals(
                WorkspaceMember.Role.OWNER,
                savedMember.getRole()
        );

        // 8. Verify the saved project.
        Project savedProject = projectRepository
                .findByWorkspace_IdAndProjectKey(
                        workspace.getId(),
                        "GTH"
                )
                .orElseThrow();

        assertEquals("GameTest Hub", savedProject.getName());

        assertEquals(
                Project.ProjectStatus.ACTIVE,
                savedProject.getStatus()
        );
    }
}
