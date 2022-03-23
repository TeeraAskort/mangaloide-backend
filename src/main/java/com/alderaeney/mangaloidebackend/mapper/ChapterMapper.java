package com.alderaeney.mangaloidebackend.mapper;

import com.alderaeney.mangaloidebackend.model.Chapter;
import com.alderaeney.mangaloidebackend.model.dto.ChapterView;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ChapterMapper {
    ChapterMapper INSTANCE = Mappers.getMapper(ChapterMapper.class);

    Chapter chapterViewToChapter(ChapterView chapter);

    ChapterView chapterToChapterView(Chapter chapter);
}
