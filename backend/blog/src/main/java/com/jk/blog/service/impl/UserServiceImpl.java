package com.jk.blog.service.impl;

import com.jk.blog.dto.UserDTO;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.UserRepository;
import com.jk.blog.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = this.dtoToUser(userDTO);
        user.setActive(true);
        User savedUser = this.userRepository.save(user);
        UserDTO createdUserDto = this.userToDTO(savedUser);
        createdUserDto.setActive(true);
        return createdUserDto;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));

        return this.userToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> usersList = this.userRepository.findAll();
        return usersList.stream()
                        .map(this::userToDTO)
                        .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
//        user.setPassword(userDTO.getPassword());
//        user.setActive(userDTO.getActive());
        User updatedUser = this.userRepository.save(user);
        return this.userToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = this.userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        this.userRepository.delete(user);
    }

    public User dtoToUser(UserDTO userDTO) {
        return this.modelMapper.map(userDTO, User.class);
//        user.setId(userDTO.getId());
//        user.setName(userDTO.getName());
//        user.setEmail(userDTO.getEmail());
//        user.setPassword(userDTO.getPassword());
//        user.setActive(userDTO.getActive());
//        return user;
    }

    public UserDTO userToDTO(User user) {
        return this.modelMapper.map(user, UserDTO.class);
//        userDTO.setId(user.getId());
//        userDTO.setName(user.getName());
//        userDTO.setEmail(user.getEmail());
//        userDTO.setPassword(user.getPassword());
//        userDTO.setActive(user.getActive());
//        return userDTO;
    }
}
