package com.alderaeney.mangaloidebackend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import com.alderaeney.mangaloidebackend.exceptions.CannotConvertImageException;
import com.alderaeney.mangaloidebackend.exceptions.CannotDeleteFolderException;
import com.alderaeney.mangaloidebackend.exceptions.CannotReadImageException;
import com.alderaeney.mangaloidebackend.exceptions.ChapterNotFoundException;
import com.alderaeney.mangaloidebackend.exceptions.ChapterNotUploaded;
import com.alderaeney.mangaloidebackend.exceptions.ComicNotFoundException;
import com.alderaeney.mangaloidebackend.exceptions.FileTypeNotAllowed;
import com.alderaeney.mangaloidebackend.exceptions.InvalidZipFile;
import com.alderaeney.mangaloidebackend.exceptions.UserByUsernameNotFound;
import com.alderaeney.mangaloidebackend.mapper.ChapterMapper;
import com.alderaeney.mangaloidebackend.mapper.ComicMapper;
import com.alderaeney.mangaloidebackend.model.Chapter;
import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.model.SendPage;
import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.model.dto.ChapterView;
import com.alderaeney.mangaloidebackend.model.dto.ComicList;
import com.alderaeney.mangaloidebackend.model.dto.ComicView;
import com.alderaeney.mangaloidebackend.model.util.ChapterUpload;
import com.alderaeney.mangaloidebackend.model.util.NewComic;
import com.alderaeney.mangaloidebackend.service.ChapterService;
import com.alderaeney.mangaloidebackend.service.ComicService;
import com.alderaeney.mangaloidebackend.service.UserService;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.awt.image.BufferedImage;

@RestController
@RequestMapping(path = "api/v1/comic")
public class ComicController {

    @Autowired
    private ComicService comicService;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private UserService userService;

    private static final String COMICSPATH = "comics" + File.separator;

    private static final String TMPPATH = "tmp" + File.separator;

    private final List<String> ALLOWEDIMAGETYPES = Arrays.asList("image/png", "image/jpg", "image/jpeg",
            "image/gif");

    private static final String ALLOWEDFILETYPE = "application/zip";

    @PostMapping(path = "newComic", headers = { "content-type=multipart/mixed",
            "content-type=multipart/form-data" })
    public ComicView newComic(@ModelAttribute NewComic newComic, @RequestPart("image") MultipartFile image) {
        try {
            Comic comic = new Comic(newComic.getName(), newComic.getAuthor(), false, newComic.getNsfw());
            Comic result = comicService.save(comic);
            String path = COMICSPATH + File.separator + newComic.getName();
            Files.createDirectories(Paths.get(path));
            path += File.separator + "image.jpg";
            this.convertImageToJPG(image, path);
            return ComicMapper.INSTANCE.comicToComicView(result);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("search/{name}/{page}")
    public SendPage<ComicList> searchComic(@PathVariable("name") String name, @PathVariable("page") Integer pageN) {
        Page<Comic> page = comicService.findAllByNameContaining(name, pageN);
        return new SendPage<ComicList>(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
                ComicMapper.INSTANCE.comicsToComicsList(page.getContent()), page.getSort());
    }

    @GetMapping(path = "getLatestComics/{page}")
    public SendPage<ComicList> getLatestComics(@PathVariable("page") Integer page) {
        Page<Comic> comics = comicService.getLatestComics(page);
        return new SendPage<ComicList>(comics.getNumber(), comics.getSize(), comics.getTotalElements(),
                comics.getTotalPages(),
                ComicMapper.INSTANCE.comicsToComicsList(comics.getContent()), comics.getSort());
    }

    @GetMapping("{id}/image")
    public ResponseEntity<byte[]> fetchComicImage(@PathVariable("id") Long id) {
        try {
            Optional<Comic> comic = comicService.getComicById(id);
            if (comic.isPresent()) {
                Comic comi = comic.get();
                String route = COMICSPATH + File.separator + comi.getName() + File.separator + "image.jpg";
                File file = new File(route);
                Path path = null;
                if (file.exists()) {
                    path = Paths.get(route);
                    byte[] image = Files.readAllBytes(path);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    headers.setContentLength(image.length);
                    return new ResponseEntity<>(image, headers, HttpStatus.OK);
                } else {
                    throw new CannotReadImageException();
                }
            } else {
                throw new ComicNotFoundException(id);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping(path = "{id}/uploadChapter", headers = { "content-type=multipart/mixed",
            "content-type=multipart/form-data" })
    @Transactional
    public void uploadChapter(@PathVariable("id") Long id, @ModelAttribute ChapterUpload chapterUpload,
            @RequestPart("chapterZip") MultipartFile chapterZip) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            User us = user.get();
            Optional<Comic> comic = comicService.getComicById(id);
            if (comic.isPresent()) {
                Comic comi = comic.get();
                try {
                    if (chapterZip.getContentType().equals(ALLOWEDFILETYPE)) {
                        List<String> imagesPath = this.unzipFile(chapterZip, comi.getName(),
                                chapterUpload.getNumber());
                        String finalPath = COMICSPATH + comi.getName() + File.separator + chapterUpload.getNumber();
                        Path path = Paths.get(finalPath);
                        Files.createDirectories(path);
                        this.convertFilesToJPG(imagesPath, finalPath);
                        this.deleteTmpFolder(comi.getName(), chapterUpload.getNumber());
                        String name = chapterUpload.getName().isEmpty() ? "Chapter " + chapterUpload.getNumber()
                                : chapterUpload.getName();
                        Chapter chapter = new Chapter(chapterUpload.getNumber(), name, Long.valueOf(imagesPath.size()),
                                us.getName(), comi);
                        chapter = chapterService.addChapter(chapter);
                        comi.getChapters().add(chapter);
                    } else {
                        throw new FileTypeNotAllowed(chapterZip.getContentType());
                    }
                } catch (NullPointerException e) {
                    throw new ChapterNotUploaded();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            } else {
                throw new ComicNotFoundException(id);
            }
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

    @GetMapping("{id}/chapter/{number}/read")
    @Transactional
    public User readChapter(@PathVariable("id") Long id, @PathVariable("number") Double number) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            User us = user.get();
            Optional<Comic> comic = comicService.getComicById(id);
            if (comic.isPresent()) {
                Comic com = comic.get();
                Optional<Chapter> chapter = chapterService.findByComicAndNumber(com, number);
                if (chapter.isPresent()) {
                    Chapter chap = chapter.get();
                    chap.getUsersCompleted().add(us);
                    us.getChaptersRead().add(chap);
                    return us;
                } else {
                    throw new ChapterNotFoundException(number);
                }
            } else {
                throw new ComicNotFoundException(id);
            }
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

    @GetMapping("{id}/chapter/{number}/fetch")
    public ChapterView fetchChapterData(@PathVariable("id") Long id, @PathVariable("number") Double number) {
        Optional<Comic> comic = comicService.getComicById(id);
        if (comic.isPresent()) {
            Comic com = comic.get();
            Optional<Chapter> chapter = chapterService.findByComicAndNumber(com, number);
            if (chapter.isPresent()) {
                return ChapterMapper.INSTANCE.chapterToChapterView(chapter.get());
            } else {
                throw new ChapterNotFoundException(number);
            }
        } else {
            throw new ComicNotFoundException(id);
        }
    }

    @GetMapping("{id}/chapter/{chNumber}/{imgNumber}")
    public ResponseEntity<byte[]> serveChapterImage(@PathVariable("id") Long id,
            @PathVariable("chNumber") Double chNumber, @PathVariable("imgNumber") Long imgNumber) {
        try {
            Optional<Comic> comic = comicService.getComicById(id);
            Path path = Paths.get(
                    COMICSPATH + comic.get().getName() + File.separator + chNumber + File.separator + imgNumber
                            + ".jpg");
            byte[] image = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(image.length);
            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new CannotReadImageException();
        }
    }

    private List<String> unzipFile(MultipartFile file, String comicName, Double number) {
        try {
            List<String> files = new ArrayList<>();
            Path tmpFolder = Paths.get(TMPPATH + File.separator + comicName + "-" + number);
            ZipInputStream inputStream = new ZipInputStream(file.getInputStream());
            for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null;) {
                Path resolvedPath = tmpFolder.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(inputStream, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                    files.add(resolvedPath.toString());
                } else {
                    Files.createDirectories(resolvedPath);
                }
            }

            return files;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("{id}/follow")
    @Transactional
    public User followComic(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            User us = user.get();
            Optional<Comic> comic = comicService.getComicById(id);
            if (comic.isPresent()) {
                Comic com = comic.get();
                com.getUsersFollowing().add(us);
                us.getComicsFollowing().add(com);
                return us;
            } else {
                throw new ComicNotFoundException(id);
            }
        } else {
            throw new UserByUsernameNotFound(username);
        }
    }

    @GetMapping("{id}/fetch")
    public ComicView fetchComicData(@PathVariable("id") Long id) {
        Optional<Comic> comic = comicService.getComicById(id);
        if (comic.isPresent()) {
            return ComicMapper.INSTANCE.comicToComicView(comic.get());
        } else {
            throw new ComicNotFoundException(id);
        }
    }

    @GetMapping("{id}/unfollow")
    @Transactional
    public User unfollowComic(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            User us = user.get();
            Optional<Comic> comic = comicService.getComicById(id);
            if (comic.isPresent()) {
                Comic com = comic.get();
                com.getUsersFollowing().remove(us);
                us.getComicsFollowing().remove(com);
                return us;
            } else {
                throw new ComicNotFoundException(id);
            }
        } else {
            throw new UserByUsernameNotFound(username);
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

    private void convertImageToJPG(MultipartFile image, String path) {
        try {
            final InputStream inputStream = image.getInputStream();
            final BufferedImage bfImage = ImageIO.read(inputStream);
            inputStream.close();

            final BufferedImage convertedImage = new BufferedImage(bfImage.getWidth(), bfImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            convertedImage.createGraphics().drawImage(bfImage, 0, 0, Color.WHITE, null);

            final FileOutputStream outputStream = new FileOutputStream(path);
            final boolean canWrite = ImageIO.write(convertedImage, "jpg", outputStream);
            outputStream.close();

            if (!canWrite) {
                throw new CannotConvertImageException();
            }
        } catch (IOException e) {
            throw new CannotConvertImageException();
        }
    }

    private void deleteTmpFolder(String comicName, Double number) {
        try {
            File tmpFolder = new File(TMPPATH + File.separator + comicName + "-" + number);
            FileUtils.deleteDirectory(tmpFolder);
        } catch (IOException e) {
            throw new CannotDeleteFolderException();
        }
    }
}
