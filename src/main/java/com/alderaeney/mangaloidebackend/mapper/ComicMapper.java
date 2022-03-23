package com.alderaeney.mangaloidebackend.mapper;

import java.util.List;

import com.alderaeney.mangaloidebackend.model.Comic;
import com.alderaeney.mangaloidebackend.model.dto.ComicList;
import com.alderaeney.mangaloidebackend.model.dto.ComicView;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ComicMapper {
    ComicMapper INSTANCE = Mappers.getMapper(ComicMapper.class);

    ComicList comicToComicList(Comic comic);

    Comic comicListToComic(ComicList comic);

    List<ComicList> comicsToComicsList(List<Comic> comics);

    List<Comic> comicsListToComics(List<ComicList> comics);

    ComicView comicToComicView(Comic comic);

    Comic comicViewToComic(ComicView comic);
}
