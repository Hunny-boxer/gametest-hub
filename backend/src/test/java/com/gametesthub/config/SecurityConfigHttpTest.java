
package com.gametesthub.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gametesthub.health.HealthController;
import com.gametesthub.identity.AppUserService;
import com.gametesthub.workspace.WorkspaceService;
import com.gametesthub.workspace.api.WorkspaceController;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = {
                HealthController.class,
                WorkspaceController.class
        }
)
@Import(SecurityConfig.class)
class SecurityConfigHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private AppUserService appUserService;

    @Test
    void shouldAllowHealthEndpointWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/health")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(
                        jsonPath("$.application")
                                .value("GameTest Hub")
                );
    }

    @Test
    void shouldDenyWorkspaceCreationWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Game Testing",
                                    "slug": "game-testing"
                                }
                                """)
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                workspaceService,
                appUserService
        );
    }
}
