package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
// каждый раз объект этого класса пересоздается заново
// для каждого теста
// при PER_CLASS объект этого класса создается
// единожды для всех тестов (в этом случает можно не использовать статик)
// т.к. уже есть конкретный объект
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private UserService userService;

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUsersAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        //Harmcrest
        MatcherAssert.assertThat(users, IsEmptyCollection.empty());

        //Assertions.assertTrue(users.isEmpty(), () -> "User List should be empty");
    }

    @Test
    void userSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        var users = userService.getAll();
        //AssertJ
        assertThat(users).hasSize(2);
//        Assertions.assertEquals(2, users.size());
    }

    @Test
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        userService.add(PETR);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

        //AssertJ
        assertThat(maybeUser).isPresent();
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        Assertions.assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> Assertions.assertEquals(IVAN,user));
    }

    @Test
    void throwExceptionIfUsernameOrPasswordIsNull() {
        Assertions.assertAll(
                () -> {
                    var exception = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                    assertThat(exception.getMessage()).isEqualTo("username or password is null");
                },
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
        );
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();
//        AssertJ
//        Assertions.assertAll(
//                ()->assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
//                ()->assertThat(users).containsValues(IVAN, PETR)
//        );

        //Harmcrest
        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));
    }

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);
        var maybeUser = userService.login(IVAN.getUsername(), "dummy");

        Assertions.assertTrue(maybeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);
        var maybeUser = userService.login("dummy", IVAN.getPassword());

        Assertions.assertTrue(maybeUser.isEmpty());
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }
}
