package com.example.devop.demo.domain.role;

import com.example.devop.demo.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles", schema = "identity")
public class Role extends BaseEntity {

    private String code;

    private String name;

    private String description;

    private Boolean isActive;

    private Boolean IsDefault;

    @OneToMany(mappedBy = "role")
    private Set<RoleClaim> claims;
}
