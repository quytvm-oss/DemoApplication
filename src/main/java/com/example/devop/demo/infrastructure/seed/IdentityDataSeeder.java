package com.example.devop.demo.infrastructure.seed;

import com.example.devop.demo.domain.role.Role;
import com.example.devop.demo.domain.role.RoleClaim;
import com.example.devop.demo.domain.user.User;
import com.example.devop.demo.infrastructure.idGen.SnowflakeIdCustomGenerator;
import com.example.devop.demo.infrastructure.persistence.RoleClaimRepository;
import com.example.devop.demo.infrastructure.persistence.RoleRepository;
import com.example.devop.demo.infrastructure.persistence.UserRepository;
import com.example.devop.demo.shared.utils.Claims;
import com.example.devop.demo.infrastructure.authorization.metadata.Permission;
import com.example.devop.demo.infrastructure.authorization.metadata.PermissionSeedData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class IdentityDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final RoleClaimRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityDataSeeder(RoleRepository roleRepository, RoleClaimRepository permissionRepository,
                              UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedDataRoles();
        seedUsers();
    }

    private void seedDataRoles() {
        if (roleRepository.count() == 0) {
            CreateRoleAndAssignPermissions(RoleSeedData.branchManager(), PermissionSeedData.BranchManager);
            CreateRoleAndAssignPermissions(RoleSeedData.accountant(), PermissionSeedData.Accountant);
            CreateRoleAndAssignPermissions(RoleSeedData.hr(), PermissionSeedData.HR);
        }

        for(Role role : RoleSeedData.defaultRoles()){
            Role currentRole = roleRepository.findByName(role.getName())
                    .orElseGet(() -> roleRepository.save(role));

            if (currentRole.getName().equals(RoleSeedData.admin().getName()))
            {
                AssignPermissionsToRoleAsync(PermissionSeedData.All, currentRole);
            }
        }
    }

    private void CreateRoleAndAssignPermissions(Role role, List<Permission> permissions)
    {
        var createdRole =  roleRepository.save(role);
        AssignPermissionsToRoleAsync(permissions, createdRole);
    }

    private void AssignPermissionsToRoleAsync(List<Permission> permissions, Role role){
        var currentClaims = permissionRepository.findByRoleId(role.getId());
        var listClaims = new ArrayList<RoleClaim>();
        for (Permission permission : permissions){
            boolean exists = currentClaims.stream()
                    .anyMatch(c -> c.getClaimType().equals(Claims.PERMISSION)
                            && c.getClaimValue().equals(permission.name()));
            if (exists) continue;
            log.info("Seeding {} Permission '{}'", role.getName(), permission.name());

            var roleClaim = new RoleClaim();
            roleClaim.setRoleId(role.getId());
            roleClaim.setClaimType(Claims.PERMISSION);
            roleClaim.setClaimValue(permission.name());
            listClaims.add(roleClaim);
        }
        if (!listClaims.isEmpty()) {
            permissionRepository.saveAll(listClaims);
        }
    }

    public void seedUsers() {
        Optional<User> adminUserOpt = userRepository.findByUserName("admin");

        if (adminUserOpt.isPresent()) {
            User adminUser = adminUserOpt.get();
            adminUser.setIsActive(true);
            userRepository.save(adminUser);
            return;
        }

        User admin = new User();
        admin.setId(SnowflakeIdCustomGenerator.nextId());
        admin.setUserName("admin");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setIsContactPhoneConfirmed(true);
        admin.setIsActive(true);
        admin.setPassword(passwordEncoder.encode("123456"));

        // Gán role Admin
        Role adminRole = roleRepository.findByName("Admin")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        log.info("Seeded admin user successfully.");
    }
}
