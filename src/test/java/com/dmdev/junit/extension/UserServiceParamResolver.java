package com.dmdev.junit.extension;

import com.dmdev.junit.dao.UserDao;
import com.dmdev.junit.service.UserService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

//если нет возможности использовать DI спринга, у JUnit тоже есть свой DI
public class UserServiceParamResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == UserService.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var store = extensionContext.getStore(ExtensionContext.Namespace.create(UserService.class));
        return store.getOrComputeIfAbsent(new UserService(new UserDao()), it -> new UserService(new UserDao()));
    }
}
