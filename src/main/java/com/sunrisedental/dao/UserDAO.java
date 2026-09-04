package com.sunrisedental.dao;

import com.sunrisedental.model.User;
import java.util.List;

public interface UserDAO {
    User authenticate(String username, String password);
    User findByUsername(String username);
    List<User> getAllUsers();
    boolean save(User user);
}
