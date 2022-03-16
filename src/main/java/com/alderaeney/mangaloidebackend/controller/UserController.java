package com.alderaeney.mangaloidebackend.controller;

import java.util.List;
import java.util.Optional;

import com.alderaeney.mangaloidebackend.exceptions.PasswordsDoNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.UserByUsernameNotFound;
import com.alderaeney.mangaloidebackend.model.util.UserCreate;
import com.alderaeney.mangaloidebackend.service.UserService;

import org.apache.tomcat.jni.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "api/v1/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private final String IMAGESPATH = "userImages/";

    private final List<String> ALLOWEDIMAGETYPES = List.of("image/png", "image/jpg", "image/jpeg",
            "image/gif");

    private final Integer MAXIMAGESIZE = 1048576;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(path = "login")
    public User login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

    @PostMapping(value = "create")
    public User creat(@RequestBody UserCreate userData) {
        Optional<User> userByName = userService.findByName(userData.getName());
        if (userByName.isPresent()) {
            throw new UsernameTakenException(userData.getName());
        } else {
            if (userData.getPassword().equals(userData.getPasswordRepeat())) {
                User user = new User(userData.getName(), passwordEncoder.encode(userData.getPassword());
                user.setAuthorities(List.of(new SimpleGrantedAuthority("USER")));
                return userService.addUser(user);
            } else {
                throw new PasswordsDoNotMatchException();
            }
        }
    }

}
