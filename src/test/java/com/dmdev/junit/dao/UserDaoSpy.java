package com.dmdev.junit.dao;

import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

public class UserDaoSpy extends UserDao {
    private UserDao userDao;
    private Map<Integer, Boolean> answers = new HashMap<>();
//    private Answer1<Integer, Boolean> answer1;

    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
