package com.alderaeney.mangaloidebackend.repository;

import java.util.Optional;

import com.alderaeney.mangaloidebackend.model.Chapter;
import com.alderaeney.mangaloidebackend.model.Comic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    public Page<Chapter> findAllByComic(Comic comic, Pageable pageable);

    public Optional<Chapter> findByComicAndChNumber(Comic comic, Double chNumber);

}
