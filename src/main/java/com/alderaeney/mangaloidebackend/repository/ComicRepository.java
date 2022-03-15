package com.alderaeney.mangaloidebackend.repository;

import com.alderaeney.mangaloidebackend.model.Comic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {

}
