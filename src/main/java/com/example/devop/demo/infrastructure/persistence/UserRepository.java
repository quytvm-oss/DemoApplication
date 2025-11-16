package com.example.devop.demo.infrastructure.persistence;

import com.example.devop.demo.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    Optional<User> findByUserName(String name);

    Optional<User> findByUserNameOrContactNumber(String userName, String contactNumber);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") Long userId);
}
