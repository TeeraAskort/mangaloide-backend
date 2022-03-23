package com.alderaeney.mangaloidebackend.repository;

import java.util.Optional;

import com.alderaeney.mangaloidebackend.model.Comic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {

    public Page<Comic> findByNameIgnoreCaseContaining(String name, Pageable pageable);

    public Optional<Comic> findByName(String name);
}
