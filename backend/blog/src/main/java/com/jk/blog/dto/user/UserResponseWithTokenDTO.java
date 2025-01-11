package com.jk.blog.dto.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseWithTokenDTO extends UserResponseBody {
    private String accessToken;

    public UserResponseWithTokenDTO(UserResponseBody userResponseDTO, String token) {
        super.setId(userResponseDTO.getId());
        super.setUserName(userResponseDTO.getUserName());
        super.setName(userResponseDTO.getName());
        super.setEmail(userResponseDTO.getEmail());
        super.setMobile(userResponseDTO.getMobile());
        super.setCountryName(userResponseDTO.getCountryName());
        super.setRoles(userResponseDTO.getRoles());
        this.accessToken = token;
    }
}