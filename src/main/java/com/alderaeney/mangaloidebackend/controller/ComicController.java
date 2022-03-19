package com.alderaeney.mangaloidebackend.controller;

import java.util.Optional;

import javax.transaction.Transactional;

import com.alderaeney.mangaloidebackend.exceptions.ChapterNotUploaded;
import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.service.ChapterService;
import com.alderaeney.mangaloidebackend.service.ComicService;
import com.alderaeney.mangaloidebackend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "api/v1/comic")
public class ComicController {

    private ComicService comicService;
    private ChapterService chapterService;
    private UserService userService;

    private final String COMICSPATH = "comics/";

    private final String TMPPATH = "tmp/";

    private final List<String> ALLOWEDIMAGETYPES = List.of("image/png", "image/jpg", "image/jpeg", "image/gif");

    private final String ALLOWEDFILETYPE = "application/zip";

    private final Integer MAXFILESIZE = 31457280;

    @Autowired
    public ComicController(ComicService comicService, ChapterService chapterService, UserService userService) {
        this.comicService = comicService;
        this.chapterService = chapterService;
        this.userService = userService;
    }

    @PostMapping(path = "{id}/uploadChapter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public Comic uploadChapter(@RequestParam("file") MultipartFile chapterZip, @RequestParam("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            User us = user.get();

            try {
                if (chapterZip.getContentType().equals(ALLOWEDFILETYPE)) {

                } else {
                    throw new FileTypeNotAllowed(chapterZip.getContentType());
                }
            } catch (NullPointerException e) {
                throw new ChapterNotUploaded();
            }
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

}
