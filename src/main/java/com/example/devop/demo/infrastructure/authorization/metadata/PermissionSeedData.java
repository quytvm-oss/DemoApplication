package com.example.devop.demo.infrastructure.authorization.metadata;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PermissionSeedData {
    public static final List<Permission> All = PermissionResourceAction.All.stream()
        .flatMap(x -> x.getActions().stream()
                .map(action -> new Permission(action.getCode(), x.getResource().getCode())))
        .collect(Collectors.toList());

    public static final List<Permission> Accountant = Stream.of(
            create(PermissionResource.Dashboard, List.of(PermissionAction.View, PermissionAction.Export)),
            create(PermissionResource.Roles, List.of(PermissionAction.View)),
            create(PermissionResource.InternalUsers, List.of(PermissionAction.LoginWebAdmin))
        )
        .flatMap(x -> x.getActions().stream()
            .map(action -> new Permission(action.getCode(), x.getResource().getCode())))
        .collect(Collectors.toList());

    public static final List<Permission> BranchManager = Stream.of(
        create(PermissionResource.Dashboard, List.of(PermissionAction.View)),
        create(PermissionResource.Roles, List.of(PermissionAction.View)),
        create(PermissionResource.Categories, List.of(PermissionAction.View)),
        create(PermissionResource.Customers, PermissionAction.Crud),
        create(PermissionResource.CustomerGroups,
            concat(PermissionAction.Crud, List.of(PermissionAction.Import))),
        create(PermissionResource.InternalUsers,
            concat(PermissionAction.Crud,
                List.of(
                    PermissionAction.ChangeStatus,
                    PermissionAction.ChangePassword,
                    PermissionAction.Export,
                    PermissionAction.LoginWebAdmin
                )))
    )
    .flatMap(x -> x.getActions().stream()
        .map(action -> new Permission(action.getCode(), x.getResource().getCode())))
    .collect(Collectors.toList());

    public static final List<Permission> HR = Stream.of(
        create(PermissionResource.AboutUs, List.of(PermissionAction.View)),
        create(PermissionResource.ContactUs, List.of(PermissionAction.View)),
        create(PermissionResource.InternalUsers,
            concat(PermissionAction.Crud,
                List.of(
                    PermissionAction.ChangeStatus,
                    PermissionAction.ChangePassword,
                    PermissionAction.Export,
                    PermissionAction.LoginWebAdmin
                )))
    )
    .flatMap(x -> x.getActions().stream()
        .map(action -> new Permission(action.getCode(), x.getResource().getCode())))
    .collect(Collectors.toList());



    private static PermissionResourceAction create(PermissionResource resource, List<PermissionAction> actions) {
        PermissionResourceAction pra = new PermissionResourceAction();
        pra.setResource(resource);
        pra.setActions(actions);
        return pra;
    }

    private static List<PermissionAction> concat(List<PermissionAction> base, List<PermissionAction> extra) {
        return Stream.concat(base.stream(), extra.stream()).collect(Collectors.toList());
    }
}
