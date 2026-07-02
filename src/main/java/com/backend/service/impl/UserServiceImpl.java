package com.backend.service.impl;

import com.backend.entity.User;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.UserRepository;
import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    // Use injected PasswordEncoder bean (defined in config). This makes testing and config easier.
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user co id = " + id));
    }

    @Override
    public User create(User user) {
        user.setId(null);
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return repository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        user.setId(id);
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return repository.save(user);
    }

    @Override
    public User updateRole(Long id, String role) {
        User user = findById(id);
        user.setRole(role);
        return repository.save(user);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}