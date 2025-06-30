package com.example.bank.service;

import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.mapper.UserMapper;
import com.example.bank.model.CreateUserDto;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserDto createUserDto) {
        if (createUserDto.getBlocked() == null) {
            createUserDto.setBlocked(false);
        }

        User user = UserMapper.toEntity(createUserDto);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    public User update(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new ResourceNotFoundException("User", "id", user.getUserId());
        }
        return userRepository.save(user);
    }
}
