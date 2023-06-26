package com.dmdev.junit.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    @Test
    void usersEmptyIfNoUsersAdded() {
        var userService = new UserService();
        var users = userService.getAll();
        Assertions.assertTrue(users.isEmpty(), () -> "User List should be empty");
    }
}
