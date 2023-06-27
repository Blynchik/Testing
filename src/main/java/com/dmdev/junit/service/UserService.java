package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserService {
    private final List<User> users = new ArrayList<>();

    public List<User> getAll(){
        return users;
    }

    public void add(User... users) {
        this.users.addAll(Arrays.asList(users));
    }

    public Optional<User> login(String username, String password) {

        if(username == null || password == null){
            throw new IllegalArgumentException("username or password is null");
        }

        return users.stream()
                .filter(user->user.getUsername().equals(username))
                .filter(user->user.getPassword().equals(password))
                .findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
