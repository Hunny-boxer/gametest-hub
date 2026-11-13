
package com.gametesthub.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AppUserRepository appUserRepository;

    public WorkspaceMemberService(
            WorkspaceMemberRepository memberRepository,
            WorkspaceRepository workspaceRepository,
            AppUserRepository appUserRepository
    ) {
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public WorkspaceMember addMember(
            @NotNull UUID workspaceId,
            @NotNull UUID userId,
            @NotNull WorkspaceMember.Role role
    ) {

        if (memberRepository.existsByWorkspace_IdAndUser_Id(
                workspaceId,
                userId
        )) {
            throw new IllegalStateException(
                    "This user is already a member of the workspace."
            );
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workspace was not found."
                ));

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User was not found."
                ));

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "Inactive users cannot join a workspace."
            );
        }

        WorkspaceMember member = new WorkspaceMember(
                workspace,
                user,
                role
        );

        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMember> findMembers(
            @NotNull UUID workspaceId
    ) {
        return memberRepository.findAllByWorkspace_Id(workspaceId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMember> findUserMemberships(
            @NotNull UUID userId
    ) {
        return memberRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceMember> findMembership(
            @NotNull UUID workspaceId,
            @NotNull UUID userId
    ) {
        return memberRepository.findByWorkspace_IdAndUser_Id(
                workspaceId,
                userId
        );
    }

    @Transactional
    public WorkspaceMember changeMemberRole(
            @NotNull UUID workspaceId,
            @NotNull UUID userId,
            @NotNull WorkspaceMember.Role newRole
    ) {

        WorkspaceMember member = memberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workspace membership was not found."
                ));

        member.changeRole(newRole);

        return memberRepository.save(member);
    }
}
