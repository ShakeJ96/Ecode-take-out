package com.ecode.service;

import com.ecode.dto.UserLoginDTO;
import com.ecode.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
