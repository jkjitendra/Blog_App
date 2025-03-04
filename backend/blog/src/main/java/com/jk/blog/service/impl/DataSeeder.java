package com.jk.blog.service.impl;

import com.jk.blog.entity.Permission;
import com.jk.blog.entity.Role;
import com.jk.blog.repository.PermissionRepository;
import com.jk.blog.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @PostConstruct
    public void seedData() {
        seedPermissions();
        seedRoles();
        assignPermissionsToRoles();
    }

    /**
     * ✅ Inserts permissions into the database if they don't exist.
     */
    private void seedPermissions() {
        List<String> permissionNames = List.of(
                "USER_MANAGE", "CATEGORY_MANAGE", "ROLE_MANAGE",
                "POST_WRITE", "POST_READ", "POST_DELETE",
                "COMMENT_WRITE", "COMMENT_READ", "COMMENT_DELETE"
        );

        Set<String> existingPermissions = permissionRepository.findAll()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Permission> newPermissions = permissionNames.stream()
                .filter(permission -> !existingPermissions.contains(permission))
                .map(permissionName -> new Permission(null, permissionName))
                .collect(Collectors.toList());

        if (!newPermissions.isEmpty()) {
            permissionRepository.saveAll(newPermissions);
        }
    }

    /**
     * ✅ Inserts roles into the database if they don't exist.
     * ✅ Uses "ROLE_" prefix for Spring Security compatibility.
     */
    private void seedRoles() {
        List<String> roleNames = List.of("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USUAL", "ROLE_SUBSCRIBER");

        Set<String> existingRoles = roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        List<Role> newRoles = roleNames.stream()
                .filter(role -> !existingRoles.contains(role))
                .map(roleName -> Role.builder()
                        .name(roleName)
                        .permissions(new HashSet<>()) // Empty set of permissions initially
                        .build())
                .collect(Collectors.toList());

        if (!newRoles.isEmpty()) {
            roleRepository.saveAll(newRoles);
        }
    }

    /**
     * ✅ Assigns permissions to roles in the database.
     */
    private void assignPermissionsToRoles() {
        Map<String, Role> roles = roleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        Map<String, Permission> permissions = permissionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Permission::getName, permission -> permission));

        if (!roles.isEmpty() && !permissions.isEmpty()) {
            assignPermissions(roles.get("ROLE_ADMIN"), permissions,
                    "USER_MANAGE", "CATEGORY_MANAGE", "ROLE_MANAGE",
                    "POST_WRITE", "POST_READ", "POST_DELETE",
                    "COMMENT_READ", "COMMENT_WRITE", "COMMENT_DELETE");

            assignPermissions(roles.get("ROLE_MODERATOR"), permissions,
                    "CATEGORY_MANAGE",
                    "POST_WRITE", "POST_READ", "POST_DELETE",
                    "COMMENT_WRITE", "COMMENT_READ", "COMMENT_DELETE");

            assignPermissions(roles.get("ROLE_USUAL"), permissions,
                    "POST_WRITE", "POST_READ", "POST_DELETE",
                    "COMMENT_WRITE", "COMMENT_READ", "COMMENT_DELETE");

            assignPermissions(roles.get("ROLE_SUBSCRIBER"), permissions,
                    "POST_WRITE", "POST_READ", "POST_DELETE",
                    "COMMENT_WRITE", "COMMENT_READ", "COMMENT_DELETE");

            roleRepository.saveAll(roles.values());
        }
    }

    /**
     * ✅ Utility method to assign a filtered set of permissions to a role.
     */
    private void assignPermissions(Role role, Map<String, Permission> allPermissions, String... permissionNames) {
        if (role == null) return;

        Set<Permission> filteredPermissions = Arrays.stream(permissionNames)
                .map(allPermissions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        role.setPermissions(filteredPermissions);
    }
}
