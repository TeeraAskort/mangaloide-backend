package com.alderaeney.mangaloidebackend.mapper;

import java.util.List;

import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.model.dto.UserList;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserList userToUserList(User user);

    User userListToUser(UserList user);

    List<User> usersListToUsers(List<UserList> users);

    List<UserList> usersToUsersList(List<User> users);
}
