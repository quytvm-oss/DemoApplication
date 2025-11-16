package com.example.devop.demo.infrastructure.persistence;

import com.example.devop.demo.domain.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    List<Role> findByNameInAndIsActiveTrue(List<String> names);
}
