package com.alderaeney.mangaloidebackend.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Comic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comic_id")
    private Long id;

    private String name;

    private String author;

    private Boolean finished;

    private Boolean nsfw;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "comic")
    private List<Chapter> chapters;

    @ManyToMany(mappedBy = "comics", cascade = CascadeType.ALL)
    private Set<Scanlation> scanlations;

    @ManyToMany(mappedBy = "comicsFollowing", cascade = CascadeType.ALL)
    private Set<User> usersFollowing;

}
