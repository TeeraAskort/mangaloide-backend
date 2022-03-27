package com.alderaeney.mangaloidebackend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import com.alderaeney.mangaloidebackend.exceptions.CannotConvertImageException;
import com.alderaeney.mangaloidebackend.exceptions.FileTooBigException;
import com.alderaeney.mangaloidebackend.exceptions.FileTypeNotAllowed;
import com.alderaeney.mangaloidebackend.exceptions.OldPasswordDoesNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.PasswordsDoNotMatchException;
import com.alderaeney.mangaloidebackend.exceptions.UserByUsernameNotFound;
import com.alderaeney.mangaloidebackend.exceptions.UsernameTakenException;
import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.model.util.UserChangePassword;
import com.alderaeney.mangaloidebackend.model.util.UserCreate;
import com.alderaeney.mangaloidebackend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.awt.Color;
import java.awt.image.BufferedImage;

@RestController
@RequestMapping(path = "api/v1/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private final String IMAGESPATH = "userImages/";

    private final List<String> ALLOWEDIMAGETYPES = Arrays.asList("image/png", "image/jpg", "image/jpeg",
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

    @PostMapping(path = "uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public User uploadUserImage(@RequestParam('file') MultipartFile image) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);

        if (user.isPresent()) {
            User us = user.get();
            boolean found = false;
            try {
                for(String type: ALLOWEDIMAGETYPES) {
                    if (image.getContentType().equals(type)) {
                        found = true;
                        break;
                    }
                }
            } catch (NullPointerException e) {
                return new ResponseEntity<>("You haven't provided an image", HttpStatus.NOT_FOUND);
            }

            if (!found) {
                throw new FileTypeNotAllowed(image.getContentType());
            }

            if (image.getSize() > MAXIMAGESIZE) {
                throw new FileTooBigException();
            }

            File storageFolder = new File(IMAGESPATH);

            if (!storageFolder.exists()) {
                storageFolder.mkdir();
            }

            this.convertImageToJPG(image, us.getName());
            return us;
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

    @GetMapping("profilePicture/{name}")
    public ResponseEntity<byte[]> fetchUserImage(@PathVariable("name") String name) {
        try {
            File file = new File(IMAGESPATH + name + ".jpg");
            Path path = null;

            if (!file.exists()) {
                path = Paths.get(IMAGESPATH + "default.jpg");
            } else {
                path = Paths.get(IMAGESPATH + name + ".jpg");
            }

            byte[] image = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(image.length);
            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void convertImageToJPG(MultipartFile image, String name) {
        try {
            final FileInputStream inputStream = image.getInputStream();
            final BufferedImage bfImage = ImageIO.read(inputStream);
            inputStream.close();

            final BufferedImage convertedImage = new BufferedImage(bfImage.getWidth(), bfImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            convertedImage.createGraphics().drawImage(bfImage, 0, 0, Color.WHITE, null);

            final FileOutputStream outputStream = new FileOutputStream(IMAGESPATH + File.separator + name + ".jpg");
            final boolean canWrite = ImageIO.write(convertedImage, "jpg", outputStream);
            outputStream.close();

            if (!canWrite) {
                throw new CannotConvertImageException();
            }
        } catch (IOException e) {
            throw new CannotConvertImageException();
        }
    }
}
