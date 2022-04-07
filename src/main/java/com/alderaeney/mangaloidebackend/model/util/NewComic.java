package com.alderaeney.mangaloidebackend.model.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewComic {
    private String name;
    private String author;
    private String description;
    private Boolean nsfw;
}
