package com.ustmd.labwork8.service;

import com.ustmd.labwork8.dto.UserDto;
import com.ustmd.labwork8.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
}
