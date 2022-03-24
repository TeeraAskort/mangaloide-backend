package com.alderaeney.mangaloidebackend.service;

import java.util.Optional;

import com.alderaeney.mangaloidebackend.model.Chapter;
import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.repository.ChapterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ChapterService {

    private ChapterRepository repository;

    @Autowired
    public ChapterService(ChapterRepository repository) {
        this.repository = repository;
    }

    public Optional<Chapter> getChapterById(Long id) {
        return repository.findById(id);
    }

    public Page<Chapter> findAllByComic(Comic comic, int page) {
        Pageable pageable = PageRequest.of(page, 30);
        return repository.findAllByComic(comic, pageable);
    }

    public Optional<Chapter> findByComicAndNumber(Comic comic, Double number) {
        return repository.findByComicAndChNumber(comic, number);
    }

    public Chapter addChapter(Chapter chapter) {
        return repository.save(chapter);
    }

    public void removeChapter(Chapter chapter) {
        repository.delete(chapter);
    }

}
