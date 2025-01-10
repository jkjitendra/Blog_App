package com.jk.blog.service.impl;

import com.jk.blog.entity.User;
import org.springframework.stereotype.Service;

@Service
public class RBACServiceImpl {

    public boolean hasPermission(User user, String permissionName) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
}


