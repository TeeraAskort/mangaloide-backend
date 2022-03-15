package com.alderaeney.mangaloidebackend.repository;

import java.util.Optional;

import com.alderaeney.mangaloidebackend.model.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByName(String name);

    public Page<User> findAll(Pageable pageable);
}
