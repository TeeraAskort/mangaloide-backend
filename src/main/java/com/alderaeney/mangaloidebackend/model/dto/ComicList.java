package com.alderaeney.mangaloidebackend.model.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComicList implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String author;
    private Boolean nsfw;
}
