package com.wy0225.imbrlabel.controller;

import com.wy0225.imbrlabel.config.RedisConfig;
import com.wy0225.imbrlabel.constant.*;
import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.utils.*;
import com.wy0225.imbrlabel.properties.*;
import com.wy0225.imbrlabel.pojo.DO.UserDO;
import com.wy0225.imbrlabel.pojo.DTO.UserDTO;
import com.wy0225.imbrlabel.pojo.DTO.UserRegisterDTO;
import com.wy0225.imbrlabel.pojo.Result;
import com.wy0225.imbrlabel.pojo.VO.UserVO;
import com.wy0225.imbrlabel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user") //不知道是什么路径，到时候改吧
@Slf4j

//登录&注册页相关接口
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    //登录
    //UserMapper里的SQL语句可能需要改一下
    @PostMapping("/login")
    public Result<?> login(@RequestBody UserDTO userDTO) {
        log.info("Login User: {}", userDTO.getUsername());

        UserDO userDO = userService.login(userDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userDO.getId());
        claims.put(JwtClaimsConstant.USERNAME, userDO.getUsername());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        // 保存到redis中
        redisTemplate.opsForValue().set(token, token, System.currentTimeMillis() + jwtProperties.getAdminTtl(), TimeUnit.MILLISECONDS);

        UserVO userVO = UserVO.builder()
                .id(userDO.getId())
                .username(userDO.getUsername())
                .nickname(userDO.getNickname())
                .token(token)
                .build();

        return Result.success(userVO);
    }

    //注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("Register User: {}", userRegisterDTO.getUsername());

        userService.register(userRegisterDTO);
        return Result.success();
    }

    // 更新密码
    @PostMapping("/updatePwd")
    public Result updataPwd(@RequestBody Map<String, String> parmas, @RequestHeader("token") String token) {
        String oldPwd = parmas.get("oldPwd");
        String newPwd = parmas.get("newPwd");
        String rePwd = parmas.get("rePwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要的参数");
        }

        // 原密码是否正确：获取原token中存储的id，用id查询密码（是加密过的），该密码与加密后的oldPwd进行对比
        Long currentId = BaseContext.getCurrentId();
        UserDO user = userService.getPwdById(currentId);
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(oldPwd.getBytes()))) {
            return Result.error("原密码填写不正确");
        }

        // 检验原密码和新密码是否一样
        if (oldPwd.equals(newPwd)) {
            return Result.error("新密码与原密码一致");
        }

        // 重设密码
        userService.update(UserDTO.builder()
                .id(currentId)
                .password(DigestUtils.md5DigestAsHex(newPwd.getBytes()))
                .username(user.getUsername())
                .build());

        // 删除redis中存储的旧token
        redisTemplate.delete(token);

        return Result.success();
    }
}
