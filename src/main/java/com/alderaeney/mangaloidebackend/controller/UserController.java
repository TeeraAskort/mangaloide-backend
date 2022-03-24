package com.alderaeney.mangaloidebackend.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.alderaeney.mangaloidebackend.exceptions.OldPasswordDoesNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.PasswordsDoNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.UserByUsernameNotFound;
import com.alderaeney.mangaloidebackend.exceptions.UsernameTakenException;
import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.model.util.UserChangePassword;
import com.alderaeney.mangaloidebackend.model.util.UserCreate;
import com.alderaeney.mangaloidebackend.service.UserService;

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
    public User create(@RequestBody UserCreate userData) {
        Optional<User> userByName = userService.findByName(userData.getName());
        if (userByName.isPresent()) {
            throw new UsernameTakenException(userData.getName());
        } else {
            if (userData.getPassword().equals(userData.getPasswordRepeat())) {
                User user = new User(userData.getName(), passwordEncoder.encode(userData.getPassword()));
                user.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("USER")));
                return userService.addUser(user);
            } else {
                throw new PasswordsDoNotMatchException();
            }
        }
    }

    @PostMapping(value = "changePassword")
    @Transactional
    public User changePassword(@RequestBody UserChangePassword passwords) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);

        if (user.isPresent()) {
            User us = user.get();
            if (passwordEncoder.matches(passwords.getOldPass(), us.getPassword())) {
                if (passwords.getNewPass().equals(passwords.getNewPassRepeat())) {
                    us.setPassword(passwordEncoder.encode(passwords.getNewPass()));
                    return us;
                } else {
                    throw new PasswordsDoNotMatchException();
                }
            } else {
                throw new OldPasswordDoesNotMatchException();
            }
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }
}
