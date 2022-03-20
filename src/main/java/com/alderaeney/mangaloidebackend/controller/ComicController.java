package com.alderaeney.mangaloidebackend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import com.alderaeney.mangaloidebackend.exceptions.CannotConvertImageException;
import com.alderaeney.mangaloidebackend.exceptions.ChapterNotUploaded;
import com.alderaeney.mangaloidebackend.model.Chapter;
import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.model.util.ChapterUpload;
import com.alderaeney.mangaloidebackend.service.ChapterService;
import com.alderaeney.mangaloidebackend.service.ComicService;
import com.alderaeney.mangaloidebackend.service.UserService;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.function.ServerRequest.Headers;

import java.awt.Color;
import java.awt.image.BufferedImage;

@RestController
@RequestMapping(path = "api/v1/comic")
public class ComicController {

    private ComicService comicService;
    private ChapterService chapterService;
    private UserService userService;

    private final String COMICSPATH = "comics" + File.separator;

    private final String TMPPATH = "tmp" + File.separator;

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
    public void uploadChapter(@PathVariable("id") Long id, @RequestBody ChapterUpload chapterUpload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            User us = user.get();
            Optional<Comic> comic = comicService.getComicById(id);
            if (comic.isPresent()) {
                Comic com = comic.get();
                try {
                    if (chapterUpload.getChapterZip().getContentType().equals(ALLOWEDFILETYPE)) {
                        List<String> imagesPath = this.unzipFile(chapterZip, com.getName(), chapterUpload.getNumber());
                        String path = COMICSPATH + com.getName() + "/" + chapterUpload.getNumber();
                        this.convertFilesToJPG(imagesPath, path);
                        Chapter chapter = new Chapter(chapterUpload.getNumber(),
                                chapterUpload.getName().isEmpty() ? null : chapterUpload.getName(), imagesPath.size(),
                                us.getName(), com);
                        chapterService.addChapter(chapter);
                    } else {
                        throw new FileTypeNotAllowed(chapterZip.getContentType());
                    }
                } catch (NullPointerException e) {
                    throw new ChapterNotUploaded();
                }
            } else {
                throw new ComicNotFoundException(id);
            }
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

    @GetMapping("{id}/chapter/{chNumber}/{imgNumber}")
    public ResponseEntity<byte[]> serveChapterImage(@PathVariable("id") Long id,
            @PathVariable("chNumber") Long chNumber, @PathVariable("imgNumber") Long imgNumber) {
        Optional<Comic> comic = comicService.getComicById(id);
        Path path = Paths.get(
                COMICSPATH + comic.get().getName() + File.separator + chNumber + File.separator + imgNumber + ".jpg");
        byte[] image = Files.readAllBytes(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(image.length);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    private String unzipFile(MultipartFile chapterZip, String comicName, Long number) {
        try {
            List<String> images = new ArrayList<>();
            ZipInputStream zis = new ZipInputStream(chapterZip.getInputStream());
            File tmpFolder = TMPPATH + File.separator + comicName + "-" + number;
            if (!tmpFolder.exists()) {
                tmpFolder.mkdir();
            }
            ZipEntry entry = zis.getNextEntry();
            MimetypesFileTypeMap mtft = new MimetypesFileTypeMap();
            while (entry != null) {
                boolean isAllowed = false;
                for (String type : ALLOWEDIMAGETYPES) {
                    if (mtft.getContentType(entry.getName()).equals(type)) {
                        isAllowed = true;
                        break;
                    }
                }
                if (isAllowed) {
                    String imagePath = tmpFolder + File.separator + entry.getName();
                    images.add(imagePath);
                    Files.copy(entry, imagePath, StandardCopyOption.REPLACE_EXISTING);
                }
                entry = zis.getNextEntry();
            }
            zis.closeEntry();
            return images;
        } catch (IOException e) {
            throw new InvalidZipFile();
        }
    }

    private void convertFilesToJPG(List<String> images, String path) {
        int number = 1;
        for (String image : images) {
            try {
                final FileInputStream fileInputStream = new FileInputStream(image);
                final BufferedImage bfImage = ImageIO.read(fileInputStream);
                fileInputStream.close();

                final BufferedImage convertedImage = new BufferedImage(bfImage.getWidth(), bfImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                convertedImage.createGraphics().drawImage(bfImage, 0, 0, Color.WHITE, null);

                final FileOutputStream fileOutputStream = new FileOutputStream(path + File.separator + number + ".jpg");
                final boolean canWrite = ImageIO.write(convertedImage, "jpg", fileOutputStream);
                fileOutputStream.close();

                if (!canWrite) {
                    throw new CannotConvertImageException();
                }

                number++;
            } catch (IOException e) {
                throw new CannotConvertImageException();
            }
        }
    }

    private void deleteTmpFolder(String comicName, Long number) {
        File tmpFolder = TMPPATH + File.separator + comicName + "-" + number;
        FileUtils.deleteDirectory(tmpFolder);
    }
}
