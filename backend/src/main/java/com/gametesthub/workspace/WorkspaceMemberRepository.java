
package com.gametesthub.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMemberRepository
        extends JpaRepository<
                WorkspaceMember,
                WorkspaceMember.WorkspaceMemberId
        > {

    List<WorkspaceMember> findAllByWorkspace_Id(UUID workspaceId);

    List<WorkspaceMember> findAllByUser_Id(UUID userId);

    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(
            UUID workspaceId,
            UUID userId
    );

    boolean existsByWorkspace_IdAndUser_Id(
            UUID workspaceId,
            UUID userId
    );
}
