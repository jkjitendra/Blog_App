package com.jk.blog.service.impl;

import com.jk.blog.entity.Role;
import com.jk.blog.entity.User;
import com.jk.blog.exception.InvalidRoleException;
import com.jk.blog.repository.RoleRepository;
import com.jk.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleServiceImpl {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new InvalidRoleException("Role '" + roleName + "' does not exist"));

        user.getRoles().add(role);
        userRepository.save(user);
    }
}
