package com.example.devop.demo.infrastructure.authorization.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PermissionAction {
    private String code;

    private String name;

    public PermissionAction(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    public static PermissionAction View = new PermissionAction(ActionName.View.name(), "Xem");

    public static  PermissionAction Create = new PermissionAction(ActionName.Create.name(), "Thêm");

    public static  PermissionAction Update = new PermissionAction(ActionName.Update.name(), "Sửa");

    public static  PermissionAction Delete = new PermissionAction(ActionName.Delete.name(), "Xoá");


    public static  PermissionAction ChangeStatus = new PermissionAction(ActionName.ChangeStatus.name(), "Vô hiệu hoá, kích hoạt");

    public static  PermissionAction ChangePassword = new PermissionAction(ActionName.ChangePassword.name(), "Đổi mật khẩu");


    public static  PermissionAction LoginWebAdmin = new PermissionAction(ActionName.LoginWebAdmin.name(), "Đăng nhập web admin");

    public static  PermissionAction Export = new PermissionAction(ActionName.Export.name(), "Xuất file");

    public static  PermissionAction Import = new PermissionAction(ActionName.Import.name(), "Upload file");


    private static  PermissionAction[] _crud =
    {
        View,
        Create,
        Update,
        Delete
    };

    public static final List<PermissionAction> Crud = new ArrayList<>(Arrays.asList(_crud));

    private static PermissionAction[] _all =
    {
        View,
        Create,
        Update,
        Delete,
        ChangeStatus,
        ChangePassword,
        LoginWebAdmin,
        Export,
        Import
    };

    public static final List<PermissionAction> All = new ArrayList<>(Arrays.asList(_all));
}
