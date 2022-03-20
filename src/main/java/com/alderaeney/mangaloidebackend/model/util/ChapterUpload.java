package com.alderaeney.mangaloidebackend.model.util;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChapterUpload {
    private Double number;
    private String name;
    private MultipartFile chapterZip;
}
