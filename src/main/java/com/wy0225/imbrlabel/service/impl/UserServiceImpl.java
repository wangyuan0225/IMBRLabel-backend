package com.wy0225.imbrlabel.service.impl;

import com.wy0225.imbrlabel.constant.UserConstant;
import com.wy0225.imbrlabel.exception.PasswordErrorException;
import com.wy0225.imbrlabel.exception.ReSetPasswordNotMatchException;
import com.wy0225.imbrlabel.exception.UserAlreadyExistException;
import com.wy0225.imbrlabel.exception.UserNotFoundException;
import com.wy0225.imbrlabel.mapper.UserMapper;
import com.wy0225.imbrlabel.pojo.DO.UserDO;
import com.wy0225.imbrlabel.pojo.DTO.UserDTO;
import com.wy0225.imbrlabel.pojo.DTO.UserRegisterDTO;
import com.wy0225.imbrlabel.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDO login(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();

        //根据用户名查询数据库中的数据
        UserDO userDO=userMapper.getByUsername(username);

        //异常情况处理
        if(userDO==null){
            //账号不存在
            throw new UserNotFoundException(UserConstant.USER_NOT_REGISTER);
        }

        //用的md5加密
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        if(!password.equals(userDO.getPassword())){
            throw new PasswordErrorException(UserConstant.PASSWORD_ERROR);
        }

        return userDO;
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // 检查密码是否匹配
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getRepassword())) {
            throw new ReSetPasswordNotMatchException(UserConstant.RE_SET_PASSWORD_NOT_MATCH);
        }

        // 设置密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());

        // 检查用户名是否重复
        UserDO existingUser = userMapper.getByUsername(userRegisterDTO.getUsername());
        if (existingUser != null) {
            throw new UserAlreadyExistException(UserConstant.USER_ALREADY_EXIST);
        }

        // 创建新的 UserDO 对象
        UserDO userDO = new UserDO();
        userDO.setUsername(userRegisterDTO.getUsername());
        userDO.setPassword(encryptedPassword);
        userDO.setNickname("新用户"); // 设置默认昵称

        // 插入用户
        userMapper.insert(userDO);
    }
}
