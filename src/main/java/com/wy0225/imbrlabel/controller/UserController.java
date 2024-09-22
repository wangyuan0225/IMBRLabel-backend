package com.wy0225.imbrlabel.controller;

import com.wy0225.imbrlabel.pojo.DO.UserDO;
import com.wy0225.imbrlabel.pojo.DTO.UserDTO;
import com.wy0225.imbrlabel.pojo.DTO.UserRegisterDTO;
import com.wy0225.imbrlabel.pojo.Result;
import com.wy0225.imbrlabel.pojo.VO.UserVO;
import com.wy0225.imbrlabel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user") //不知道是什么路径，到时候改吧
@Slf4j

//登录&注册页相关接口
public class UserController {
    @Autowired
    private UserService userService;

    //登录
    //UserMapper里的SQL语句可能需要改一下
    @PostMapping("/login")
    public Result<?> login(@RequestBody UserDTO userDTO) {
        log.info("Login User: {}", userDTO.getUsername());
        UserDO userDO=userService.login(userDTO);
        //以后如果需要数据回显的话
        //可以用下面的代码
//        UserVO userVO= UserVO.builder()
//                .id(userDO.getId())
//                .nickname(userDO.getNickname())
//                .username(userDO.getUsername())
//                .build();
//        return Result.success(userVO);

        return Result.success();
    }

    //注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("Register User: {}",userRegisterDTO .getUsername());

        userService.register(userRegisterDTO);
        return Result.success();
    }

}