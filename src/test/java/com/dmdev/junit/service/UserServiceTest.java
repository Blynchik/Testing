package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
// каждый раз объект этого класса пересоздается заново
// для каждого теста
// при PER_CLASS объект этого класса создается
// единожды для всех тестов (в этом случает можно не использовать статик)
// т.к. уже есть конкретный объект
public class UserServiceTest {

    private UserService userService;

    @BeforeAll
    static void init(){
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare(){
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUsersAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();
        Assertions.assertTrue(users.isEmpty(), () -> "User List should be empty");
    }

    @Test
    void userSizeIfUserAdded(){
        System.out.println("Test 2: " + this);
        userService.add(new User());
        userService.add(new User());

        var users = userService.getAll();
        Assertions.assertEquals(2, users.size());
    }

    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: "+ this);
    }

    @AfterAll
    static void closeConnectionPool(){
        System.out.println("After all: ");
    }
}
