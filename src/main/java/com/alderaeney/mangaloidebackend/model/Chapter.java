package com.alderaeney.mangaloidebackend.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
@RequiredArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private Double number;

    @NonNull
    private String name;

    @NonNull
    private Long pages;

    @NonNull
    private String usernameUploader;

    @NonNull
    @ManyToOne(cascade = CascadeType.ALL)
    private Comic comic;

    @ManyToMany(mappedBy = "chaptersRead", cascade = CascadeType.ALL)
    private List<User> usersCompleted;

}
