package com.alderaeney.mangaloidebackend.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserChangePassword {
    private String oldPass;
    private String newPass;
    private String newPassRepeat;
}
