package com.jk.blog.repository;

import com.jk.blog.entity.Role;
import com.jk.blog.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String roleName);
}

