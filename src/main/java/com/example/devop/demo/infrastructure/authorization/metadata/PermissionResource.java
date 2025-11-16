package com.example.devop.demo.infrastructure.authorization.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PermissionResource {
    private String code;

    private String name;

    public PermissionResource(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    public static  PermissionResource Dashboard = new PermissionResource(ResourceName.Dashboard.name(), "Doanh thu thuần");

    public static  PermissionResource Categories = new PermissionResource(ResourceName.Categories.name(), "Quản lý danh mục");

    public static  PermissionResource Products = new PermissionResource(ResourceName.Products.name(), "Danh sách sản phẩm");

    public static  PermissionResource ContactUs = new PermissionResource(ResourceName.ContactUs.name(), "Yêu cầu hỗ trợ");

    public static  PermissionResource Customers = new PermissionResource(ResourceName.Customers.name(), "Khách hàng");

    public static  PermissionResource CustomerGroups = new PermissionResource(ResourceName.CustomerGroups.name(), "Nhóm khách hàng");

    public static  PermissionResource Users = new PermissionResource(ResourceName.Users.name(), "Danh sách người dùng");

    public static  PermissionResource InternalUsers = new PermissionResource(ResourceName.InternalUsers.name(), "Danh sách nhân viên");

    public static  PermissionResource Roles = new PermissionResource(ResourceName.Roles.name(), "Danh sách vai trò");

    public static  PermissionResource AboutUs = new PermissionResource(ResourceName.AboutUs.name(), "Giới Thiệu");

    private static  PermissionResource[] _all =
    {
        Dashboard,
        Categories,
        Products,
        ContactUs,
        Customers,
        CustomerGroups,
        InternalUsers,
        Users,
        Roles,
        AboutUs,
    };

    public static List<PermissionResource> All = new ArrayList<PermissionResource>(Arrays.asList(_all));
}
