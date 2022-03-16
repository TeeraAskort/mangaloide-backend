package com.alderaeney.mangaloidebackend.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreate {
    private String name;
    private String password;
    private String passwordRepeat;
}
