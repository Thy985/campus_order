package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.User;
import com.xingchen.backend.mapper.UserMapper;
import com.xingchen.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Optional<User> findByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectOneById(id));
    }

    @Override
    public User create(User user) {
        // Encrypt password
        if (user.getPassword() != null) {
            user.setPassword(encodePassword(user.getPassword()));
        }
        // Set default values
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(0);
        }
        if (user.getUserType() == null) {
            user.setUserType(0);
        }
        // Save user
        userMapper.insert(user);
        log.info("User created successfully: userId={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        userMapper.update(user);
        log.info("User updated successfully: userId={}", user.getId());
        return user;
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        String encodedPassword = encodePassword(newPassword);
        userMapper.updateByEmail(email, encodedPassword);
        log.info("Password updated successfully: email={}", email);
    }
}
