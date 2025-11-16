package com.example.devop.demo.domain.user;

import com.example.devop.demo.domain.BaseEntity;
import com.example.devop.demo.domain.role.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "identity")
public class User extends BaseEntity {

    private String firstName;

    private String lastName;

    private String userName;

    private String email;

    private String contactNumber;

    private Boolean isActive = true;

    private Boolean isLocked = false;

    private Boolean isContactPhoneConfirmed;

    private Boolean isEmailConfirmed;

    private Boolean requireUpdatePassword;

    private Boolean twoFactorEnabled;

    private Integer accessFailedCount;

    private LocalDateTime lastSignInDate;

    private LocalDateTime lockoutEndDate;

    private Integer lockoutEnabled;

    private LocalDateTime unlockDate;

    private String securityStamp;

    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserDeviceToken> deviceTokens = new HashSet<>();
}
