package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;
import com.dmdev.junit.paramresolver.UserServiceParamResolver;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
// каждый раз объект этого класса пересоздается заново
// для каждого теста
// при PER_CLASS объект этого класса создается
// единожды для всех тестов (в этом случает можно не использовать статик)
// т.к. уже есть конкретный объект
@Tag("fast")
@Tag("user")
//можно запустить только те тесты, которые помечены тэгом
//или запустить исключив эти тесты через кастомный лаунчер
//или командную строку например
//mvn clean test -DexcludedGroups=login
//mvn clean test -Dgroups=login
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class) - если над тестом задан @Order
//то тест выполнится в заданном порядке, значения могут быть не последовательными
//@TestMethodOrder(MethodOrderer.MethodName.class) - в алфавитном порядке
//@TestMethodOrder(MethodOrderer.DisplayName.class) - в алфавитном порядке названия тестов в отображении с аннотацией @DisplayName
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before each: " + this);
        this.userService = userService;
    }

    @Test
//    @Order(1)
    void usersEmptyIfNoUsersAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        //Harmcrest
        MatcherAssert.assertThat(users, IsEmptyCollection.empty());

        //Assertions.assertTrue(users.isEmpty(), () -> "User List should be empty");
    }

    @Test
//    @Order(2)
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
    @Tag("login")
    @Disabled("check test") //отключает тест
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);
        var maybeUser = userService.login(IVAN.getUsername(), "dummy");

        Assertions.assertTrue(maybeUser.isEmpty());
    }

    @Test
    @Tag("login")
    @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)//повторяет тест несколько раз(указано)
    void loginFailIfUserDoesNotExist(RepetitionInfo repetitionInfo) {//repetitionInfo это одна из итераций теста, можно использовать дальше,а можно просто не вводить этот параметр
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

    //можно создать внутренний класс, где будут выполняться схожие тесты
    //можно вест класс сделать @Tag("login"), все методы будут с этим тегом
    @Nested
            //Сообщает, что внутри класса есть тесты, которые нужно выполнить
            // @DisplayName тоже можно
    class LoginTest {
        @Test
        @Tag("login")
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

        @Test//тест на время
        void checkLoginFunctionalityPerformance(){
    //        var result= Assertions.assertTimeout(Duration.ofMillis(200L), ()->{
            var result = Assertions.assertTimeoutPreemptively(Duration.ofMillis(200L),()->{ //то же самое, просто в другом потоке
                // а можно просто над любым тестом поставить @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
                Thread.sleep(300);
                return userService.login("dummy", IVAN.getPassword());
            });
        }

        @Test
        @Tag("login")
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
        @Tag("login")
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            var maybeUser = userService.login(IVAN.getUsername(), "dummy");

            Assertions.assertTrue(maybeUser.isEmpty());
        }

        @Test
        @Tag("login")
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);
            var maybeUser = userService.login("dummy", IVAN.getPassword());

            Assertions.assertTrue(maybeUser.isEmpty());
        }

        @ParameterizedTest(name = "{arguments} test")
        //  @ArgumentsSource() - принимает ArgumentProvider, который является массивом объектов
//        @NullSource //все 5 аннотации, являются @ArgumentSource с соответствующим праметром, но работают только с одним аргументом в методе
//        @EmptySource
//        @NullAndEmptySource
//        @ValueSource(strings = {"Ivan", "Petr"})//может принять примитивы, строки, массивы объекты
        //@EnumSource
        @MethodSource("com.dmdev.junit.service.UserServiceTest#getArgumentsForLoginTest")//принимает название статического метода или путь к нему
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)//можно передавать данные из файла, но тогда в аргументах не нужно Optional<User>, а в теле assertThat...
//        @CsvSource({
//                "Ivan,123",
//                "Petr,111"
//        })//как прошлая аннотация, но без отдельного файла
        @DisplayName("login param test")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);

            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
