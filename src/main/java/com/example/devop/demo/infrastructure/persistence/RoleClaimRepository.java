package com.example.devop.demo.infrastructure.persistence;

import com.example.devop.demo.domain.role.RoleClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleClaimRepository extends JpaRepository<RoleClaim, Long> {
    List<RoleClaim> findByRoleId(Long roleId);

    @Query("SELECT rc.claimValue FROM RoleClaim rc WHERE rc.role.id = :roleId AND rc.claimType = :claimType")
    List<String> findClaimValuesByRoleIdAndClaimType(@Param("roleId") Long roleId,
                                                     @Param("claimType") String claimType);
}
