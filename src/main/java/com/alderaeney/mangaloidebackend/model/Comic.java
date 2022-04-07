package com.alderaeney.mangaloidebackend.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
@RequiredArgsConstructor
@Data
public class Comic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comic_id")
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String author;

    @NonNull
    private Boolean finished;

    @NonNull
    private String description;

    @NonNull
    private Boolean nsfw;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "comic")
    private List<Chapter> chapters;

    @ManyToMany(mappedBy = "comics", cascade = CascadeType.ALL)
    private List<Scanlation> scanlations;

    @ManyToMany(mappedBy = "comicsFollowing", cascade = CascadeType.ALL)
    private List<User> usersFollowing;

}
