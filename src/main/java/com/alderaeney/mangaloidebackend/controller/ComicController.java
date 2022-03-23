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

    @GetMapping("search/{name}/{page}")
    public SendPage<ComicList> searchComic(@PathVariable("name") String name, @PathVariable("page") Integer pageN) {
        Page<Comic> page = comicService.findAllByNameContaining(name, pageN);
        return new SendPage<ComicList>(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
                ComicMapper.INSTANCE.comicsToComicsList(page.getContent()), page.getSort());
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
                        List<String> imagesPath = this.unzipFile(chapterUpload.getChapterZip(), com.getName(),
                                chapterUpload.getNumber());
                        String path = COMICSPATH + com.getName() + File.separator + chapterUpload.getNumber();
                        this.convertFilesToJPG(imagesPath, path);
                        String name = chapterUpload.getName().isEmpty() ? null : chapterUpload.getName();
                        Chapter chapter = new Chapter(chapterUpload.getNumber(), name, Long.valueOf(imagesPath.size()),
                                us.getName(), com);
                        chapterService.addChapter(chapter);
                    } else {
                        throw new FileTypeNotAllowed(chapterUpload.getChapterZip().getContentType());
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

    @GetMapping("{id}/chapter/{number}/read")
    @Transactional
    public User readChapter(@PathVariable("id") Long id, @PathVariable("number") Long number) {
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
    public ChapterView fetchChapterData(@PathVariable("id") Long id, @PathVariable("number") Long number) {
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
            @PathVariable("chNumber") Long chNumber, @PathVariable("imgNumber") Long imgNumber) {
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

    private List<String> unzipFile(MultipartFile chapterZip, String comicName, Double number) {
        try {
            List<String> images = new ArrayList<>();
            ZipInputStream zis = new ZipInputStream(chapterZip.getInputStream());
            File tmpFolder = new File(TMPPATH + File.separator + comicName + "-" + number);
            if (!tmpFolder.exists()) {
                tmpFolder.mkdir();
            }
            ZipEntry entry = zis.getNextEntry();
            MimetypesFileTypeMap mtft = new MimetypesFileTypeMap();
            byte[] buffer = new byte[1024];
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
                    File newFile = new File(imagePath);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                entry = zis.getNextEntry();
            }
            zis.closeEntry();
            return images;
        } catch (IOException e) {
            throw new InvalidZipFile();
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

    private void deleteTmpFolder(String comicName, Long number) {
        try {
            File tmpFolder = new File(TMPPATH + File.separator + comicName + "-" + number);
            FileUtils.deleteDirectory(tmpFolder);
        } catch (IOException e) {
            throw new CannotDeleteFolderException();
        }
    }
}
