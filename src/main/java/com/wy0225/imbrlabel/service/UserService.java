package com.wy0225.imbrlabel.service;
import com.wy0225.imbrlabel.pojo.DO.UserDO;
import com.wy0225.imbrlabel.pojo.DTO.UserDTO;
import com.wy0225.imbrlabel.pojo.DTO.UserRegisterDTO;

public interface UserService {

    UserDO login(UserDTO userDTO);

    void register(UserRegisterDTO userRegisterDTO);
}
