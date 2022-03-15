package com.alderaeney.mangaloidebackend.repository;

import com.alderaeney.mangaloidebackend.model.Scanlation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanlationRepository extends JpaRepository<Scanlation, Long> {

}
