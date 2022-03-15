package com.alderaeney.mangaloidebackend.repository;

import com.alderaeney.mangaloidebackend.model.Chapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

}
