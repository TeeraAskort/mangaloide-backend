package com.alderaeney.mangaloidebackend.model.dto;

import java.io.Serializable;
import java.util.List;

import com.alderaeney.mangaloidebackend.model.Chapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComicView implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String author;
    private Boolean nsfw;
    private String description;
    private List<Chapter> chapters;
}
