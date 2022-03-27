package com.alderaeney.mangaloidebackend.model.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChapterView implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Double number;
    private String name;
    private Long pages;
    private String usernameUploader;
}
