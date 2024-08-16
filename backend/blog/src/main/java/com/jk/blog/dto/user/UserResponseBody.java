package com.jk.blog.dto.user;

import com.jk.blog.dto.RoleDTO;
import com.jk.blog.dto.profile.ProfileResponseBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserResponseBody {

    private Long id;

    private String name;

    private String userName;

    private String email;

    private String mobile;

    private String countryName;

    private String userCreatedDate;

    private String userLastLoggedInDate;

    private boolean isUserDeleted;

    private ProfileResponseBody profile;

    private String userDeletionTimestamp;

    private Set<RoleDTO> roles = new HashSet<>();

}
