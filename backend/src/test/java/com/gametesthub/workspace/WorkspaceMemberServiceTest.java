
package com.gametesthub.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceTest {

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private WorkspaceMemberService memberService;

    @Test
    void shouldAddMemberToWorkspace() {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "creator@example.com",
                "Workspace Creator"
        );

        AppUser user = new AppUser(
                "member@example.com",
                "QA Engineer"
        );

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                creator
        );

        when(memberRepository.existsByWorkspace_IdAndUser_Id(
                workspaceId,
                userId
        )).thenReturn(false);

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(memberRepository.save(any(WorkspaceMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceMember result = memberService.addMember(
                workspaceId,
                userId,
                WorkspaceMember.Role.QA_ENGINEER
        );

        assertEquals(workspace, result.getWorkspace());
        assertEquals(user, result.getUser());
        assertEquals(
                WorkspaceMember.Role.QA_ENGINEER,
                result.getRole()
        );

        verify(memberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void shouldRejectDuplicateMembership() {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(memberRepository.existsByWorkspace_IdAndUser_Id(
                workspaceId,
                userId
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> memberService.addMember(
                        workspaceId,
                        userId,
                        WorkspaceMember.Role.VIEWER
                )
        );

        verify(workspaceRepository, never()).findById(workspaceId);
        verify(appUserRepository, never()).findById(userId);
        verify(memberRepository, never())
                .save(any(WorkspaceMember.class));
    }

    @Test
    void shouldRejectInactiveUser() {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "creator@example.com",
                "Workspace Creator"
        );

        AppUser inactiveUser = new AppUser(
                "inactive@example.com",
                "Inactive User"
        );

        inactiveUser.deactivate();

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                creator
        );

        when(memberRepository.existsByWorkspace_IdAndUser_Id(
                workspaceId,
                userId
        )).thenReturn(false);

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.of(inactiveUser));

        assertThrows(
                IllegalStateException.class,
                () -> memberService.addMember(
                        workspaceId,
                        userId,
                        WorkspaceMember.Role.VIEWER
                )
        );

        verify(memberRepository, never())
                .save(any(WorkspaceMember.class));
    }

    @Test
    void shouldChangeMemberRole() {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "creator@example.com",
                "Workspace Creator"
        );

        AppUser user = new AppUser(
                "member@example.com",
                "QA Engineer"
        );

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                creator
        );

        WorkspaceMember member = new WorkspaceMember(
                workspace,
                user,
                WorkspaceMember.Role.VIEWER
        );

        when(memberRepository.findByWorkspace_IdAndUser_Id(
                workspaceId,
                userId
        )).thenReturn(Optional.of(member));

        when(memberRepository.save(any(WorkspaceMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceMember result = memberService.changeMemberRole(
                workspaceId,
                userId,
                WorkspaceMember.Role.QA_LEAD
        );

        assertEquals(
                WorkspaceMember.Role.QA_LEAD,
                result.getRole()
        );

        verify(memberRepository).save(member);
    }
}
