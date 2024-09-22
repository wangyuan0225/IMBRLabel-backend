package com.wy0225.imbrlabel.service.impl;

import com.wy0225.imbrlabel.constant.UserConstant;
import com.wy0225.imbrlabel.exception.PasswordErrorException;
import com.wy0225.imbrlabel.exception.UserNotFoundException;
import com.wy0225.imbrlabel.mapper.UserMapper;
import com.wy0225.imbrlabel.pojo.DO.UserDO;
import com.wy0225.imbrlabel.pojo.DTO.UserDTO;
import com.wy0225.imbrlabel.pojo.DTO.UserRegisterDTO;
import com.wy0225.imbrlabel.service.UserService;
import org.springframework.beans.BeanUtils;
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
        userRegisterDTO.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDO, userRegisterDTO);
        userDO.setNickname("新用户");//设置一下默认昵称，虽然好像没什么用
        userMapper.insert(userDO);
    }
}
