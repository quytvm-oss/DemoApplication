package com.example.devop.demo.infrastructure.seed;

import com.example.devop.demo.domain.role.Role;
import com.example.devop.demo.infrastructure.idGen.SnowflakeIdCustomGenerator;

import java.util.Arrays;
import java.util.List;

public class RoleSeedData {
    public static Role accountant() {
        Role r = new Role();
        r.setId(SnowflakeIdCustomGenerator.nextId());
        r.setName("Accountant");
        r.setCode("Accountant");
        r.setDescription("Kế toán");
        r.setIsActive(true);
        return r;
    }

    public static Role branchManager() {
        Role r = new Role();
        r.setId(SnowflakeIdCustomGenerator.nextId());
        r.setName("BranchManager");
        r.setCode("BranchManager");
        r.setDescription("Quản lý chi nhánh");
        r.setIsActive(true);
        return r;
    }

    public static Role hr() {
        Role r = new Role();
        r.setId(SnowflakeIdCustomGenerator.nextId());
        r.setName("HR");
        r.setCode("HR");
        r.setDescription("Nhân sự");
        r.setIsActive(true);
        return r;
    }

    public static Role user()
    {
        Role r = new Role();
        r.setId(SnowflakeIdCustomGenerator.nextId());
        r.setName("User");
        r.setCode("User");
        r.setDescription("Khách hàng");
        r.setIsActive(true);
        r.setIsDefault(true);
        return r;
    }

    public static Role internalUser()
    {
        Role r = new Role();
        r.setId(SnowflakeIdCustomGenerator.nextId());
        r.setName("InternalUser");
        r.setCode("InternalUser");
        r.setDescription("Nhân viên");
        r.setIsActive(true);
        r.setIsDefault(true);
        return r;
    };

    public static Role admin()
    {
        Role r = new Role();
        r.setId(SnowflakeIdCustomGenerator.nextId());
        r.setName("Admin");
        r.setCode("Admin");
        r.setDescription("Admin");
        r.setIsActive(true);
        r.setIsDefault(true);
        return r;
    };

    public static List<Role> defaultRoles() {
        return Arrays.asList(
                RoleSeedData.user(),
                RoleSeedData.internalUser(),
                RoleSeedData.admin()
        );
    }
}
