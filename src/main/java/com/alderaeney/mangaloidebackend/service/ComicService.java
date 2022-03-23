package com.alderaeney.mangaloidebackend.service;

import java.util.List;
import java.util.Optional;

import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.repository.ComicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ComicService {

    private final ComicRepository repository;

    @Autowired
    public ComicService(ComicRepository repository) {
        this.repository = repository;
    }

    public Optional<Comic> getComicById(Long id) {
        return repository.findById(id);
    }

    public List<Comic> getAllComics() {
        return repository.findAll();
    }

    public Page<Comic> findAllByNameContaining(String name, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return repository.findByNameIgnoreCaseContaining(name, pageable);
    }
}
