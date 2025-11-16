package com.example.devop.demo.infrastructure.authorization.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class PermissionResourceAction {
    private PermissionResource Resource ;

    private List<PermissionAction> Actions ;

    private static final List<PermissionResourceAction> _all = List.of(
        create(PermissionResource.Dashboard, List.of(
            PermissionAction.View
        )),
        create(PermissionResource.Categories, concat(
            PermissionAction.Crud,
            List.of(PermissionAction.ChangeStatus)
        )),
        create(PermissionResource.Products, concat(
            PermissionAction.Crud,
            List.of(PermissionAction.Export)
        )),
        create(PermissionResource.ContactUs, List.of(
            PermissionAction.View,
            PermissionAction.Update
        )),
        create(PermissionResource.Customers, concat(
            PermissionAction.Crud,
            List.of(PermissionAction.Export, PermissionAction.Import)
        )),
        create(PermissionResource.CustomerGroups, concat(
            PermissionAction.Crud,
            List.of(PermissionAction.Import)
        )),
        create(PermissionResource.InternalUsers, concat(
            PermissionAction.Crud,
            List.of(
                PermissionAction.ChangeStatus,
                PermissionAction.ChangePassword,
                PermissionAction.Export,
                PermissionAction.LoginWebAdmin
            )
        )),
        create(PermissionResource.Users, concat(
            PermissionAction.Crud,
            List.of(
                PermissionAction.ChangeStatus,
                PermissionAction.ChangePassword
            )
        )),
        create(PermissionResource.Roles, PermissionAction.Crud),
        create(PermissionResource.AboutUs, List.of(
            PermissionAction.View,
            PermissionAction.Update
        ))
    );

    public static final List<PermissionResourceAction> All = Collections.unmodifiableList(_all);


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
