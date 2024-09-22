package com.wy0225.imbrlabel.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//用户注册数据传到后端
public class UserRegisterDTO {
    private String username;
    private String password;
    private String repassword;
}
