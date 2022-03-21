package com.alderaeney.mangaloidebackend.mapper;

import com.alderaeney.mangaloidebackend.model.dto.UserList;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserList userToUserList(User user);

    User userListToUser(UserList user);
}
