package com.backend.service;

import com.backend.entity.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    User create(User user);

    User update(Long id, User user);

    User updateRole(Long id, String role);

    void delete(Long id);
}