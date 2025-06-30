package com.example.bank.mapper;

import com.example.bank.model.Account;
import com.example.bank.model.CreateUserDto;
import com.example.bank.model.User;
import com.example.bank.model.UserDto;

public class UserMapper {
    public static User toEntity(CreateUserDto createUserDto) {
        User user = new User();
        user.setUserId(createUserDto.getUserId());
        user.setUsername(createUserDto.getUsername());
        user.setPassword(createUserDto.getPassword());
        user.setEmail(createUserDto.getEmail());
        user.setFirstName(createUserDto.getFirstName());
        user.setLastName(createUserDto.getLastName());
        user.setPhoneNumber(createUserDto.getPhoneNumber());
        user.setCreatedAt(createUserDto.getCreatedAt());
        user.setBlocked(createUserDto.getBlocked());
        user.setBlocked(createUserDto.getBlocked());

        return user;
    }

    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setPassword(user.getPassword());
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setBlocked(user.getBlocked());
        userDto.setBlocked(user.getBlocked());
        return userDto;
    }
}
