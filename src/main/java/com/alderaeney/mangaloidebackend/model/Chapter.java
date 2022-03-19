package com.alderaeney.mangaloidebackend.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double number;

    private String name;

    private Long pages;

    private Long number;

    private String usernameUploader;

    @ManyToOne(cascade = CascadeType.ALL)
    private Comic comic;

    @ManyToMany(mappedBy = "chaptersRead", cascade = CascadeType.ALL)
    private User usersCompleted;

}
