package com.jk.blog.utils;

import com.jk.blog.dto.AuthDTO.AuthenticatedUserDTO;
import org.springframework.stereotype.Component;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Component
public class AuthUtil {

    private final UserRepository userRepository;

    public AuthUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the authenticated user from the Security Context.
     * @return Authenticated User entity.
     * @throws ResourceNotFoundException if user is not found.
     */
    public AuthenticatedUserDTO getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();

            return userRepository.findByEmail(email)
                    .map(AuthenticatedUserDTO::fromUser)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        }
        return null;
    }

    /**
     * Checks if the user has a specific role.
     * @param user The User entity.
     * @param role The role to check.
     * @return true if the user has the role, false otherwise.
     */
    public boolean userHasRole(User user, String role) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_" + role));
    }
}
