package com.alderaeney.mangaloidebackend.mapper;

import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.model.dto.ComicList;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ComicMapper {
    ComicMapper INSTANCE = Mappers.getMapper(ComicMapper.class);

    ComicList comicToComicList(Comic comic);

    Comic comicListToComic(ComicList comic);
}
